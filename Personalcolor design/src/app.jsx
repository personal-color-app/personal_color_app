// OliveMe — main app: navigation, state, mock data
const { useState: useS, useEffect: useE, useRef: useR } = React;
// alias used by app
const useRef = React.useRef;

// ─── Mock data ───
const USER = {
  name: '민서',
  email: 'kakao.minseo@oliveme.app',
  joinedAt: '2025년 9월',
};

const RESULT = {
  type: '겨울 쿨톤',
  englishLabel: 'WINTER · COOL · DEEP',
  matchScore: 92,
  dateLabel: '2026.05.18 토',
  heroBg: ['#5B1A1F', '#1B2A4E'],
  description:
    '깊고 시원한 톤이 어울려요. 채도가 높고 선명한 컬러로 얼굴의 윤곽을 또렷하게 살리는 인상이에요.',
  signature:
    '와인빛 깊이와 차가운 광채. 깊은 컬러일수록 당신의 시선이 또렷해지는, 도시적이면서 우아한 무드.',
  matchScoreLabel: '매칭 정확도',
  palette: [
    { name: '딥 와인', hex: '#722F37' },
    { name: '딥 네이비', hex: '#1B2A4E' },
    { name: '아이스 핑크', hex: '#F2C2D1' },
    { name: '푸시아', hex: '#C13584' },
    { name: '플럼', hex: '#4A2347' },
    { name: '머스크 그레이', hex: '#6B7280' },
  ],
  avoidColors: [
    { name: '오렌지', hex: '#E89A5F' },
    { name: '머스타드', hex: '#C9A14E' },
    { name: '카키 브라운', hex: '#7A6240' },
    { name: '베이지', hex: '#D8C0A0' },
  ],
  clothes: [
    { name: '와인 니트', hex: '#722F37' },
    { name: '딥 네이비 셔츠', hex: '#1B2A4E' },
    { name: '버건디 트렌치', hex: '#5B1A1F' },
    { name: '아이스 핑크 블라우스', hex: '#F2C2D1' },
    { name: '딥 푸시아', hex: '#C13584' },
    { name: '차콜 그레이', hex: '#3D3B4A' },
  ],
  makeup: [
    {
      category: '립 컬러', icon: 'lip', kind: 'lipstick',
      items: [
        { name: '와인 매트', hex: '#6B2737' },
        { name: '딥 베리', hex: '#9B2D5F' },
        { name: '체리 글로스', hex: '#A02942' },
        { name: '플럼 누드', hex: '#8A4B6F' },
        { name: '쿨 로즈', hex: '#B85C7B' },
        { name: '아이스 핑크', hex: '#F2C2D1' },
      ],
    },
    {
      category: '아이 섀도우', icon: 'eye', kind: 'eyeshadow',
      items: [
        { name: '딥 플럼', hex: '#4A2347' },
        { name: '쿨 그레이', hex: '#6B7280' },
        { name: '버건디 글리터', hex: '#5B1A1F' },
        { name: '아이스 라벤더', hex: '#C9B8E8' },
        { name: '미드나잇 블루', hex: '#2A3F5F' },
        { name: '실버 샤인', hex: '#D5D8E0' },
      ],
    },
    {
      category: '베이스', icon: 'base', kind: 'cushion',
      items: [
        { name: '쿨 아이보리 21', hex: '#F2E1D2' },
        { name: '쿨 베이지 23', hex: '#E5C8B0' },
        { name: '핑크 글로우', hex: '#F4D5DA' },
      ],
    },
  ],
  traits: [
    {
      title: '뚜렷한 명도 대비',
      body: '검정과 흰색처럼 명확한 컬러 대비가 잘 어울려요. 모호한 톤보다 또렷한 색이 얼굴을 살립니다.',
      bg: 'rgba(114, 47, 55, 0.1)', color: 'var(--w-burgundy)',
    },
    {
      title: '쿨한 핑크 언더톤',
      body: '피부에 푸른빛이 도는 차가운 톤. 골드보다 실버 액세서리가 더 잘 어울려요.',
      bg: 'rgba(27, 42, 78, 0.1)', color: 'var(--w-navy)',
    },
    {
      title: '깊고 진한 컬러 적합',
      body: '파스텔이나 흐릿한 색보다 와인, 푸시아처럼 채도 높은 깊은 색이 시너지를 만들어요.',
      bg: 'rgba(193, 53, 132, 0.12)', color: 'var(--w-fuchsia)',
    },
  ],
  keywords: ['도시적인', '우아한', '강렬한', '미니멀', '모던클래식', '컨템포러리'],
};

