// Result screen — 4-page ViewPager with type/cloth/makeup/character
function ResultScreen({ result, onBack, onMap, onMyPage, onSave }) {
  const [page, setPage] = useState(0);
  const [saved, setSaved] = useState(false);
  const pages = [
    { id: 'type', label: '내 컬러' },
    { id: 'cloth', label: '의상' },
    { id: 'makeup', label: '메이크업' },
    { id: 'traits', label: '특징' },
  ];

  const handleSave = () => {
    setSaved(true);
    onSave();
    setTimeout(() => setSaved(false), 2400);
  };

  return (
    <div data-screen-label="04 Result" style={{
      height: '100%', display: 'flex', flexDirection: 'column',
      background: 'var(--bg)', position: 'relative', overflow: 'hidden',
    }}>
      <AppBar
        leading={<IconButton onClick={onBack}><IconBack/></IconButton>}
        title="진단 결과"
        subtitle={result.dateLabel}
        trailing={<>
          <IconButton><IconShare size={20}/></IconButton>
          <IconButton onClick={handleSave}>
            <IconHeart size={20} fill={saved ? 'var(--primary-deep)' : 'none'} stroke={saved ? 'var(--primary-deep)' : 'currentColor'}/>
          </IconButton>
        </>}
      />

      {/* Tab pager indicator */}
      <div style={{
        display: 'flex', gap: 6, padding: '4px 20px 12px',
      }}>
        {pages.map((p, i) => (
          <button key={p.id} onClick={() => setPage(i)} className="tap" style={{
            flex: 1, padding: '10px 0', borderRadius: 100,
            background: page === i ? 'var(--text)' : 'transparent',
            color: page === i ? '#fff' : 'var(--text-mid)',
            fontSize: 12, fontWeight: 600,
            border: page === i ? 'none' : '1px solid var(--line)',
          }}>
            {p.label}
          </button>
        ))}
      </div>

      {/* Page count indicator */}
      <div style={{ display: 'flex', justifyContent: 'center', gap: 4, padding: '0 0 8px' }}>
        {pages.map((_, i) => (
          <div key={i} className={`dot ${i === page ? 'active' : ''}`}/>
        ))}
      </div>

      {/* Pager content */}
      <div className="phone-scroll" key={page} style={{
        flex: 1, overflow: 'auto', padding: '0 20px 88px',
      }}>
        <div className="fade-up">
          {page === 0 && <PageType result={result} />}
          {page === 1 && <PageCloth result={result} />}
          {page === 2 && <PageMakeup result={result} />}
          {page === 3 && <PageTraits result={result} />}
        </div>
      </div>

      {/* Bottom actions — slim pill bar */}
      <div style={{
        position: 'absolute', bottom: 0, left: 0, right: 0,
        padding: '12px 16px 16px',
        background: 'linear-gradient(180deg, rgba(251,246,242,0) 0%, rgba(251,246,242,0.95) 35%, rgba(251,246,242,1) 100%)',
        display: 'flex', gap: 8,
      }}>
        <button onClick={onMap} className="tap" style={{
          flex: 1, height: 44,
          background: '#fff', color: 'var(--text)',
          border: '1px solid var(--line)', borderRadius: 100,
          fontSize: 13, fontWeight: 600,
          display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 6,
          boxShadow: '0 2px 6px rgba(91, 60, 76, 0.04)',
        }}>
          <IconMap size={15}/> 근처 매장
        </button>
        <button onClick={onMyPage} className="tap" style={{
          flex: 1.2, height: 44,
          background: 'linear-gradient(135deg, var(--primary) 0%, var(--primary-deep) 100%)',
          color: '#fff', border: 'none', borderRadius: 100,
          fontSize: 13, fontWeight: 600,
          display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 6,
          boxShadow: '0 4px 12px rgba(216, 126, 146, 0.32)',
          letterSpacing: '-0.01em',
        }}>
          <IconBookmark size={15}/> 마이페이지 저장
        </button>
      </div>

      {saved && (
        <div className="fade-up" style={{
          position: 'absolute', bottom: 76, left: 20, right: 20,
          padding: '12px 16px', borderRadius: 12,
          background: 'rgba(61, 49, 55, 0.95)',
          color: '#fff', fontSize: 13, fontWeight: 500,
          display: 'flex', alignItems: 'center', gap: 8,
          boxShadow: 'var(--shadow-md)',
        }}>
          <IconCheck size={16} stroke="#A8E6CF"/>
          마이페이지에 저장되었어요
        </div>
      )}
    </div>
  );
}

