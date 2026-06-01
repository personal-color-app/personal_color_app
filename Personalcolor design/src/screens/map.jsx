// Map screen — nearby beauty store search with mock map & bottom sheet
function MapScreen({ onBack, onNavigate }) {
  const [selected, setSelected] = useState(0);
  const stores = SAMPLE_STORES;

  return (
    <div data-screen-label="05 Map" style={{
      height: '100%', display: 'flex', flexDirection: 'column',
      background: 'var(--bg)', position: 'relative', overflow: 'hidden',
    }}>
      {/* Map area — fills above bottom sheet */}
      <div style={{
        position: 'absolute', top: 0, left: 0, right: 0, bottom: 0,
        background: '#E8EAE5',
      }}>
        <MockMap stores={stores} selected={selected} onSelect={setSelected}/>
      </div>

      {/* Top header (transparent over map) */}
      <div style={{ position: 'relative', zIndex: 2 }}>
        <AppBar
          transparent
          leading={
            <button onClick={onBack} className="tap" style={{
              width: 44, height: 44, borderRadius: '50%',
              background: 'rgba(255,255,255,0.95)',
              display: 'flex', alignItems: 'center', justifyContent: 'center',
              boxShadow: 'var(--shadow-sm)',
            }}><IconBack/></button>
          }
          title=""
          trailing={
            <button className="tap" style={{
              width: 44, height: 44, borderRadius: '50%',
              background: 'rgba(255,255,255,0.95)',
              display: 'flex', alignItems: 'center', justifyContent: 'center',
              boxShadow: 'var(--shadow-sm)',
            }}><IconLocate size={20}/></button>
          }
        />

        {/* Search bar */}
        <div style={{ padding: '0 16px', position: 'absolute', top: 14, left: 60, right: 60 }}>
          <div style={{
            background: 'rgba(255,255,255,0.95)', borderRadius: 100,
            padding: '12px 18px', display: 'flex', alignItems: 'center', gap: 10,
            boxShadow: 'var(--shadow-sm)',
          }}>
            <IconSearch size={16} stroke="var(--text-mid)"/>
            <div style={{ flex: 1, fontSize: 13, color: 'var(--text)', fontWeight: 500 }}>
              내 주변 뷰티 매장
            </div>
            <div style={{
              padding: '3px 8px', background: 'var(--primary-soft)',
              color: 'var(--primary-deep)', borderRadius: 6,
              fontSize: 10, fontWeight: 700,
            }}>{stores.length}</div>
          </div>
        </div>

        {/* Filter chips */}
        <div style={{ padding: '12px 16px 0', display: 'flex', gap: 8, overflowX: 'auto' }} className="phone-scroll">
          {['전체', '영업 중', '드러그스토어', '백화점', '브랜드샵', '✓ 즐겨찾기'].map((f, i) => (
            <button key={i} className="tap" style={{
              padding: '6px 14px', borderRadius: 100, whiteSpace: 'nowrap',
              background: i === 0 ? 'var(--text)' : 'rgba(255,255,255,0.95)',
              color: i === 0 ? '#fff' : 'var(--text)',
              fontSize: 11, fontWeight: 600,
              boxShadow: 'var(--shadow-sm)',
            }}>{f}</button>
          ))}
        </div>
      </div>

      {/* Bottom sheet — store list */}
      <div style={{
        position: 'absolute', bottom: 0, left: 0, right: 0,
        background: 'var(--card)',
        borderTopLeftRadius: 24, borderTopRightRadius: 24,
        boxShadow: '0 -10px 30px rgba(0,0,0,0.08)',
        maxHeight: '54%', display: 'flex', flexDirection: 'column',
        zIndex: 3,
      }}>
        {/* drag handle */}
        <div style={{ padding: '10px 0 6px', display: 'flex', justifyContent: 'center' }}>
          <div style={{ width: 40, height: 4, borderRadius: 2, background: 'var(--line)' }}/>
        </div>

        <div style={{ padding: '4px 20px 8px', display: 'flex', alignItems: 'baseline', justifyContent: 'space-between' }}>
          <div>
            <div className="serif" style={{
              fontSize: 17, fontWeight: 600, color: 'var(--text)',
              letterSpacing: '-0.01em',
            }}>근처 매장 {stores.length}곳</div>
            <div style={{ fontSize: 11, color: 'var(--text-dim)', marginTop: 2 }}>
              부산 금정구 부산대학로 일대 · 2km 이내
            </div>
          </div>
          <div style={{ display: 'flex', gap: 4 }}>
            <button className="tap" style={{
              padding: 6, color: 'var(--text-mid)',
            }}><IconList size={18}/></button>
          </div>
        </div>

        <div className="phone-scroll" style={{ flex: 1, overflow: 'auto', padding: '4px 16px 20px' }}>
          {stores.map((s, i) => (
            <StoreCard key={s.id} store={s} active={i === selected} onClick={() => setSelected(i)} />
          ))}
        </div>
      </div>
    </div>
  );
}