const HISTORY = [
  {
    id: 'h2', date: '2026.04.02 목', type: '겨울 쿨톤 (브라이트)',
    palette: ['#722F37', '#C13584', '#F2C2D1', '#1B2A4E', '#6B7280'],
  },
  {
    id: 'h3', date: '2026.02.14 금', type: '여름 쿨톤 (라이트)',
    palette: ['#C9B8E8', '#B5D5E8', '#E8C5D5', '#9DCDC1', '#D8B8C5'],
  },
];

const RECENT_PREVIEW = {
  date: '2026.05.18',
  type: '겨울 쿨톤',
  label: 'NEW',
  summary: '깊고 차가운 컬러가 또렷한 인상을 살리는 타입. 와인·푸시아·네이비 추천.',
  palette: ['#722F37', '#C13584', '#1B2A4E', '#F2C2D1', '#4A2347'],
};

// Screen order for swipe navigation (login is outside the swipe loop)
const SWIPE_ORDER = ['main', 'diagnosis', 'result', 'map', 'mypage'];

// ─── App ───
function OliveMeApp() {
  const [screen, setScreen] = useState('login');
  const [savedDiagnoses, setSavedDiagnoses] = useState(true);
  const [swipeOffset, setSwipeOffset] = useState(0);
  const [swipeDir, setSwipeDir] = useState(0); // -1 prev / +1 next during animation
  const touch = useRef({ x: 0, y: 0, active: false, locked: null });

  const navigate = (target) => {
    if (target === 'login') return setScreen('login');
    if (target === 'logout') return setScreen('login');
    setScreen(target);
  };

  // Pointer (touch + mouse drag) handlers — horizontal swipe between adjacent screens
  const onPointerDown = (e) => {
    if (screen === 'login') return;
    // ignore right-click etc.
    if (e.pointerType === 'mouse' && e.button !== 0) return;
    touch.current = { x: e.clientX, y: e.clientY, active: true, locked: null };
    setSwipeOffset(0);
    setSwipeDir(0);
  };
  const onPointerMove = (e) => {
    if (!touch.current.active) return;
    const dx = e.clientX - touch.current.x;
    const dy = e.clientY - touch.current.y;
    if (touch.current.locked == null) {
      if (Math.abs(dx) > 10 || Math.abs(dy) > 10) {
        touch.current.locked = Math.abs(dx) > Math.abs(dy) ? 'x' : 'y';
      }
    }
    if (touch.current.locked === 'x') {
      const idx = SWIPE_ORDER.indexOf(screen);
      let off = dx;
      if ((idx === 0 && dx > 0) || (idx === SWIPE_ORDER.length - 1 && dx < 0)) {
        off = dx * 0.25;
      }
      setSwipeOffset(off);
    }
  };
  const onPointerUp = () => {
    if (!touch.current.active) return;
    const dx = swipeOffset;
    const wasX = touch.current.locked === 'x';
    touch.current.active = false;
    const idx = SWIPE_ORDER.indexOf(screen);
    const THRESHOLD = 60;
    if (wasX && Math.abs(dx) > THRESHOLD && idx >= 0) {
      if (dx < 0 && idx < SWIPE_ORDER.length - 1) {
        setScreen(SWIPE_ORDER[idx + 1]);
      } else if (dx > 0 && idx > 0) {
        setScreen(SWIPE_ORDER[idx - 1]);
      }
    }
    setSwipeOffset(0);
  };

  let body;
  if (screen === 'login') {
    body = <LoginScreen onLogin={() => setScreen('main')} />;
  } else if (screen === 'main') {
    body = <MainScreen
      user={USER}
      recentDiagnosis={savedDiagnoses ? RECENT_PREVIEW : null}
      onNavigate={navigate}
      onLogout={() => setScreen('login')}
    />;
  } else if (screen === 'diagnosis') {
    body = <DiagnosisScreen
      onBack={() => setScreen('main')}
      onComplete={() => setScreen('result')}
    />;
  } else if (screen === 'result') {
    body = <ResultScreen
      result={RESULT}
      onBack={() => setScreen('main')}
      onMap={() => setScreen('map')}
      onMyPage={() => { setSavedDiagnoses(true); setScreen('mypage'); }}
      onSave={() => setSavedDiagnoses(true)}
    />;
  } else if (screen === 'map') {
    body = <MapScreen onBack={() => setScreen('main')} onNavigate={navigate}/>;
  } else if (screen === 'mypage') {
    body = <MyPageScreen
      user={USER}
      latest={RESULT}
      history={HISTORY}
      onBack={() => setScreen('main')}
      onNavigate={navigate}
      onOpenResult={() => setScreen('result')}
    />;
  }

  return (
    <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 16 }}>
      <AndroidDevice width={400} height={860}>
        <div
          onPointerDown={onPointerDown}
          onPointerMove={onPointerMove}
          onPointerUp={onPointerUp}
          onPointerCancel={onPointerUp}
          style={{
            height: '100%',
            display: 'flex', flexDirection: 'column',
            transform: `translateX(${swipeOffset}px)`,
            transition: touch.current.active ? 'none' : 'transform 0.25s cubic-bezier(0.2, 0.8, 0.2, 1)',
            touchAction: 'pan-y',
            userSelect: 'none',
          }}>
          {body}
        </div>
      </AndroidDevice>

      {/* Below-frame nav: jump to any screen */}
      <ScreenNav current={screen} onJump={setScreen}/>
      <SwipeHint screen={screen}/>
    </div>
  );
}