// ─── Page 1: Type & main palette ───
function PageType({ result }) {
  return (
    <>
      {/* Hero card */}
      <div style={{
        borderRadius: 24, overflow: 'hidden', position: 'relative',
        background: `linear-gradient(160deg, ${result.heroBg[0]} 0%, ${result.heroBg[1]} 100%)`,
        color: '#fff', padding: '28px 24px 24px',
        boxShadow: '0 16px 40px rgba(91, 60, 76, 0.18)',
      }}>
        {/* deco */}
        <div style={{
          position: 'absolute', top: -40, right: -40, width: 180, height: 180,
          borderRadius: '50%', background: 'rgba(255,255,255,0.08)',
        }}/>
        <div style={{
          position: 'absolute', bottom: -60, left: -20, width: 140, height: 140,
          borderRadius: '50%', background: 'rgba(255,255,255,0.06)',
        }}/>

        <div style={{ position: 'relative' }}>
          <Pill bg="rgba(255,255,255,0.2)" color="#fff">
            <IconSparkle size={11}/> AI 진단 결과
          </Pill>
          <div style={{ marginTop: 16, fontSize: 11, opacity: 0.85, letterSpacing: '0.18em', fontWeight: 500 }}>
            {result.englishLabel}
          </div>
          <div className="serif" style={{
            marginTop: 6, fontSize: 36, fontWeight: 500, lineHeight: 1.1,
            letterSpacing: '-0.02em', fontStyle: 'italic',
          }}>{result.type}</div>
          <div style={{ marginTop: 14, fontSize: 13, lineHeight: 1.7, opacity: 0.92, maxWidth: 280 }}>
            {result.description}
          </div>

          {/* Match score */}
          <div style={{
            marginTop: 20, display: 'flex', alignItems: 'baseline', gap: 14,
            padding: '14px 0 0', borderTop: '1px solid rgba(255,255,255,0.2)',
          }}>
            <div>
              <div style={{ fontSize: 10, opacity: 0.7, fontWeight: 500, letterSpacing: '0.1em' }}>매칭 정확도</div>
              <div className="serif" style={{ fontSize: 30, fontWeight: 500, fontStyle: 'italic' }}>
                {result.matchScore}<span style={{ fontSize: 18, opacity: 0.7 }}>%</span>
              </div>
            </div>
            <div style={{ flex: 1, height: 4, background: 'rgba(255,255,255,0.2)', borderRadius: 2, overflow: 'hidden' }}>
              <div style={{ width: `${result.matchScore}%`, height: '100%', background: '#fff', borderRadius: 2 }}/>
            </div>
          </div>
        </div>
      </div>

      {/* Main palette */}
      <div style={{ marginTop: 24 }}>
        <SectionHeader title="베이스 컬러 팔레트" sub="당신을 빛나게 하는 6가지 색"/>
        <div style={{
          background: 'var(--card)', borderRadius: 20, padding: 20,
          border: '1px solid var(--line-soft)', boxShadow: 'var(--shadow-sm)',
        }}>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 16 }}>
            {result.palette.map((c, i) => (
              <Swatch key={i} color={c.hex} name={c.name} hex={c.hex} size={64} showCode/>
            ))}
          </div>
        </div>
      </div>

      {/* Avoid colors */}
      <div style={{ marginTop: 24 }}>
        <SectionHeader title="피하면 좋아요" sub="얼굴을 어둡게 보이게 할 수 있어요"/>
        <div style={{
          background: 'var(--card)', borderRadius: 20, padding: 20,
          border: '1px solid var(--line-soft)',
          display: 'flex', gap: 12,
        }}>
          {result.avoidColors.map((c, i) => (
            <div key={i} style={{
              flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 8,
            }}>
              <div style={{
                width: 48, height: 48, borderRadius: '50%', background: c.hex,
                position: 'relative', opacity: 0.85,
              }}>
                <div style={{
                  position: 'absolute', inset: 0, borderRadius: '50%',
                  background: 'linear-gradient(135deg, transparent 47%, #fff 47%, #fff 53%, transparent 53%)',
                  opacity: 0.6,
                }}/>
              </div>
              <div style={{ fontSize: 11, fontWeight: 600, color: 'var(--text-mid)' }}>{c.name}</div>
            </div>
          ))}
        </div>
      </div>

      {/* Type comparison */}
      <div style={{ marginTop: 24 }}>
        <SectionHeader title="4계절 16타입 분포" sub="당신의 위치"/>
        <div style={{
          background: 'var(--card)', borderRadius: 20, padding: 20,
          border: '1px solid var(--line-soft)',
        }}>
          <SeasonChart current="winter-cool" />
        </div>
      </div>
    </>
  );
}

