// MyPage screen — latest report (magazine style) + history list
function MyPageScreen({ user, latest, history, onBack, onNavigate, onOpenResult }) {
  const [tab, setTab] = useState('report'); // 'report' | 'history' | 'stores'
  return (
    <div data-screen-label="06 MyPage" style={{
      height: '100%', display: 'flex', flexDirection: 'column',
      background: 'var(--bg)', position: 'relative', overflow: 'hidden',
    }}>
      <AppBar
        leading={<IconButton onClick={onBack}><IconBack/></IconButton>}
        title="마이페이지"
        trailing={<IconButton><IconSettings size={20}/></IconButton>}
      />

      <div className="phone-scroll" style={{ flex: 1, overflow: 'auto', padding: '0 0 24px' }}>
        {/* Profile header */}
        <div style={{ padding: '4px 20px 16px', display: 'flex', alignItems: 'center', gap: 14 }}>
          <Avatar name={user.name} size={56}/>
          <div style={{ flex: 1 }}>
            <div style={{ fontSize: 16, fontWeight: 700, color: 'var(--text)' }}>{user.name}</div>
            <div style={{ fontSize: 11, color: 'var(--text-mid)', marginTop: 2 }}>{user.email}</div>
            <div style={{ marginTop: 6, display: 'flex', gap: 6 }}>
              <Pill color="var(--w-burgundy)" bg="rgba(114,47,55,0.1)" dot>겨울 쿨톤</Pill>
            </div>
          </div>
          <button className="tap" style={{
            padding: '7px 12px', borderRadius: 100,
            background: 'var(--card)', border: '1px solid var(--line)',
            fontSize: 11, fontWeight: 600, color: 'var(--text)',
          }}>편집</button>
        </div>

        {/* Stats row */}
        <div style={{ padding: '0 20px 16px', display: 'flex', gap: 8 }}>
          <StatCell num={history.length + 1} label="진단 횟수"/>
          <StatCell num="92%" label="평균 정확도"/>
          <StatCell num="3" label="즐겨찾기"/>
        </div>

        {/* Tabs */}
        <div style={{
          padding: '0 20px', display: 'flex', gap: 4,
          borderBottom: '1px solid var(--line-soft)',
        }}>
          {[
            { id: 'report', label: '내 리포트' },
            { id: 'history', label: '진단 이력' },
            { id: 'stores', label: '즐겨찾기 매장' },
          ].map(t => (
            <button key={t.id} onClick={() => setTab(t.id)} className="tap" style={{
              padding: '12px 4px', flex: 1,
              fontSize: 13, fontWeight: tab === t.id ? 700 : 500,
              color: tab === t.id ? 'var(--text)' : 'var(--text-dim)',
              borderBottom: tab === t.id ? '2px solid var(--text)' : '2px solid transparent',
              marginBottom: -1,
            }}>{t.label}</button>
          ))}
        </div>

        <div key={tab} className="fade-up" style={{ padding: '16px 20px 0' }}>
          {tab === 'report' && <ReportTab latest={latest} onNavigate={onNavigate}/>}
          {tab === 'history' && <HistoryTab history={history} latest={latest} onOpen={onOpenResult}/>}
          {tab === 'stores' && <StoresTab onNavigate={onNavigate}/>}
        </div>
      </div>
    </div>
  );
}

function StatCell({ num, label }) {
  return (
    <div style={{
      flex: 1, padding: '12px 8px', borderRadius: 12,
      background: 'var(--card)', border: '1px solid var(--line-soft)',
      textAlign: 'center',
    }}>
      <div className="serif" style={{ fontSize: 22, fontWeight: 600, color: 'var(--text)', fontStyle: 'italic', lineHeight: 1 }}>
        {num}
      </div>
      <div style={{ fontSize: 10, color: 'var(--text-dim)', marginTop: 4, fontWeight: 500 }}>
        {label}
      </div>
    </div>
  );
}