function SwipeHint({ screen }) {
  const idx = SWIPE_ORDER.indexOf(screen);
  if (idx < 0) return null;
  const prev = idx > 0 ? SWIPE_ORDER[idx - 1] : null;
  const next = idx < SWIPE_ORDER.length - 1 ? SWIPE_ORDER[idx + 1] : null;
  const labels = { main: '홈', diagnosis: '진단', result: '결과', map: '지도', mypage: 'MY' };
  return (
    <div style={{
      display: 'flex', alignItems: 'center', gap: 10,
      fontSize: 10, color: 'rgba(255,255,255,0.45)', fontWeight: 500,
      letterSpacing: '0.1em', textTransform: 'uppercase',
    }}>
      <span style={{ opacity: prev ? 1 : 0.3 }}>{prev ? `← ${labels[prev]}` : '—'}</span>
      <span style={{ color: 'rgba(255,255,255,0.7)', fontWeight: 700 }}>swipe</span>
      <span style={{ opacity: next ? 1 : 0.3 }}>{next ? `${labels[next]} →` : '—'}</span>
    </div>
  );
}

function ScreenNav({ current, onJump }) {
  const screens = [
    { id: 'login', label: '로그인', icon: IconLogout },
    { id: 'main', label: '홈', icon: IconHome },
    { id: 'diagnosis', label: '진단', icon: IconSparkle },
    { id: 'result', label: '결과', icon: IconPalette },
    { id: 'map', label: '지도', icon: IconMap },
    { id: 'mypage', label: 'MY', icon: IconUser },
  ];
  return (
    <div style={{
      display: 'flex', alignItems: 'center', gap: 6,
      padding: '10px 14px', borderRadius: 100,
      background: 'rgba(255,255,255,0.08)',
      border: '1px solid rgba(255,255,255,0.1)',
      backdropFilter: 'blur(20px)',
    }}>
      <div style={{
        fontSize: 10, color: 'rgba(255,255,255,0.45)', fontWeight: 600,
        letterSpacing: '0.18em', marginRight: 8, textTransform: 'uppercase',
      }}>Jump to</div>
      {screens.map(s => {
        const active = s.id === current;
        const Ico = s.icon;
        return (
          <button key={s.id} onClick={() => onJump(s.id)} style={{
            padding: '8px 12px', borderRadius: 100,
            background: active ? '#fff' : 'transparent',
            color: active ? '#3D3137' : 'rgba(255,255,255,0.7)',
            fontSize: 11, fontWeight: 600,
            display: 'flex', alignItems: 'center', gap: 5,
            border: 'none', cursor: 'pointer',
            transition: 'all 0.15s ease',
          }}>
            <Ico size={12} sw={2}/>{s.label}
          </button>
        );
      })}
    </div>
  );
}

ReactDOM.createRoot(document.getElementById('root')).render(<OliveMeApp/>);