// ─── Mock map (SVG) ───
function MockMap({ stores, selected, onSelect }) {
  return (
    <div style={{ position: 'absolute', inset: 0, overflow: 'hidden' }}>
      {/* base map svg — abstract Korean city block layout */}
      <svg viewBox="0 0 400 800" preserveAspectRatio="xMidYMid slice" style={{
        position: 'absolute', inset: 0, width: '100%', height: '100%',
        background: 'linear-gradient(180deg, #EDF1E9 0%, #E1E8DC 100%)',
      }}>
        {/* park / water blocks */}
        <rect x="0" y="0" width="400" height="800" fill="#E8EDE2"/>
        <path d="M-20 200 Q 60 180 140 240 T 280 220 L 420 280 L 420 380 L -20 380 Z" fill="#D7E6E2" opacity="0.6"/>
        <circle cx="80" cy="150" r="50" fill="#CDDAB9" opacity="0.6"/>
        <circle cx="320" cy="600" r="70" fill="#CDDAB9" opacity="0.6"/>
        <rect x="40" y="450" width="100" height="80" rx="6" fill="#DEE6D5"/>
        <rect x="220" y="500" width="140" height="100" rx="6" fill="#DEE6D5"/>
        <rect x="50" y="650" width="120" height="60" rx="6" fill="#DEE6D5"/>

        {/* roads */}
        <g stroke="#FFFFFF" strokeLinecap="round">
          <line x1="0" y1="120" x2="400" y2="140" strokeWidth="14"/>
          <line x1="0" y1="120" x2="400" y2="140" strokeWidth="12" stroke="#F5F6F1"/>
          <line x1="0" y1="320" x2="400" y2="350" strokeWidth="10" stroke="#F5F6F1"/>
          <line x1="0" y1="430" x2="400" y2="430" strokeWidth="22"/>
          <line x1="0" y1="430" x2="400" y2="430" strokeWidth="18" stroke="#F5F6F1"/>
          <line x1="0" y1="620" x2="400" y2="640" strokeWidth="10" stroke="#F5F6F1"/>

          <line x1="100" y1="0" x2="120" y2="800" strokeWidth="12" stroke="#F5F6F1"/>
          <line x1="240" y1="0" x2="260" y2="800" strokeWidth="14"/>
          <line x1="240" y1="0" x2="260" y2="800" strokeWidth="12" stroke="#F5F6F1"/>
          <line x1="340" y1="0" x2="350" y2="800" strokeWidth="8" stroke="#F5F6F1"/>
        </g>

        {/* labels */}
        <text x="190" y="100" fill="#A6B098" fontSize="9" fontFamily="Noto Sans KR" fontWeight="500">금정로</text>
        <text x="160" y="420" fill="#A6B098" fontSize="11" fontFamily="Noto Sans KR" fontWeight="600">부산대학로</text>
        <text x="80" y="490" fill="#B5B4A8" fontSize="9" fontFamily="Noto Sans KR">부산대학교</text>
        <text x="260" y="540" fill="#B5B4A8" fontSize="9" fontFamily="Noto Sans KR">상권</text>

        {/* subway dot */}
        <circle cx="260" cy="430" r="8" fill="#2E7BBF"/>
        <text x="276" y="434" fill="#2E7BBF" fontSize="10" fontFamily="Noto Sans KR" fontWeight="700">부산대</text>
      </svg>

      {/* current location */}
      <div style={{
        position: 'absolute', left: '52%', top: '46%', transform: 'translate(-50%, -50%)',
        width: 80, height: 80, borderRadius: '50%',
        background: 'radial-gradient(circle, rgba(46,123,191,0.3), transparent 60%)',
      }}/>
      <div style={{
        position: 'absolute', left: '52%', top: '46%', transform: 'translate(-50%, -50%)',
        width: 18, height: 18, borderRadius: '50%',
        background: '#2E7BBF', border: '3px solid #fff',
        boxShadow: '0 2px 8px rgba(0,0,0,0.25)',
      }}/>

      {/* store markers */}
      {stores.map((s, i) => (
        <button key={s.id} onClick={() => onSelect(i)} className="tap" style={{
          position: 'absolute', left: `${s.x}%`, top: `${s.y}%`,
          transform: 'translate(-50%, -100%)',
          padding: 0, background: 'none', border: 'none',
        }}>
          <StoreMarker label={s.name.slice(0, 4)} active={i === selected} brand={s.brandColor}/>
        </button>
      ))}
    </div>
  );
}