// ─── Report tab: magazine-style ───
function ReportTab({ latest, onNavigate }) {
  return (
    <>
      {/* Magazine cover */}
      <div style={{
        borderRadius: 22, overflow: 'hidden',
        background: `linear-gradient(160deg, ${latest.heroBg[0]} 0%, ${latest.heroBg[1]} 100%)`,
        color: '#fff', padding: '24px 22px 22px',
        position: 'relative',
        boxShadow: '0 16px 36px rgba(91, 60, 76, 0.18)',
      }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', fontSize: 10, opacity: 0.7, letterSpacing: '0.2em', fontWeight: 500 }}>
          <span>VOL. 03 · MY COLOR REPORT</span>
          <span>{latest.dateLabel}</span>
        </div>

        <div style={{ marginTop: 36, marginBottom: 36 }}>
          <div style={{ fontSize: 10, opacity: 0.7, letterSpacing: '0.2em', fontWeight: 600 }}>
            {latest.englishLabel}
          </div>
          <div className="serif" style={{
            marginTop: 4, fontSize: 38, fontWeight: 500, lineHeight: 1, fontStyle: 'italic',
            letterSpacing: '-0.02em',
          }}>
            {latest.type}
          </div>
        </div>

        {/* color strip */}
        <div style={{ display: 'flex', height: 6, borderRadius: 3, overflow: 'hidden' }}>
          {latest.palette.map((c, i) => (
            <div key={i} style={{ flex: 1, background: c.hex }}/>
          ))}
        </div>

        <div style={{
          marginTop: 14, display: 'flex', alignItems: 'baseline', justifyContent: 'space-between',
          fontSize: 11, opacity: 0.85,
        }}>
          <span>매칭 정확도</span>
          <span className="serif" style={{ fontSize: 18, fontStyle: 'italic', fontWeight: 500 }}>
            {latest.matchScore}<span style={{ fontSize: 11, opacity: 0.7 }}>%</span>
          </span>
        </div>
      </div>

      {/* Download row */}
      <div style={{
        marginTop: 12, display: 'flex', gap: 8,
      }}>
        <button className="tap" style={{
          flex: 1, padding: '12px',
          background: 'var(--card)', border: '1px solid var(--line)',
          borderRadius: 12, fontSize: 12, fontWeight: 600,
          color: 'var(--text)',
          display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 6,
        }}><IconDownload size={14}/> 리포트 저장</button>
        <button className="tap" style={{
          flex: 1, padding: '12px',
          background: 'var(--card)', border: '1px solid var(--line)',
          borderRadius: 12, fontSize: 12, fontWeight: 600,
          color: 'var(--text)',
          display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 6,
        }}><IconShare size={14}/> 공유</button>
      </div>

      {/* Highlight: signature line */}
      <div style={{
        marginTop: 24, padding: 20,
        background: '#fff', borderRadius: 16,
        border: '1px solid var(--line-soft)',
      }}>
        <div style={{ fontSize: 10, color: 'var(--text-dim)', letterSpacing: '0.18em', fontWeight: 600 }}>
          SIGNATURE
        </div>
        <div className="serif" style={{
          marginTop: 8, fontSize: 16, fontWeight: 500, color: 'var(--text)',
          lineHeight: 1.6, letterSpacing: '-0.01em',
        }}>{latest.signature}</div>
      </div>

      {/* Palette grid */}
      <div style={{ marginTop: 20 }}>
        <SectionHeader title="베스트 컬러 6"/>
        <div style={{
          background: 'var(--card)', borderRadius: 18, padding: 18,
          border: '1px solid var(--line-soft)',
          display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 14,
        }}>
          {latest.palette.map((c, i) => (
            <div key={i} style={{ textAlign: 'center' }}>
              <div className="swatch-ring" style={{
                width: 56, height: 56, margin: '0 auto', background: c.hex, borderRadius: '50%',
              }}/>
              <div style={{ marginTop: 6, fontSize: 11, fontWeight: 600, color: 'var(--text)' }}>{c.name}</div>
              <div style={{ fontSize: 9, color: 'var(--text-dim)', fontFamily: 'JetBrains Mono, monospace' }}>{c.hex}</div>
            </div>
          ))}
        </div>
      </div>

      {/* Recommended makeup quick view */}
      <div style={{ marginTop: 20 }}>
        <SectionHeader title="추천 메이크업"/>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: 8 }}>
          {latest.makeup[0].items.slice(0, 4).map((it, i) => (
            <div key={i} style={{
              background: 'var(--card)', borderRadius: 10, padding: 8,
              border: '1px solid var(--line-soft)',
            }}>
              <div style={{ aspectRatio: '1', background: '#FAF4F0', borderRadius: 6, overflow: 'hidden' }}>
                <PlaceholderTile kind="lipstick" color={it.hex}/>
              </div>
              <div style={{ marginTop: 6, fontSize: 10, fontWeight: 600, color: 'var(--text)', textAlign: 'center', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                {it.name}
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Next CTA */}
      <button onClick={() => onNavigate('diagnosis')} className="tap" style={{
        marginTop: 24, width: '100%', padding: 16,
        background: 'var(--text)', color: '#fff',
        borderRadius: 14, fontSize: 14, fontWeight: 700,
        display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 8,
      }}>
        <IconRefresh size={16}/> 다시 진단하기
      </button>
    </>
  );
}

// ─── History tab ───
function HistoryTab({ history, latest, onOpen }) {
  const all = [
    { id: 'cur', date: latest.dateLabel, type: latest.type, palette: latest.palette.map(c => c.hex), label: 'NEW' },
    ...history,
  ];
  return (
    <>
      <div style={{ fontSize: 11, color: 'var(--text-dim)', fontWeight: 500, marginBottom: 10 }}>
        총 {all.length}건 · 최신순
      </div>
      <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
        {all.map((h, i) => (
          <div key={h.id} onClick={i === 0 ? onOpen : null} className={i === 0 ? 'tap' : ''} style={{
            background: 'var(--card)', borderRadius: 14, padding: 14,
            border: '1px solid var(--line-soft)',
            display: 'flex', gap: 12, alignItems: 'center',
          }}>
            <div style={{
              width: 60, height: 76, borderRadius: 10, flexShrink: 0,
              background: `linear-gradient(135deg, ${h.palette[0]}, ${h.palette[1]})`,
              position: 'relative', overflow: 'hidden',
            }}>
              {h.label && (
                <div style={{
                  position: 'absolute', top: 6, left: 6,
                  background: 'rgba(255,255,255,0.95)', borderRadius: 4,
                  padding: '1px 5px', fontSize: 8, fontWeight: 700,
                  color: 'var(--primary-deep)',
                }}>{h.label}</div>
              )}
            </div>
            <div style={{ flex: 1, minWidth: 0 }}>
              <div style={{ fontSize: 10, color: 'var(--text-dim)', fontWeight: 500 }}>{h.date}</div>
              <div className="serif" style={{
                marginTop: 3, fontSize: 16, fontWeight: 600, color: 'var(--text)',
                letterSpacing: '-0.01em',
              }}>{h.type}</div>
              <div style={{ marginTop: 8, display: 'flex', gap: 4 }}>
                {h.palette.slice(0, 6).map((c, j) => (
                  <div key={j} style={{
                    width: 14, height: 14, borderRadius: '50%', background: c,
                    border: '1.5px solid #fff',
                    boxShadow: '0 1px 2px rgba(0,0,0,0.1)',
                  }}/>
                ))}
              </div>
            </div>
            <IconChevron size={14} stroke="var(--text-dim)"/>
          </div>
        ))}
      </div>
    </>
  );
}

// ─── Stores tab ───
function StoresTab({ onNavigate }) {
  const favorites = [
    { name: '뷰티샵 부산대점', addr: '부산대학로 63', distance: '180m', brand: '#7BA068', initials: 'BS' },
    { name: '코스메 장전점', addr: '장전로 14', distance: '680m', brand: '#7B5BC9', initials: 'CO' },
    { name: '컬러랩 NC점', addr: '금정로 250', distance: '420m', brand: '#D87E92', initials: 'CL' },
  ];
  return (
    <>
      <div style={{ fontSize: 11, color: 'var(--text-dim)', fontWeight: 500, marginBottom: 10 }}>
        총 {favorites.length}곳 저장됨
      </div>
      <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
        {favorites.map((s, i) => (
          <div key={i} onClick={() => onNavigate('map')} className="tap" style={{
            background: 'var(--card)', borderRadius: 14, padding: 14,
            border: '1px solid var(--line-soft)',
            display: 'flex', gap: 12, alignItems: 'center',
          }}>
            <div style={{
              width: 50, height: 50, borderRadius: 12, background: s.brand,
              display: 'flex', alignItems: 'center', justifyContent: 'center',
              color: '#fff', fontFamily: 'Cormorant Garamond, serif', fontStyle: 'italic',
              fontSize: 16, fontWeight: 600,
              flexShrink: 0,
            }}>{s.initials}</div>
            <div style={{ flex: 1 }}>
              <div style={{ fontSize: 13, fontWeight: 700, color: 'var(--text)' }}>{s.name}</div>
              <div style={{ fontSize: 11, color: 'var(--text-mid)', marginTop: 2 }}>
                {s.addr} · {s.distance}
              </div>
            </div>
            <IconHeart size={16} fill="var(--primary-deep)" stroke="var(--primary-deep)"/>
          </div>
        ))}
      </div>

      <button className="tap" style={{
        marginTop: 16, width: '100%', padding: 14,
        background: 'transparent', color: 'var(--text-mid)',
        border: '1.5px dashed var(--line)', borderRadius: 12,
        fontSize: 13, fontWeight: 600,
        display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 6,
      }}>
        <IconPlus size={14}/> 매장 추가하기
      </button>
    </>
  );
}

window.MyPageScreen = MyPageScreen;