function SeasonChart({ current }) {
  const seasons = [
    { id: 'spring-warm', label: '봄 웜', color: '#FFB683', x: 25, y: 25 },
    { id: 'summer-cool', label: '여름 쿨', color: '#C9B8E8', x: 75, y: 25 },
    { id: 'autumn-warm', label: '가을 웜', color: '#C28572', x: 25, y: 75 },
    { id: 'winter-cool', label: '겨울 쿨', color: '#722F37', x: 75, y: 75 },
  ];
  return (
    <div style={{ position: 'relative', height: 180 }}>
      {/* Axes */}
      <div style={{ position: 'absolute', top: '50%', left: 0, right: 0, height: 1, background: 'var(--line)' }}/>
      <div style={{ position: 'absolute', left: '50%', top: 0, bottom: 0, width: 1, background: 'var(--line)' }}/>
      <div style={{ position: 'absolute', top: -2, left: '50%', transform: 'translateX(-50%)', fontSize: 9, color: 'var(--text-dim)', fontWeight: 600 }}>COOL</div>
      <div style={{ position: 'absolute', bottom: -2, left: '50%', transform: 'translateX(-50%)', fontSize: 9, color: 'var(--text-dim)', fontWeight: 600 }}>WARM</div>
      <div style={{ position: 'absolute', left: -2, top: '50%', transform: 'translateY(-50%) rotate(-90deg)', fontSize: 9, color: 'var(--text-dim)', fontWeight: 600 }}>LIGHT</div>
      <div style={{ position: 'absolute', right: -2, top: '50%', transform: 'translateY(-50%) rotate(90deg)', fontSize: 9, color: 'var(--text-dim)', fontWeight: 600 }}>DEEP</div>

      {seasons.map(s => {
        const active = s.id === current;
        return (
          <div key={s.id} style={{
            position: 'absolute', left: `${s.x}%`, top: `${s.y}%`,
            transform: 'translate(-50%, -50%)',
            display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 4,
          }}>
            <div style={{
              width: active ? 40 : 22, height: active ? 40 : 22,
              borderRadius: '50%', background: s.color,
              boxShadow: active ? `0 0 0 4px rgba(114,47,55,0.18), 0 6px 16px rgba(0,0,0,0.15)` : '0 2px 6px rgba(0,0,0,0.1)',
              border: active ? '2px solid #fff' : 'none',
            }}/>
            <div style={{
              fontSize: 10, fontWeight: active ? 700 : 500,
              color: active ? 'var(--text)' : 'var(--text-mid)',
            }}>{s.label}</div>
          </div>
        );
      })}
    </div>
  );
}

