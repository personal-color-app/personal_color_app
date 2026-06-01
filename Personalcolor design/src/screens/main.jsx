// Main / Home — greeting, diagnose CTA, recent diagnosis preview, drawer
function MainScreen({ user, recentDiagnosis, onNavigate, onLogout }) {
  const [drawerOpen, setDrawerOpen] = useState(false);

  return (
    <div data-screen-label="02 Main" style={{
      height: '100%', position: 'relative', overflow: 'hidden',
      background: 'var(--bg)', display: 'flex', flexDirection: 'column',
    }}>
      <AppBar
        leading={<IconButton onClick={() => setDrawerOpen(true)}><IconMenu/></IconButton>}
        title={<div style={{ display: 'flex', justifyContent: 'center' }}><OliveMeLogo size={18} variant="inline" /></div>}
        trailing={<IconButton><IconBell/></IconButton>}
      />

      <div className="phone-scroll" style={{ flex: 1, overflow: 'auto', padding: '0 20px 24px' }}>
        {/* Greeting */}
        <div style={{ padding: '8px 0 20px' }}>
          <div style={{ fontSize: 13, color: 'var(--text-mid)', fontWeight: 500 }}>
            안녕하세요, <span style={{ color: 'var(--primary-deep)' }}>{user.name}</span>님
          </div>
          <div className="serif" style={{
            marginTop: 6, fontSize: 24, fontWeight: 500, color: 'var(--text)',
            lineHeight: 1.3, letterSpacing: '-0.01em',
          }}>
            오늘의 컬러를<br/>
            <span style={{ fontStyle: 'italic' }}>발견해볼까요?</span>
          </div>
        </div>

        {/* Hero CTA card */}
        <Card noPad style={{
          background: 'linear-gradient(135deg, #F2A6B5 0%, #C9B8E8 100%)',
          color: '#fff', overflow: 'hidden', position: 'relative',
          boxShadow: '0 12px 32px rgba(216, 126, 146, 0.25)',
        }}>
          {/* deco circles */}
          <div style={{
            position: 'absolute', top: -40, right: -40, width: 160, height: 160,
            borderRadius: '50%', background: 'rgba(255,255,255,0.15)',
          }}/>
          <div style={{
            position: 'absolute', bottom: -30, right: 30, width: 80, height: 80,
            borderRadius: '50%', background: 'rgba(255,255,255,0.1)',
          }}/>

          <div style={{ padding: 22, position: 'relative' }}>
            <Pill bg="rgba(255,255,255,0.25)" color="#fff">
              <IconSparkle size={11}/> AI 컬러 진단
            </Pill>
            <div style={{
              marginTop: 14, fontSize: 19, fontWeight: 700, lineHeight: 1.35,
              letterSpacing: '-0.02em',
            }}>
              사진 한 장으로<br/>나의 퍼스널 컬러 찾기
            </div>
            <div style={{ marginTop: 6, fontSize: 12, opacity: 0.9, lineHeight: 1.6 }}>
              평균 18초 · 정확도 92%
            </div>

            <button onClick={() => onNavigate('diagnosis')} className="tap" style={{
              marginTop: 20, padding: '12px 18px',
              background: '#fff', color: 'var(--primary-deep)',
              borderRadius: 12, border: 'none',
              fontSize: 14, fontWeight: 700,
              display: 'inline-flex', alignItems: 'center', gap: 8,
            }}>
              지금 진단 시작 <IconArrow size={16}/>
            </button>
          </div>
        </Card>

        {/* Quick actions */}
        <div style={{ marginTop: 16, display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 10 }}>
          <QuickAction
            icon={<IconMap size={20}/>}
            label="근처 매장"
            sub="2.4km 이내 7곳"
            color="var(--secondary-soft)"
            iconBg="var(--secondary)"
            onClick={() => onNavigate('map')}
          />
          <QuickAction
            icon={<IconUser size={20}/>}
            label="마이페이지"
            sub="진단 이력 3건"
            color="var(--accent-soft)"
            iconBg="var(--accent)"
            onClick={() => onNavigate('mypage')}
          />
        </div>

        {/* Recent diagnosis */}
        {recentDiagnosis && (
          <>
            <SectionHeader title="최근 진단 결과" action="모두 보기" onAction={() => onNavigate('mypage')}/>
            <Card noPad onClick={() => onNavigate('result')}>
              <div style={{ display: 'flex', gap: 14, padding: 16 }}>
                <div style={{
                  width: 80, height: 100, borderRadius: 12, flexShrink: 0,
                  background: `linear-gradient(135deg, ${recentDiagnosis.palette[0]}, ${recentDiagnosis.palette[1]})`,
                  position: 'relative', overflow: 'hidden',
                }}>
                  <div style={{
                    position: 'absolute', bottom: 6, right: 6,
                    background: 'rgba(255,255,255,0.9)', borderRadius: 8,
                    padding: '2px 6px', fontSize: 9, fontWeight: 700,
                    color: 'var(--text)',
                  }}>{recentDiagnosis.label}</div>
                </div>
                <div style={{ flex: 1, minWidth: 0 }}>
                  <div style={{ fontSize: 11, color: 'var(--text-dim)', fontWeight: 500 }}>
                    {recentDiagnosis.date}
                  </div>
                  <div className="serif" style={{
                    marginTop: 4, fontSize: 18, fontWeight: 600,
                    color: 'var(--text)', letterSpacing: '-0.01em',
                  }}>{recentDiagnosis.type}</div>
                  <div style={{
                    marginTop: 4, fontSize: 12, color: 'var(--text-mid)',
                    lineHeight: 1.5, display: '-webkit-box',
                    WebkitLineClamp: 2, WebkitBoxOrient: 'vertical', overflow: 'hidden',
                  }}>{recentDiagnosis.summary}</div>
                  <div style={{ marginTop: 10, display: 'flex', gap: 4 }}>
                    {recentDiagnosis.palette.slice(0, 5).map((c, i) => (
                      <div key={i} style={{
                        width: 18, height: 18, borderRadius: '50%', background: c,
                        border: '1.5px solid #fff',
                        boxShadow: '0 1px 3px rgba(0,0,0,0.1)',
                      }}/>
                    ))}
                  </div>
                </div>
                <IconChevron size={16} stroke="var(--text-dim)"/>
              </div>
            </Card>
          </>
        )}

        {/* Color story / inspiration */}
        <SectionHeader title="오늘의 컬러 스토리" sub="에디터 추천"/>
        <div style={{
          display: 'flex', gap: 12, overflowX: 'auto', margin: '0 -20px',
          padding: '0 20px 8px', scrollbarWidth: 'none',
        }} className="phone-scroll">
          {COLOR_STORIES.map((s, i) => (
            <div key={i} style={{
              minWidth: 180, borderRadius: 16, overflow: 'hidden',
              background: 'var(--card)', border: '1px solid var(--line-soft)',
              boxShadow: 'var(--shadow-sm)',
            }} className="tap">
              <div style={{
                height: 100, background: s.bg, position: 'relative',
                display: 'flex', alignItems: 'flex-end', padding: 12,
              }}>
                <div style={{ display: 'flex', gap: 4 }}>
                  {s.colors.map((c, j) => (
                    <div key={j} style={{
                      width: 14, height: 14, borderRadius: '50%', background: c,
                      border: '1.5px solid rgba(255,255,255,0.8)',
                    }}/>
                  ))}
                </div>
              </div>
              <div style={{ padding: '10px 12px 14px' }}>
                <div style={{ fontSize: 10, color: 'var(--text-dim)', fontWeight: 500 }}>
                  {s.tag}
                </div>
                <div style={{ marginTop: 3, fontSize: 13, fontWeight: 600, color: 'var(--text)', lineHeight: 1.3 }}>
                  {s.title}
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Drawer */}
      {drawerOpen && (
        <Drawer user={user} onClose={() => setDrawerOpen(false)} onNavigate={onNavigate} onLogout={onLogout}/>
      )}
    </div>
  );
}

function SectionHeader({ title, sub, action, onAction }) {
  return (
    <div style={{
      marginTop: 26, marginBottom: 12,
      display: 'flex', alignItems: 'flex-end', justifyContent: 'space-between',
    }}>
      <div>
        <div className="serif" style={{
          fontSize: 17, fontWeight: 600, color: 'var(--text)',
          letterSpacing: '-0.01em',
        }}>{title}</div>
        {sub && <div style={{ fontSize: 11, color: 'var(--text-dim)', marginTop: 2 }}>{sub}</div>}
      </div>
      {action && (
        <button onClick={onAction} className="tap" style={{
          fontSize: 12, color: 'var(--text-mid)', fontWeight: 500,
          display: 'flex', alignItems: 'center', gap: 2,
        }}>
          {action} <IconChevron size={12}/>
        </button>
      )}
    </div>
  );
}

function QuickAction({ icon, label, sub, color, iconBg, onClick }) {
  return (
    <button onClick={onClick} className="tap" style={{
      background: 'var(--card)', borderRadius: 16,
      padding: 14, display: 'flex', alignItems: 'center', gap: 12,
      border: '1px solid var(--line-soft)',
      boxShadow: 'var(--shadow-sm)', textAlign: 'left',
    }}>
      <div style={{
        width: 38, height: 38, borderRadius: 12,
        background: color,
        display: 'flex', alignItems: 'center', justifyContent: 'center',
        color: iconBg,
      }}>{icon}</div>
      <div style={{ flex: 1, minWidth: 0 }}>
        <div style={{ fontSize: 13, fontWeight: 600, color: 'var(--text)' }}>{label}</div>
        <div style={{ fontSize: 10, color: 'var(--text-dim)', marginTop: 1 }}>{sub}</div>
      </div>
    </button>
  );
}

function BottomTab({ current, onNavigate }) {
  const tabs = [
    { id: 'home', label: '홈', icon: IconHome, target: 'main' },
    { id: 'diagnose', label: '진단', icon: IconSparkle, target: 'diagnosis' },
    { id: 'map', label: '매장', icon: IconMap, target: 'map' },
    { id: 'mypage', label: 'MY', icon: IconUser, target: 'mypage' },
  ];
  return (
    <div style={{
      position: 'absolute', bottom: 0, left: 0, right: 0,
      background: 'rgba(255, 251, 248, 0.95)',
      backdropFilter: 'blur(20px)',
      WebkitBackdropFilter: 'blur(20px)',
      borderTop: '1px solid var(--line-soft)',
      padding: '8px 8px 12px',
      display: 'flex',
    }}>
      {tabs.map(t => {
        const active = t.id === current;
        const Ico = t.icon;
        return (
          <button key={t.id} onClick={() => onNavigate(t.target)} className="tap" style={{
            flex: 1, padding: '6px 4px',
            display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 3,
            color: active ? 'var(--primary-deep)' : 'var(--text-dim)',
          }}>
            <Ico size={20} sw={active ? 2 : 1.6}/>
            <div style={{ fontSize: 10, fontWeight: active ? 700 : 500 }}>{t.label}</div>
          </button>
        );
      })}
    </div>
  );
}

function Drawer({ user, onClose, onNavigate, onLogout }) {
  const items = [
    { icon: IconHome, label: '홈', target: 'main' },
    { icon: IconSparkle, label: '컬러 진단하기', target: 'diagnosis' },
    { icon: IconPalette, label: '내 컬러 리포트', target: 'mypage' },
    { icon: IconMap, label: '근처 매장 찾기', target: 'map' },
    { icon: IconBookmark, label: '즐겨찾기 매장', target: 'map' },
    { icon: IconChat, label: '컬러 상담', target: null },
  ];
  return (
    <>
      <div onClick={onClose} className="scrim-enter" style={{
        position: 'absolute', inset: 0, background: 'rgba(30, 20, 26, 0.45)', zIndex: 10,
      }}/>
      <div className="drawer-enter" style={{
        position: 'absolute', top: 0, bottom: 0, left: 0,
        width: '78%', background: 'var(--bg)', zIndex: 11,
        display: 'flex', flexDirection: 'column',
        boxShadow: '12px 0 40px rgba(0,0,0,0.15)',
      }}>
        {/* Header */}
        <div style={{
          padding: '40px 24px 24px',
          background: 'linear-gradient(160deg, #FCE2E8 0%, #ECE4F8 100%)',
          position: 'relative',
        }}>
          <button onClick={onClose} className="tap" style={{
            position: 'absolute', top: 16, right: 16,
            width: 36, height: 36, borderRadius: '50%',
            background: 'rgba(255,255,255,0.5)',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
          }}><IconClose size={18}/></button>

          <Avatar name={user.name} size={56}/>
          <div style={{ marginTop: 14, fontSize: 16, fontWeight: 700, color: 'var(--text)' }}>
            {user.name}
          </div>
          <div style={{ fontSize: 11, color: 'var(--text-mid)', marginTop: 2 }}>
            {user.email}
          </div>
          <div style={{ marginTop: 12, display: 'flex', gap: 6 }}>
            <Pill color="var(--w-burgundy)" bg="rgba(114, 47, 55, 0.1)">겨울 쿨톤</Pill>
            <Pill color="var(--accent)" bg="var(--accent-soft)">진단 3회</Pill>
          </div>
        </div>

        {/* Menu items */}
        <div style={{ flex: 1, overflow: 'auto', padding: '12px 0' }}>
          {items.map((it, i) => {
            const Ico = it.icon;
            return (
              <button key={i} onClick={() => { if (it.target) { onClose(); onNavigate(it.target); } }} className="tap" style={{
                width: '100%', display: 'flex', alignItems: 'center', gap: 14,
                padding: '14px 24px', textAlign: 'left',
                color: 'var(--text)',
              }}>
                <Ico size={20} stroke="var(--text-mid)"/>
                <div style={{ flex: 1, fontSize: 14, fontWeight: 500 }}>{it.label}</div>
                <IconChevron size={14} stroke="var(--text-dim)"/>
              </button>
            );
          })}

          <div style={{ height: 1, background: 'var(--line-soft)', margin: '12px 24px' }}/>

          <button className="tap" style={{
            width: '100%', display: 'flex', alignItems: 'center', gap: 14,
            padding: '14px 24px', color: 'var(--text-mid)',
          }}>
            <IconSettings size={20}/>
            <div style={{ flex: 1, fontSize: 14, fontWeight: 500, textAlign: 'left' }}>설정</div>
          </button>
          <button onClick={onLogout} className="tap" style={{
            width: '100%', display: 'flex', alignItems: 'center', gap: 14,
            padding: '14px 24px', color: 'var(--text-mid)',
          }}>
            <IconLogout size={20}/>
            <div style={{ flex: 1, fontSize: 14, fontWeight: 500, textAlign: 'left' }}>로그아웃</div>
          </button>
        </div>

        <div style={{
          padding: '16px 24px 24px', fontSize: 10, color: 'var(--text-dim)',
        }}>
          OliveMe · v1.0.0
        </div>
      </div>
    </>
  );
}

// Sample data
const COLOR_STORIES = [
  {
    tag: 'WINTER · COOL',
    title: '와인빛 깊은 가을 무드',
    bg: 'linear-gradient(135deg, #722F37, #1B2A4E)',
    colors: ['#722F37', '#1B2A4E', '#C13584'],
  },
  {
    tag: 'SPRING · WARM',
    title: '코랄 피치 데이라이트',
    bg: 'linear-gradient(135deg, #FF9E80, #FFD4A3)',
    colors: ['#FF9E80', '#FFD4A3', '#FFB6A1'],
  },
  {
    tag: 'SUMMER · COOL',
    title: '라벤더 듀 모닝',
    bg: 'linear-gradient(135deg, #C9B8E8, #B5D5E8)',
    colors: ['#C9B8E8', '#B5D5E8', '#E8C5D5'],
  },
];

window.MainScreen = MainScreen;
window.BottomTab = BottomTab;
window.SectionHeader = SectionHeader;
