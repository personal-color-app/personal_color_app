import express from "express";
import { GoogleAuth } from "google-auth-library";
import { existsSync, readFileSync } from "node:fs";
import { dirname, resolve } from "node:path";
import { fileURLToPath } from "node:url";

loadEnvFile(resolve(dirname(fileURLToPath(import.meta.url)), "../.env"));

const app = express();
const port = Number(process.env.PORT || 8787);
const geminiModel = process.env.GEMINI_MODEL || "gemini-2.5-flash";
const geminiApiKey = process.env.GEMINI_API_KEY || "";
const naverClientId = process.env.NAVER_CLIENT_ID || "";
const naverClientSecret = process.env.NAVER_CLIENT_SECRET || process.env.NAVER_SECRET || "";

const curatedProducts = [
  { category: "상의", title: "라벤더 니트", subtitle: "여름 쿨톤용 부드러운 상의", colorHex: "#C9B8E8", searchKeywords: ["라벤더 니트", "여름쿨톤 상의"] },
  { category: "아우터", title: "네이비 재킷", subtitle: "겨울 쿨톤 대비를 정리하는 아우터", colorHex: "#1B2A4E", searchKeywords: ["네이비 재킷", "겨울쿨톤 아우터"] },
  { category: "립", title: "쿨 로즈 립", subtitle: "차분한 쿨톤 혈색", colorHex: "#B85C7B", searchKeywords: ["쿨톤 립", "로즈 립"] },
  { category: "아이", title: "모브 브라운 섀도", subtitle: "부드러운 쿨톤 음영", colorHex: "#9D8497", searchKeywords: ["모브 섀도", "쿨톤 섀도"] },
];

app.use(express.json({ limit: "18mb" }));

app.get("/health", (_req, res) => {
  res.json({
    ok: true,
    geminiConfigured: Boolean(geminiApiKey),
    naverShoppingConfigured: Boolean(naverClientId && naverClientSecret),
    fcmConfigured: Boolean(process.env.FCM_PROJECT_ID && process.env.GOOGLE_APPLICATION_CREDENTIALS),
  });
});

app.post("/v1/personal-color/analyze", async (req, res) => {
  try {
    if (!geminiApiKey) {
      return res.status(503).json({ error: "GEMINI_API_KEY is not configured on the proxy." });
    }
    const { imageBase64, mimeType = "image/jpeg" } = req.body || {};
    if (!imageBase64 || typeof imageBase64 !== "string") {
      return res.status(400).json({ error: "imageBase64 is required." });
    }
    if (Buffer.byteLength(imageBase64, "utf8") > 16 * 1024 * 1024) {
      return res.status(413).json({ error: "Image payload is too large for inline Gemini input." });
    }

    const prompt = [
      "You are a Korean personal color styling consultant for OliveMe.",
      "Analyze visible fashion/beauty color cues only. Do not infer identity, health, ethnicity, age, or sensitive traits.",
      "Return JSON only with schemaVersion, type, englishLabel, season, tone, undertone, depth, chroma, matchScore, confidenceScore, imageQuality, description, signature, palette, avoidColors, clothes, makeup, traits, keywords, storeSearchKeywords, contentStoryTags, disclaimer.",
      "Use Korean labels and valid hex colors. palette should include 6 distinct recommended colors. makeup must include concrete lip, eye, base, and cheek recommendations; never use generic titles like 추천 아이템.",
    ].join(" ");

    const upstream = await fetch(`https://generativelanguage.googleapis.com/v1beta/models/${geminiModel}:generateContent?key=${encodeURIComponent(geminiApiKey)}`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        contents: [{
          parts: [
            { inline_data: { mime_type: mimeType, data: imageBase64 } },
            { text: prompt },
          ],
        }],
        generationConfig: { responseMimeType: "application/json" },
      }),
    });
    const data = await upstream.json();
    if (!upstream.ok) {
      return res.status(upstream.status).json({ error: "Gemini request failed.", upstream: data });
    }
    res.json(data);
  } catch (error) {
    res.status(500).json({ error: "Proxy analysis failed.", message: error?.message || String(error) });
  }
});