// ─── Page 2: Cloth recommendations ───
function PageCloth({ result }) {
  const [filter, setFilter] = useState('전체');
  const cats = ['전체', '톱', '아우터', '드레스'];
  return (
    <>
      <div style={{ padding: '8px 0 16px' }}>
        <div className="serif" style={{
          fontSize: 22, fontWeight: 500, color: 'var(--text)',
          letterSpacing: '-0.01em', lineHeight: 1.35,
        }}>
          이런 옷이<br/><span style={{ fontStyle: 'italic', color: 'var(--w-burgundy)' }}>당신을 빛나게 해요</span>
        </div>
        <div style={{ marginTop: 8, fontSize: 12, color: 'var(--text-mid)' }}>
          쿨톤 깊이감을 살리는 컬러 추천 12종
        </div>
      </div>

      <div style={{ display: 'flex', gap: 8, marginBottom: 14, overflowX: 'auto' }} className="phone-scroll">
        {cats.map(c => (
          <button key={c} onClick={() => setFilter(c)} className="tap" style={{
            padding: '7px 14px', borderRadius: 100, whiteSpace: 'nowrap',
            background: filter === c ? 'var(--text)' : 'var(--card)',
            color: filter === c ? '#fff' : 'var(--text-mid)',
            fontSize: 12, fontWeight: 600,
            border: filter === c ? 'none' : '1px solid var(--line)',
          }}>{c}</button>
        ))}
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
        {result.clothes.map((c, i) => (
          <ProductCard key={i} item={c} kind="garment"/>
        ))}
      </div>
    </>
  );
}

// ─── Page 3: Makeup recommendations ───
function PageMakeup({ result }) {
  return (
    <>
      <div style={{ padding: '8px 0 16px' }}>
        <div className="serif" style={{
          fontSize: 22, fontWeight: 500, color: 'var(--text)',
          letterSpacing: '-0.01em', lineHeight: 1.35,
        }}>
          나에게 어울리는<br/><span style={{ fontStyle: 'italic', color: 'var(--w-fuchsia)' }}>메이크업 컬러</span>
        </div>
        <div style={{ marginTop: 8, fontSize: 12, color: 'var(--text-mid)' }}>
          입체감을 살리는 베리·플럼 계열 추천
        </div>
      </div>

      {/* Categories */}
      {result.makeup.map((cat, i) => (
        <div key={i} style={{ marginBottom: 20 }}>
          <div style={{
            display: 'flex', alignItems: 'center', gap: 8,
            marginBottom: 10,
          }}>
            <div style={{
              width: 28, height: 28, borderRadius: 8,
              background: 'var(--secondary-soft)', color: 'var(--text)',
              display: 'flex', alignItems: 'center', justifyContent: 'center',
            }}>{cat.icon === 'lip' ? <IconLipstick size={14}/> : cat.icon === 'eye' ? <IconSparkle size={14}/> : <IconPalette size={14}/>}</div>
            <div style={{ fontSize: 14, fontWeight: 700, color: 'var(--text)' }}>{cat.category}</div>
            <div style={{ flex: 1, height: 1, background: 'var(--line-soft)' }}/>
            <div style={{ fontSize: 11, color: 'var(--text-dim)' }}>{cat.items.length}개</div>
          </div>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: 10 }}>
            {cat.items.map((it, j) => (
              <ProductCard key={j} item={it} kind={cat.kind} compact/>
            ))}
          </div>
        </div>
      ))}
    </>
  );
}

