import assert from "node:assert/strict";
import { after, before, test } from "node:test";

process.env.NODE_ENV = "test";
process.env.PORT = "0";

let server;
let baseUrl;

before(async () => {
  const module = await import("../src/server.js");
  server = module.startServer(0);
  await new Promise((resolve, reject) => {
    server.once("listening", resolve);
    server.once("error", reject);
  });
  const address = server.address();
  baseUrl = `http://127.0.0.1:${address.port}`;
});

after(async () => {
  await new Promise((resolve, reject) => {
    server.close((error) => (error ? reject(error) : resolve()));
  });
});

test("quota fault returns quota-exhausted without products", async () => {
  const response = await recommendation("quota");

  assert.equal(response.source, "quota-exhausted");
  assert.equal(response.fallbackReason, "quota-exhausted");
  assert.equal(response.upstreamStatus, 429);
  assert.deepEqual(response.items, []);
});

test("empty upstream fault falls back to renderable curated products", async () => {
  const response = await recommendation("empty");

  assert.equal(response.source, "curated");
  assert.equal(response.fallbackReason, "curated-fallback");
  assert.ok(response.items.length > 0);
  assertRenderableProducts(response.items);
});

test("non-quota upstream error fault falls back to renderable curated products", async () => {
  const response = await recommendation("error");

  assert.equal(response.source, "curated");
  assert.equal(response.fallbackReason, "curated-fallback");
  assert.equal(response.upstreamStatus, 503);
  assert.ok(response.items.length > 0);
  assertRenderableProducts(response.items);
});

test("test env fault applies to plain Android-style recommendation requests", async () => {
  const previousFault = process.env.OLIVEME_PRODUCT_FAULT;
  process.env.OLIVEME_PRODUCT_FAULT = "quota";
  try {
    const response = await recommendation("");

    assert.equal(response.source, "quota-exhausted");
    assert.equal(response.fallbackReason, "quota-exhausted");
    assert.equal(response.upstreamStatus, 429);
    assert.deepEqual(response.items, []);
  } finally {
    if (previousFault === undefined) {
      delete process.env.OLIVEME_PRODUCT_FAULT;
    } else {
      process.env.OLIVEME_PRODUCT_FAULT = previousFault;
    }
  }
});

async function recommendation(faultMode) {
  const headers = { "Content-Type": "application/json" };
  if (faultMode) {
    headers["x-oliveme-product-fault"] = faultMode;
  }
  const response = await fetch(`${baseUrl}/v1/products/recommendations`, {
    method: "POST",
    headers,
    body: JSON.stringify({
      type: "겨울 쿨톤",
      season: "winter",
      subtype: "winter-cool",
      category: "메이크업",
      keywords: ["쿨톤 립"],
      palette: [{ hex: "#B85C7B", name: "쿨 로즈", role: "best" }],
      avoidColors: [],
      display: 8,
    }),
  });
  assert.equal(response.status, 200);
  return response.json();
}

function assertRenderableProducts(items) {
  for (const item of items) {
    assert.ok(String(item.title || "").trim(), "title should be present");
    assert.ok(String(item.link || "").trim(), "link should be present");
    assert.ok(String(item.image || "").trim(), "image should be present");
  }
}