app.post("/v1/notifications/test", async (req, res) => {
  try {
    const { token, title = "OliveMe", body = "테스트 알림입니다." } = req.body || {};
    const projectId = process.env.FCM_PROJECT_ID;
    if (!projectId || !process.env.GOOGLE_APPLICATION_CREDENTIALS) {
      return res.status(503).json({ error: "FCM_PROJECT_ID and GOOGLE_APPLICATION_CREDENTIALS are required." });
    }
    if (!token) return res.status(400).json({ error: "FCM token is required." });

    const auth = new GoogleAuth({ scopes: ["https://www.googleapis.com/auth/firebase.messaging"] });
    const client = await auth.getClient();
    const accessToken = await client.getAccessToken();
    const upstream = await fetch(`https://fcm.googleapis.com/v1/projects/${encodeURIComponent(projectId)}/messages:send`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${accessToken.token}`,
      },
      body: JSON.stringify({
        message: {
          token,
          notification: { title, body },
          android: { priority: "NORMAL" },
        },
      }),
    });
    const data = await upstream.json();
    if (!upstream.ok) return res.status(upstream.status).json(data);
    res.json(data);
  } catch (error) {
    res.status(500).json({ error: "FCM test send failed.", message: error?.message || String(error) });
  }
});

app.get("/v1/products/search", async (req, res) => {
  const query = String(req.query.query || "").trim();
  const category = String(req.query.category || "").trim();
  const display = Math.min(Math.max(Number(req.query.display || 10), 1), 20);
  if (!query && !category) {
    return res.json({ source: "curated", items: curatedProducts.slice(0, display) });
  }

  const result = await searchNaverProducts(query || category, display, category);
  res.json(result);
});

app.post("/v1/products/recommendations", async (req, res) => {
  const body = req.body || {};
  const display = Math.min(Math.max(Number(body.display || 8), 1), 20);
  const keywords = Array.isArray(body.keywords) ? body.keywords.map((item) => String(item).trim()).filter(Boolean) : [];
  const category = String(body.category || "").trim();
  const type = String(body.type || "").trim();
  const season = String(body.season || "").trim();
  const queryCandidates = productQueryCandidates({ keywords, type, category });

  const productResult = isMakeupCategory(category)
    ? await mergedProductResult(queryCandidates, display, category)
    : await firstProductResult(queryCandidates, display, category);
  if (productResult.source !== "naver-shopping" || !Array.isArray(productResult.items) || productResult.items.length === 0) {
    return res.json({
      source: productResult.source,
      aiSummary: null,
      items: [],
    });
  }

  const summaryContext = {
    type,
    season,
    subtype: String(body.subtype || "").trim(),
    category,
    palette: Array.isArray(body.palette) ? body.palette : [],
    avoidColors: Array.isArray(body.avoidColors) ? body.avoidColors : [],
    keywords,
    items: productResult.items,
  };
  const aiSummary = await summarizeProducts(summaryContext) || fallbackProductSummary(summaryContext);

  res.json({
    source: "naver-shopping",
    aiSummary,
    total: productResult.total,
    items: productResult.items,
  });
});

async function firstProductResult(queries, display, category = "") {
  let lastResult = { source: "curated", total: 0, items: [] };
  for (const query of queries) {
    const result = await searchNaverProducts(query, display, category);
    lastResult = result;
    if (result.source === "naver-shopping" && Array.isArray(result.items) && result.items.length > 0) {
      return result;
    }
  }
  return lastResult;
}

async function mergedProductResult(queries, display, category = "") {
  const merged = [];
  const seen = new Set();
  let total = 0;
  let lastResult = { source: "curated", total: 0, items: [] };
  const needsMakeupCoverage = isMakeupCategory(category);
  for (const query of queries.slice(0, 10)) {
    const result = await searchNaverProducts(query, Math.min(Math.max(display, 4), 8), category);
    lastResult = result;
    if (result.source !== "naver-shopping" || !Array.isArray(result.items)) continue;
    total = Math.max(total, Number(result.total || 0));
    for (const item of result.items) {
      const key = `${String(item.link || "").trim()}|${String(item.title || "").trim()}`;
      if (!key.trim() || seen.has(key)) continue;
      seen.add(key);
      merged.push(item);
    }
    if (
      merged.length >= display
      && (!needsMakeupCoverage || hasMakeupFamilyCoverage(merged))
    ) {
      break;
    }
  }
  if (merged.length) {
    return { source: "naver-shopping", total: total || merged.length, items: diverseProductOrder(merged).slice(0, display) };
  }
  return lastResult;
}

function hasMakeupFamilyCoverage(items) {
  const families = new Set(items.map(classifyProductFamily));
  return ["lip", "eye", "base", "cheek"].every((family) => families.has(family));
}

function productQueryCandidates({ keywords = [], type = "", category = "" } = {}) {
  const safeCategory = String(category || "").trim();
  const intents = productSearchIntents(safeCategory);
  const candidates = [];
  if (isMakeupCategory(safeCategory)) {
    const palettePrefix = [type, "퍼스널컬러"].filter(Boolean).join(" ");
    ["립", "아이섀도우", "메이크업 베이스", "치크", "쿠션", "파운데이션", "컨실러"].forEach((intent) => {
      candidates.push([palettePrefix, intent].filter(Boolean).join(" "));
    });
  }
  for (const keyword of keywords.slice(0, 10)) {
    const safeKeyword = String(keyword || "").trim();
    if (!safeKeyword) continue;
    const hasIntent = intents.some((intent) => safeKeyword.includes(intent));
    candidates.push(hasIntent ? safeKeyword : [safeKeyword, intents[0] || safeCategory].filter(Boolean).join(" "));
    if (safeCategory) candidates.push([safeKeyword, safeCategory].filter(Boolean).join(" "));
  }
  candidates.push([type, intents[0] || safeCategory].filter(Boolean).join(" "));
  candidates.push([type, safeCategory].filter(Boolean).join(" "));
  candidates.push(safeCategory);
  return [...new Set(candidates.map((item) => item.trim()).filter(Boolean))];
}

function productSearchIntents(category) {
  if (isMakeupCategory(category)) {
    return ["립", "아이섀도우", "메이크업 베이스", "치크", "틴트", "블러셔", "섀도", "쿠션", "파운데이션", "컨실러", "메이크업"];
  }
  if (category.includes("의상") || category.includes("상의") || category.includes("아우터") || category.includes("하의")) {
    return ["의상", "블라우스", "니트", "셔츠", "재킷", "원피스"];
  }
  return [category].filter(Boolean);
}

function isMakeupCategory(category) {
  const text = String(category || "");
  return ["메이크업", "립", "틴트", "치크", "블러셔", "아이", "섀도", "베이스", "쿠션", "파운데이션"]
    .some((term) => text.includes(term));
}

function diverseProductOrder(items) {
  const buckets = {
    lip: [],
    eye: [],
    base: [],
    cheek: [],
    other: [],
  };
  for (const item of items) {
    buckets[classifyProductFamily(item)].push(item);
  }
  const ordered = [];
  const families = ["lip", "eye", "base", "cheek", "other"];
  let added = true;
  while (added) {
    added = false;
    for (const family of families) {
      const next = buckets[family].shift();
      if (next) {
        ordered.push(next);
        added = true;
      }
    }
  }
  return ordered;
}

function classifyProductFamily(item) {
  const text = productText(item);
  if (isRejectedMakeupProductText(text)) return "other";
  if (/립|틴트|립스틱|lip/i.test(text)) return "lip";
  if (/아이|섀도|섀도우|마스카라|아이라이너|shadow|eye/i.test(text)) return "eye";
  if (/쿠션|파운데이션|메이크업\s*베이스|톤업|프라이머|컨실러|비비크림|BB크림|CC크림|foundation|cushion|makeup\s*base|primer|concealer/i.test(text)) return "base";
  if (/치크|블러셔|블러쉬|blush|cheek/i.test(text)) return "cheek";
  return "other";
}

async function searchNaverProducts(query, display, category = "") {
  const safeQuery = String(query || category || "").trim();
  if (!safeQuery) {
    return { source: "curated", items: curatedProducts.slice(0, display) };
  }

  if (!naverClientId || !naverClientSecret) {
    return {
      source: "curated",
      items: curatedProducts
        .filter((item) => !category || item.category === category)
        .slice(0, display),
    };
  }

  try {
    const params = new URLSearchParams({
      query: safeQuery,
      display: String(display),
      start: "1",
      sort: "sim",
      exclude: "used:rental",
    });
    const upstream = await fetch(`https://openapi.naver.com/v1/search/shop.json?${params}`, {
      headers: {
        "X-Naver-Client-Id": naverClientId,
        "X-Naver-Client-Secret": naverClientSecret,
      },
    });
    const data = await upstream.json();
    if (!upstream.ok) {
      return { source: "curated", upstreamStatus: upstream.status, items: curatedProducts.slice(0, display) };
    }
    const rawItems = (data.items || []).map((item) => ({
      title: stripTags(item.title || ""),
      link: String(item.link || "").trim(),
      image: String(item.image || "").trim(),
      lprice: item.lprice || "",
      mallName: item.mallName || "",
      category1: item.category1 || "",
      category2: item.category2 || "",
      category3: item.category3 || "",
      category4: item.category4 || "",
    })).filter(isRenderableProduct);
    const items = rawItems.filter((item) => isRelevantProduct(item, category, safeQuery));
    return { source: "naver-shopping", total: data.total || items.length, items };
  } catch (error) {
    return { source: "curated", error: "product search fallback", items: curatedProducts.slice(0, display) };
  }
}