function ProductCard({ item, kind, compact }) {
  return (
    <div style={{
      background: 'var(--card)', borderRadius: 14, overflow: 'hidden',
      border: '1px solid var(--line-soft)',
    }} className="tap">
      <div style={{
        background: compact ? '#FAF4F0' : '#F8F0EC',
        aspectRatio: compact ? '1' : '4/5',
        padding: compact ? 8 : 16,
      }}>
        <PlaceholderTile kind={kind} color={item.hex}/>
      </div>
      <div style={{ padding: compact ? '8px 10px 10px' : '10px 12px 12px' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
          <div style={{
            width: compact ? 10 : 12, height: compact ? 10 : 12, borderRadius: '50%',
            background: item.hex, border: '1px solid rgba(0,0,0,0.05)',
            flexShrink: 0,
          }}/>
          <div style={{
            fontSize: compact ? 11 : 12, fontWeight: 600, color: 'var(--text)',
            whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis',
          }}>{item.name}</div>
        </div>
        {!compact && (
          <div style={{ marginTop: 4, fontSize: 10, color: 'var(--text-dim)', fontFamily: 'JetBrains Mono, monospace' }}>
            {item.hex}
          </div>
        )}
      </div>
    </div>
  );
}

// ─── Page 4: Character traits ───
function PageTraits({ result }) {
  return (
    <>
      <div style={{ padding: '8px 0 20px' }}>
        <div className="serif" style={{
          fontSize: 22, fontWeight: 500, color: 'var(--text)',
          letterSpacing: '-0.01em', lineHeight: 1.35,
        }}>
          당신의 컬러는<br/><span style={{ fontStyle: 'italic' }}>이런 특징이 있어요</span>
        </div>
      </div>

      {/* Big quote-style trait */}
      <div style={{
        padding: 22, borderRadius: 20,
        background: 'linear-gradient(160deg, #FCE2E8 0%, #ECE4F8 100%)',
        position: 'relative', overflow: 'hidden',
      }}>
        <div className="serif" style={{
          fontSize: 90, lineHeight: 0.5, color: 'rgba(114,47,55,0.2)',
          fontStyle: 'italic', position: 'absolute', top: 24, left: 12,
        }}>&ldquo;</div>
        <div className="serif" style={{
          position: 'relative', fontSize: 16, fontWeight: 500, color: 'var(--text)',
          lineHeight: 1.6, letterSpacing: '-0.01em', paddingLeft: 8,
        }}>
          {result.signature}
        </div>
      </div>

      {/* Trait cards */}
      <div style={{ marginTop: 20, display: 'flex', flexDirection: 'column', gap: 10 }}>
        {result.traits.map((t, i) => (
          <div key={i} style={{
            background: 'var(--card)', borderRadius: 14, padding: 16,
            border: '1px solid var(--line-soft)',
            display: 'flex', gap: 14,
          }}>
            <div style={{
              width: 36, height: 36, borderRadius: 10,
              background: t.bg, color: t.color,
              display: 'flex', alignItems: 'center', justifyContent: 'center',
              flexShrink: 0, fontSize: 16, fontWeight: 700,
            }}>{i + 1}</div>
            <div style={{ flex: 1 }}>
              <div style={{ fontSize: 13, fontWeight: 700, color: 'var(--text)' }}>{t.title}</div>
              <div style={{ marginTop: 4, fontSize: 12, color: 'var(--text-mid)', lineHeight: 1.6 }}>
                {t.body}
              </div>
            </div>
          </div>
        ))}
      </div>

      {/* Style keywords */}
      <div style={{ marginTop: 24 }}>
        <SectionHeader title="스타일 키워드"/>
        <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8 }}>
          {result.keywords.map((k, i) => (
            <div key={i} style={{
              padding: '8px 14px', borderRadius: 100,
              background: i % 2 === 0 ? 'var(--w-burgundy)' : 'var(--text)',
              color: '#fff', fontSize: 12, fontWeight: 600,
              letterSpacing: '-0.01em',
            }}>#{k}</div>
          ))}
        </div>
      </div>

      {/* Compatible celebrities (placeholder note) */}
      <div style={{
        marginTop: 24, padding: 16, borderRadius: 14,
        background: 'var(--accent-soft)',
        display: 'flex', alignItems: 'flex-start', gap: 12,
      }}>
        <IconSparkle size={18} stroke="var(--accent)"/>
        <div style={{ flex: 1 }}>
          <div style={{ fontSize: 12, fontWeight: 700, color: 'var(--text)' }}>비슷한 톤의 셀럽</div>
          <div style={{ marginTop: 4, fontSize: 11, color: 'var(--text-mid)', lineHeight: 1.5 }}>
            깊고 차가운 톤을 가진 인물군과 매칭됩니다 · 정확한 비교는 마이페이지에서 확인하세요
          </div>
        </div>
      </div>
    </>
  );
}

window.ResultScreen = ResultScreen;
