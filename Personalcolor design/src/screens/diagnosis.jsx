// Diagnosis screen — photo upload → step-by-step analysis
function DiagnosisScreen({ onBack, onComplete }) {
  // stages: 'choose' (pick photo) -> 'preview' (review) -> 'analyzing' (steps) -> auto onComplete
  const [stage, setStage] = useState('choose');
  const [step, setStep] = useState(0);

  const steps = [
    { label: '얼굴 인식 중', sub: '눈·코·입의 위치를 찾고 있어요', icon: IconUser },
    { label: '피부 색상 추출', sub: '피부톤과 언더톤을 분석합니다', icon: IconPalette },
    { label: '컬러 매칭', sub: '4계절 16타입 데이터와 매칭 중', icon: IconSparkle },
    { label: '결과 정리', sub: '나만의 추천 팔레트를 만들고 있어요', icon: IconLayers },
  ];

  useEffect(() => {
    if (stage !== 'analyzing') return;
    if (step >= steps.length) {
      const t = setTimeout(() => onComplete(), 600);
      return () => clearTimeout(t);
    }
    const t = setTimeout(() => setStep(s => s + 1), 1100);
    return () => clearTimeout(t);
  }, [stage, step]);

  return (
    <div data-screen-label="03 Diagnosis" style={{
      height: '100%', display: 'flex', flexDirection: 'column',
      background: 'var(--bg)', position: 'relative', overflow: 'hidden',
    }}>
      <AppBar
        leading={<IconButton onClick={onBack}><IconBack/></IconButton>}
        title="컬러 진단"
        trailing={<IconButton><IconChat/></IconButton>}
      />

      {stage === 'choose' && (
        <ChoosePhoto onPick={() => setStage('preview')} />
      )}
      {stage === 'preview' && (
        <PreviewPhoto onRetry={() => setStage('choose')} onAnalyze={() => { setStage('analyzing'); setStep(0); }} />
      )}
      {stage === 'analyzing' && (
        <Analyzing steps={steps} currentStep={step}/>
      )}
    </div>
  );
}