function isRenderableProduct(item) {
  return Boolean(
    String(item?.title || "").trim()
    && String(item?.link || "").trim()
    && String(item?.image || "").trim()
  );
}

function isRelevantProduct(item, category = "", query = "") {
  if (!isMakeupCategory(category) && !isMakeupCategory(query)) return true;
  const text = productText(item);
  if (isRejectedMakeupProductText(text)) return false;
  if (classifyProductFamily(item) !== "other") return true;
  return /메이크업|색조|화장품|립|틴트|섀도|섀도우|마스카라|아이라이너|쿠션|파운데이션|컨실러|프라이머|블러셔|치크/i.test(text);
}

function productText(item) {
  return [
    item?.title,
    item?.mallName,
    item?.category1,
    item?.category2,
    item?.category3,
    item?.category4,
  ].join(" ");
}

function isRejectedMakeupProductText(text) {
  return /네일|매니큐어|매니\s|젤네일|젤\s*네일|네일팁|네일아트|페디|큐티클|손톱|발톱|탑\s*코트|베이스\s*코트|베이스젤|글리터젤|드릴|네일\s*파츠|네일\s*스티커/i.test(String(text || ""));
}

async function summarizeProducts(context) {
  if (!geminiApiKey) return null;
  const productBrief = context.items.slice(0, 8).map((item, index) => ({
    rank: index + 1,
    title: item.title,
    price: item.lprice,
    mallName: item.mallName,
    category: [item.category1, item.category2, item.category3, item.category4].filter(Boolean).join(" > "),
  }));
  const prompt = [
    "You are OliveMe's Korean personal color commerce stylist.",
    "Given a personal color result and Naver Shopping products, return JSON only.",
    "Do not claim medical or biometric certainty. Recommend based on color harmony, category fit, and practical styling.",
    "Schema: {\"headline\": string, \"summary\": string, \"bullets\": [string, string, string], \"picks\": [{\"rank\": number, \"reason\": string}]}",
    "The picks must choose 2 or 3 products from the given Naver Shopping products by rank. Use only provided ranks. Reasons must explain color/category fit in Korean.",
    "If category is makeup, prefer a balanced set across lip, eye shadow, base/cushion, and cheek when those products exist. Do not pick three near-identical lip products unless the product list only contains lip products.",
    "Do not use vague phrases like 추천 아이템. Mention concrete product roles and color families such as 딥 베리 립, 모브 섀도, 핑크 베이스, or 쿨 로즈 치크 when relevant.",
    "Keep Korean copy polished, concise, and app-like.",
    JSON.stringify({
      type: context.type,
      season: context.season,
      subtype: context.subtype,
      category: context.category,
      palette: context.palette,
      avoidColors: context.avoidColors,
      keywords: context.keywords,
      products: productBrief,
    }),
  ].join("\n");

  try {
    const upstream = await fetch(`https://generativelanguage.googleapis.com/v1beta/models/${geminiModel}:generateContent?key=${encodeURIComponent(geminiApiKey)}`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        contents: [{ parts: [{ text: prompt }] }],
        generationConfig: { responseMimeType: "application/json" },
      }),
    });
    const data = await upstream.json();
    if (!upstream.ok) return null;
    const text = data?.candidates?.[0]?.content?.parts?.[0]?.text || "";
    const parsed = JSON.parse(cleanJsonText(text));
    return {
      headline: String(parsed.headline || "").trim(),
      summary: String(parsed.summary || "").trim(),
      bullets: Array.isArray(parsed.bullets) ? parsed.bullets.map((item) => String(item).trim()).filter(Boolean).slice(0, 3) : [],
      picks: normalizeProductPicks(parsed.picks, context.items),
      source: "gemini",
    };
  } catch (_error) {
    return null;
  }
}