function StoreMarker({ label, active, brand }) {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
      <div style={{
        padding: active ? '6px 12px' : '4px 9px',
        borderRadius: 100,
        background: active ? 'var(--text)' : '#fff',
        color: active ? '#fff' : 'var(--text)',
        fontSize: active ? 12 : 10, fontWeight: 700,
        boxShadow: active ? '0 6px 16px rgba(0,0,0,0.25)' : '0 2px 6px rgba(0,0,0,0.15)',
        whiteSpace: 'nowrap',
        transition: 'all 0.2s ease',
        border: active ? 'none' : `2px solid ${brand}`,
      }}>{label}</div>
      <div style={{
        width: 0, height: 0,
        borderLeft: `${active ? 5 : 4}px solid transparent`,
        borderRight: `${active ? 5 : 4}px solid transparent`,
        borderTop: `${active ? 6 : 5}px solid ${active ? 'var(--text)' : '#fff'}`,
        marginTop: -1,
      }}/>
    </div>
  );
}

function StoreCard({ store, active, onClick }) {
  return (
    <div onClick={onClick} className="tap" style={{
      padding: '14px 14px',
      borderRadius: 14,
      background: active ? 'var(--primary-soft)' : 'transparent',
      border: active ? '1px solid var(--primary)' : '1px solid transparent',
      marginBottom: 6,
      display: 'flex', gap: 12,
    }}>
      <div style={{
        width: 56, height: 56, borderRadius: 12,
        background: store.brandColor,
        display: 'flex', alignItems: 'center', justifyContent: 'center',
        color: '#fff', fontSize: 16, fontWeight: 700,
        flexShrink: 0,
        fontFamily: 'Cormorant Garamond, serif',
        fontStyle: 'italic',
        boxShadow: 'var(--shadow-sm)',
      }}>{store.initials}</div>
      <div style={{ flex: 1, minWidth: 0 }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
          <div style={{ fontSize: 14, fontWeight: 700, color: 'var(--text)' }}>{store.name}</div>
          {store.favorite && <IconHeart size={12} fill="var(--primary-deep)" stroke="var(--primary-deep)"/>}
        </div>
        <div style={{ marginTop: 3, fontSize: 11, color: 'var(--text-mid)' }}>
          {store.type} · {store.address}
        </div>
        <div style={{ marginTop: 6, display: 'flex', alignItems: 'center', gap: 10, fontSize: 11 }}>
          <span style={{
            color: store.open ? '#2DB88A' : 'var(--text-dim)', fontWeight: 700,
          }}>
            ● {store.open ? '영업 중' : '영업 종료'}
          </span>
          <span style={{ color: 'var(--text-dim)' }}>{store.hours}</span>
          <span style={{ color: 'var(--text-dim)' }}>· {store.distance}</span>
        </div>
      </div>
      <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end', gap: 6 }}>
        <div style={{
          display: 'flex', alignItems: 'center', gap: 3,
          padding: '3px 7px', borderRadius: 6,
          background: 'var(--accent-soft)', color: 'var(--accent)',
          fontSize: 10, fontWeight: 700,
        }}><IconStar size={9} fill="var(--accent)"/> {store.rating}</div>
        <button className="tap" style={{
          padding: '6px 10px', borderRadius: 8,
          background: 'var(--text)', color: '#fff',
          fontSize: 10, fontWeight: 700,
        }}>길찾기</button>
      </div>
    </div>
  );
}

const SAMPLE_STORES = [
  { id: 1, name: '뷰티샵 부산대점', initials: 'BS', type: '드러그스토어', address: '부산대학로 63', open: true, hours: '~22:00', distance: '180m', rating: 4.8, favorite: true, x: 50, y: 38, brandColor: '#7BA068' },
  { id: 2, name: '컬러랩 NC점', initials: 'CL', type: '드러그스토어', address: '금정로 250', open: true, hours: '~23:00', distance: '420m', rating: 4.6, favorite: false, x: 32, y: 58, brandColor: '#D87E92' },
  { id: 3, name: '코스메 장전점', initials: 'CO', type: '브랜드샵', address: '장전로 14', open: true, hours: '~21:30', distance: '680m', rating: 4.5, favorite: true, x: 68, y: 28, brandColor: '#7B5BC9' },
  { id: 4, name: '메이크업하우스', initials: 'MH', type: '드러그스토어', address: '온천천로 88', open: false, hours: '~22:00', distance: '950m', rating: 4.4, favorite: false, x: 72, y: 66, brandColor: '#C9994E' },
  { id: 5, name: '뷰티앤 롯데백화점', initials: 'B&', type: '백화점', address: '중앙대로 935', open: true, hours: '~20:00', distance: '1.4km', rating: 4.7, favorite: false, x: 22, y: 78, brandColor: '#3D3137' },
];

window.MapScreen = MapScreen;