function ChoosePhoto({ onPick }) {
  return (
    <div className="phone-scroll" style={{ flex: 1, overflow: 'auto', padding: '0 20px 24px' }}>
      <div style={{ padding: '4px 0 20px' }}>
        <div className="serif" style={{
          fontSize: 22, fontWeight: 500, color: 'var(--text)',
          letterSpacing: '-0.01em', lineHeight: 1.35,
        }}>
          정면 사진으로<br/>나의 컬러를 찾아드릴게요
        </div>
        <div style={{ marginTop: 8, fontSize: 12, color: 'var(--text-mid)', lineHeight: 1.6 }}>
          AI가 18초 안에 분석을 완료합니다
        </div>
      </div>

      {/* Big upload area */}
      <button onClick={onPick} className="tap" style={{
        width: '100%', aspectRatio: '4/5',
        background: 'linear-gradient(160deg, #FDF4F0 0%, #F5EDF8 100%)',
        borderRadius: 24,
        border: '2px dashed var(--primary)',
        display: 'flex', flexDirection: 'column',
        alignItems: 'center', justifyContent: 'center',
        gap: 14, position: 'relative', overflow: 'hidden',
      }}>
        <Blob color="rgba(242,166,181,0.4)" size={160} top={-30} right={-30}/>
        <Blob color="rgba(201,184,232,0.4)" size={140} bottom={-30} left={-30}/>

        <div style={{
          width: 72, height: 72, borderRadius: '50%',
          background: '#fff', display: 'flex', alignItems: 'center', justifyContent: 'center',
          boxShadow: '0 6px 20px rgba(216, 126, 146, 0.25)',
          color: 'var(--primary-deep)', zIndex: 1,
        }}>
          <IconPlus size={32} sw={2}/>
        </div>
        <div style={{ textAlign: 'center', zIndex: 1 }}>
          <div style={{ fontSize: 15, fontWeight: 600, color: 'var(--text)' }}>
            사진을 추가해주세요
          </div>
          <div style={{ marginTop: 4, fontSize: 12, color: 'var(--text-mid)' }}>
            얼굴이 잘 보이는 사진을 선택하세요
          </div>
        </div>
      </button>

      {/* Source buttons */}
      <div style={{ marginTop: 14, display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 10 }}>
        <SourceBtn icon={<IconCamera size={18}/>} label="카메라" onClick={onPick}/>
        <SourceBtn icon={<IconGallery size={18}/>} label="갤러리" onClick={onPick}/>
      </div>

      {/* Tips card */}
      <div style={{
        marginTop: 20, padding: 16, borderRadius: 16,
        background: 'var(--secondary-soft)',
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
          <IconSparkle size={16} stroke="var(--secondary)"/>
          <div style={{ fontSize: 13, fontWeight: 700, color: 'var(--text)' }}>
            더 정확한 진단을 위한 팁
          </div>
        </div>
        <div style={{ marginTop: 12, display: 'flex', flexDirection: 'column', gap: 8 }}>
          {TIPS.map((t, i) => (
            <div key={i} style={{ display: 'flex', alignItems: 'flex-start', gap: 8 }}>
              <div style={{
                width: 18, height: 18, borderRadius: '50%',
                background: '#fff', flexShrink: 0,
                display: 'flex', alignItems: 'center', justifyContent: 'center',
                color: 'var(--secondary)', marginTop: 1,
              }}><IconCheck size={11} sw={2.5}/></div>
              <div style={{ fontSize: 12, color: 'var(--text-mid)', lineHeight: 1.5 }}>{t}</div>
            </div>
          ))}
        </div>
      </div>

      {/* Sample photo row */}
      <div style={{ marginTop: 20 }}>
        <div style={{ fontSize: 12, color: 'var(--text-dim)', fontWeight: 500, marginBottom: 10 }}>
          또는 샘플 사진으로 체험해보세요
        </div>
        <div style={{ display: 'flex', gap: 10 }}>
          {[1,2,3,4].map(i => (
            <button key={i} onClick={onPick} className="tap" style={{
              flex: 1, aspectRatio: '1', borderRadius: 12, overflow: 'hidden',
              background: `linear-gradient(${i*60}deg, var(--primary-soft), var(--secondary-soft))`,
              border: '1px solid var(--line-soft)',
            }}>
              <FacePlaceholder size={60}/>
            </button>
          ))}
        </div>
      </div>
    </div>
  );
}

function SourceBtn({ icon, label, onClick }) {
  return (
    <button onClick={onClick} className="tap" style={{
      padding: '14px',
      background: 'var(--card)', borderRadius: 12,
      border: '1px solid var(--line)',
      display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 8,
      fontSize: 13, fontWeight: 600, color: 'var(--text)',
    }}>
      {icon} {label}
    </button>
  );
}

function PreviewPhoto({ onRetry, onAnalyze }) {
  return (
    <div className="phone-scroll" style={{ flex: 1, overflow: 'auto', padding: '0 20px 24px', display: 'flex', flexDirection: 'column' }}>
      <div style={{ padding: '4px 0 16px' }}>
        <div className="serif" style={{
          fontSize: 20, fontWeight: 500, color: 'var(--text)',
        }}>
          이 사진으로 진단할까요?
        </div>
      </div>

      <div style={{
        width: '100%', aspectRatio: '4/5',
        borderRadius: 24, overflow: 'hidden', position: 'relative',
        background: 'linear-gradient(160deg, #F4D5C2 0%, #E8C5D5 100%)',
        display: 'flex', alignItems: 'center', justifyContent: 'center',
      }}>
        <FacePlaceholder size={240}/>
        {/* face detection box hint */}
        <div style={{
          position: 'absolute', top: '22%', left: '28%', right: '28%', bottom: '32%',
          border: '2px solid rgba(255,255,255,0.8)', borderRadius: '50%',
          boxShadow: '0 0 0 9999px rgba(0,0,0,0.05)',
        }}>
          <div style={{
            position: 'absolute', top: -22, left: 0,
            background: 'rgba(255,255,255,0.95)', padding: '3px 8px', borderRadius: 6,
            fontSize: 9, fontWeight: 700, color: 'var(--text)',
          }}>FACE · 98%</div>
        </div>
      </div>

      <div style={{ marginTop: 14, padding: 14, borderRadius: 12, background: 'var(--card-2)', border: '1px solid var(--line-soft)' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
          <div style={{
            width: 32, height: 32, borderRadius: '50%',
            background: 'rgba(45, 184, 138, 0.15)', color: '#2DB88A',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
          }}><IconCheck size={16} sw={2.5}/></div>
          <div style={{ flex: 1 }}>
            <div style={{ fontSize: 13, fontWeight: 600, color: 'var(--text)' }}>
              사진 품질 우수
            </div>
            <div style={{ fontSize: 11, color: 'var(--text-mid)', marginTop: 1 }}>
              조명·해상도·각도 모두 적합합니다
            </div>
          </div>
        </div>
      </div>

      <div style={{ flex: 1 }}/>

      <div style={{ marginTop: 20, display: 'flex', gap: 10 }}>
        <button onClick={onRetry} className="tap" style={{
          padding: '15px 22px',
          background: 'var(--card)', color: 'var(--text)',
          border: '1px solid var(--line)', borderRadius: 14,
          fontSize: 14, fontWeight: 600,
          display: 'flex', alignItems: 'center', gap: 6,
        }}>
          <IconRefresh size={16}/> 다시 선택
        </button>
        <CTAButton icon={<IconSparkle size={16}/>} onClick={onAnalyze}>분석 시작</CTAButton>
      </div>
    </div>
  );
}

function Analyzing({ steps, currentStep }) {
  return (
    <div style={{
      flex: 1, display: 'flex', flexDirection: 'column',
      padding: '0 20px 32px', overflow: 'hidden',
    }}>
      <div style={{ padding: '4px 0 20px' }}>
        <div className="serif" style={{
          fontSize: 22, fontWeight: 500, color: 'var(--text)',
          letterSpacing: '-0.01em', lineHeight: 1.35,
        }}>
          AI가 컬러를<br/>분석하고 있어요
        </div>
        <div style={{ marginTop: 8, fontSize: 12, color: 'var(--text-mid)' }}>
          잠시만 기다려주세요 · 약 18초
        </div>
      </div>

      {/* Scanning visualization */}
      <div style={{
        width: '100%', aspectRatio: '4/5',
        borderRadius: 24, overflow: 'hidden', position: 'relative',
        background: 'linear-gradient(160deg, #F4D5C2 0%, #E8C5D5 100%)',
        display: 'flex', alignItems: 'center', justifyContent: 'center',
      }}>
        <FacePlaceholder size={220}/>

        {/* Scan line */}
        <div className="scan-line" style={{
          position: 'absolute', left: '8%', right: '8%', height: 2,
          background: 'linear-gradient(90deg, transparent, var(--primary-deep), transparent)',
          boxShadow: '0 0 16px rgba(216, 126, 146, 0.7)',
          borderRadius: 2,
        }}/>

        {/* Sample point chips */}
        {SAMPLE_POINTS.map((p, i) => (
          <div key={i} style={{
            position: 'absolute', left: `${p.x}%`, top: `${p.y}%`,
            transform: 'translate(-50%, -50%)',
            width: 14, height: 14, borderRadius: '50%',
            background: p.color, border: '2px solid #fff',
            boxShadow: '0 2px 6px rgba(0,0,0,0.2)',
            opacity: currentStep >= 1 ? 1 : 0,
            transition: `opacity 0.4s ease ${i * 0.1}s`,
          }}/>
        ))}

        {/* corner brackets */}
        {['tl','tr','bl','br'].map(c => (
          <div key={c} style={{
            position: 'absolute',
            ...(c.startsWith('t') ? { top: 16 } : { bottom: 16 }),
            ...(c.endsWith('l') ? { left: 16 } : { right: 16 }),
            width: 28, height: 28,
            borderTop: c.startsWith('t') ? '2px solid rgba(255,255,255,0.8)' : 'none',
            borderBottom: c.startsWith('b') ? '2px solid rgba(255,255,255,0.8)' : 'none',
            borderLeft: c.endsWith('l') ? '2px solid rgba(255,255,255,0.8)' : 'none',
            borderRight: c.endsWith('r') ? '2px solid rgba(255,255,255,0.8)' : 'none',
            borderRadius: 4,
          }}/>
        ))}
      </div>

      {/* Step list */}
      <div style={{ marginTop: 20, display: 'flex', flexDirection: 'column', gap: 8 }}>
        {steps.map((s, i) => {
          const done = i < currentStep;
          const active = i === currentStep;
          const pending = i > currentStep;
          const Ico = s.icon;
          return (
            <div key={i} style={{
              display: 'flex', alignItems: 'center', gap: 12,
              padding: '10px 14px', borderRadius: 12,
              background: active ? 'var(--primary-soft)' : 'transparent',
              border: active ? '1px solid var(--primary)' : '1px solid transparent',
              opacity: pending ? 0.45 : 1,
              transition: 'all 0.3s ease',
            }}>
              <div style={{
                width: 32, height: 32, borderRadius: '50%',
                background: done ? '#2DB88A' : active ? 'var(--primary-deep)' : 'var(--line-soft)',
                color: done || active ? '#fff' : 'var(--text-dim)',
                display: 'flex', alignItems: 'center', justifyContent: 'center',
                flexShrink: 0,
              }}>
                {done ? <IconCheck size={16} sw={2.5}/>
                  : active ? <div className="spin" style={{ width: 14, height: 14, border: '2px solid rgba(255,255,255,0.3)', borderTopColor: '#fff', borderRadius: '50%' }}/>
                  : <Ico size={14}/>}
              </div>
              <div style={{ flex: 1, minWidth: 0 }}>
                <div style={{ fontSize: 13, fontWeight: active ? 700 : 600, color: 'var(--text)' }}>
                  {s.label}
                </div>
                <div style={{ fontSize: 11, color: 'var(--text-mid)', marginTop: 1 }}>
                  {s.sub}
                </div>
              </div>
              {active && (
                <div style={{ fontSize: 10, color: 'var(--primary-deep)', fontWeight: 700, fontFamily: 'JetBrains Mono, monospace' }}>
                  · · ·
                </div>
              )}
            </div>
          );
        })}
      </div>
    </div>
  );
}

const TIPS = [
  '자연광 아래에서 촬영한 정면 사진을 권장해요',
  '메이크업이 없는 사진일수록 정확도가 높아져요',
  '머리카락이 얼굴을 가리지 않도록 해주세요',
];

const SAMPLE_POINTS = [
  { x: 50, y: 38, color: '#E8B89E' },
  { x: 42, y: 48, color: '#D49F8A' },
  { x: 58, y: 48, color: '#D49F8A' },
  { x: 50, y: 58, color: '#C28572' },
  { x: 35, y: 65, color: '#E8B89E' },
  { x: 65, y: 65, color: '#E8B89E' },
];

window.DiagnosisScreen = DiagnosisScreen;