function fallbackProductSummary(context) {
  const type = context.type || "현재 리포트";
  const category = context.category || "상품";
  const paletteNames = context.palette
    .slice(0, 3)
    .map((item) => item?.name)
    .filter(Boolean);
  const avoidNames = context.avoidColors
    .slice(0, 2)
    .map((item) => item?.name)
    .filter(Boolean);
  const headline = `${type}에 맞춘 ${category} 추천`;
  const paletteText = paletteNames.length ? `${paletteNames.join(", ")} 계열을 중심으로` : "리포트 팔레트를 기준으로";
  const isMakeup = isMakeupCategory(category);
  return {
    headline,
    summary: isMakeup
      ? `${paletteText} 립, 아이, 베이스, 치크가 한쪽으로 치우치지 않도록 골랐어요. 아래 후보는 이미지와 링크가 확인된 Naver 상품입니다.`
      : `${paletteText} 얼굴빛을 또렷하게 보완하는 구성을 먼저 골랐어요. 이미지와 링크가 확인된 Naver 상품만 보여드립니다.`,
    bullets: isMakeup
      ? [
          "립은 딥 베리나 버건디처럼 대비를 살리는 색을 우선합니다.",
          "아이와 베이스는 플럼 음영, 핑크 보정처럼 차가운 선명도를 해치지 않는 조합을 봅니다.",
          avoidNames.length ? `${avoidNames.join(", ")} 계열은 넓은 면적보다 작은 포인트로만 권장합니다.` : "피해야 할 색은 넓은 면적보다 작은 포인트로만 권장합니다.",
        ]
      : [
          `${withSubjectParticle(category)} 리포트의 온도감과 대비감을 해치지 않는 색을 우선합니다.`,
          avoidNames.length ? `${avoidNames.join(", ")} 계열은 넓은 면적보다 작은 포인트로만 권장합니다.` : "피해야 할 색은 넓은 면적보다 작은 포인트로만 권장합니다.",
          "상품 이미지를 보고 실제 색감이 리포트 팔레트와 가까운지 한 번 더 확인하세요.",
        ],
    picks: fallbackProductPicks(context),
    source: "local-fallback",
  };
}

function normalizeProductPicks(rawPicks, items) {
  if (!Array.isArray(rawPicks)) return fallbackProductPicks({ items });
  const maxRank = Array.isArray(items) ? items.length : 0;
  const seen = new Set();
  const picks = [];
  for (const pick of rawPicks) {
    const rank = Number(pick?.rank);
    if (!Number.isInteger(rank) || rank < 1 || rank > maxRank || seen.has(rank)) continue;
    const reason = String(pick?.reason || "").trim();
    picks.push({
      rank,
      reason: reason || "리포트 팔레트와 상품 색감이 자연스럽게 맞는 후보입니다.",
    });
    seen.add(rank);
    if (picks.length >= 3) break;
  }
  return picks.length ? picks : fallbackProductPicks({ items });
}

function fallbackProductPicks(context) {
  const paletteNames = (context.palette || [])
    .slice(0, 2)
    .map((item) => item?.name)
    .filter(Boolean);
  const paletteText = paletteNames.length ? `${paletteNames.join(", ")} 팔레트와` : "리포트 팔레트와";
  const category = context.category || "상품";
  return (context.items || [])
    .slice(0, 3)
    .map((item, index) => ({
      rank: index + 1,
      reason: isMakeupCategory(category)
        ? makeupFallbackPickReason(item, paletteText)
        : `${paletteText} 함께 보기 좋은 ${category} 후보입니다.`,
    }));
}

function makeupFallbackPickReason(item, paletteText) {
  switch (classifyProductFamily(item)) {
    case "lip":
      return `${paletteText} 맞춰 입술에 깊은 혈색을 더하는 립 후보입니다.`;
    case "eye":
      return `${paletteText} 어울리는 음영과 선명도를 만드는 아이 메이크업 후보입니다.`;
    case "base":
      return `${paletteText} 충돌하지 않게 피부 톤을 정리하는 베이스 후보입니다.`;
    case "cheek":
      return `${paletteText} 자연스럽게 이어지는 쿨 로즈 계열 치크 후보입니다.`;
    default:
      return `${paletteText} 함께 보기 좋은 메이크업 후보입니다.`;
  }
}

function withSubjectParticle(value) {
  const text = String(value || "추천").trim();
  const last = text.charCodeAt(text.length - 1);
  const hasFinalConsonant = last >= 0xac00 && last <= 0xd7a3 && ((last - 0xac00) % 28) > 0;
  return `${text}${hasFinalConsonant ? "은" : "는"}`;
}

function cleanJsonText(value) {
  const text = String(value || "").trim();
  if (!text.startsWith("```")) return text;
  return text.replace(/^```(?:json)?\s*/i, "").replace(/\s*```$/i, "").trim();
}

function stripTags(value) {
  return String(value).replace(/<[^>]+>/g, "").replace(/\s+/g, " ").trim();
}

function loadEnvFile(path) {
  if (!existsSync(path)) return;
  const lines = readFileSync(path, "utf8").split(/\r?\n/);
  for (const line of lines) {
    const trimmed = line.trim();
    if (!trimmed || trimmed.startsWith("#") || !trimmed.includes("=")) continue;
    const [rawKey, ...rawValueParts] = trimmed.split("=");
    const key = rawKey.trim();
    if (!key || process.env[key] !== undefined) continue;
    let value = rawValueParts.join("=").trim();
    if ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'"))) {
      value = value.slice(1, -1);
    }
    process.env[key] = value;
  }
}

const server = app.listen(port, () => {
  console.log(`OliveMe backend proxy listening on http://127.0.0.1:${port}`);
});

server.on("error", (error) => {
  if (error?.code === "EADDRINUSE") {
    console.error(`OliveMe backend proxy could not start because port ${port} is already in use.`);
  } else {
    console.error("OliveMe backend proxy failed to start.", error);
  }
  process.exitCode = 1;
});
