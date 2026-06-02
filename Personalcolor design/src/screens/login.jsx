// Login screen — OliveMe brand intro + Kakao login mock
function LoginScreen({ onLogin }) {
  return (
    <div data-screen-label="01 Login" style={{
      height: '100%', position: 'relative', overflow: 'hidden',
      background: 'linear-gradient(160deg, #FDF4F0 0%, #F5EDF8 50%, #FDEFE4 100%)',
      display: 'flex', flexDirection: 'column',
    }}>
      {/* Decorative blobs */}
      <Blob color="rgba(242, 166, 181, 0.45)" size={280} top={-80} right={-60} delay={0}/>
      <Blob color="rgba(201, 184, 232, 0.45)" size={260} bottom={120} left={-100} delay={3}/>
      <Blob color="rgba(212, 165, 116, 0.3)" size={180} top={250} right={-40} delay={6}/>

      {/* Olive leaves decorative SVG */}
      <svg style={{ position: 'absolute', top: 80, left: 20, opacity: 0.18 }} width="80" height="80" viewBox="0 0 80 80">
        <ellipse cx="40" cy="40" rx="8" ry="20" transform="rotate(-30 40 40)" fill="none" stroke="var(--primary-deep)" strokeWidth="1"/>
        <ellipse cx="50" cy="35" rx="6" ry="14" transform="rotate(20 50 35)" fill="none" stroke="var(--primary-deep)" strokeWidth="1"/>
      </svg>

      {/* Hero */}
      <div style={{
        flex: 1, display: 'flex', flexDirection: 'column',
        alignItems: 'center', justifyContent: 'center',
        padding: '0 32px', position: 'relative', zIndex: 1,
      }}>
        <OliveMeLogo variant="full" size={300} />

        <div style={{ marginTop: 12, textAlign: 'center' }} className="fade-up">
          <div style={{
            fontSize: 12, color: 'var(--text-mid)',
            lineHeight: 1.7, fontWeight: 400,
          }}>
            얼굴 사진 한 장으로 퍼스널 컬러를 진단하고<br/>
            나에게 어울리는 옷·메이크업·매장까지
          </div>
        </div>
      </div>

      {/* Login bottom sheet */}
      <div style={{
        padding: '24px 24px 40px',
        background: 'rgba(255, 255, 255, 0.7)',
        backdropFilter: 'blur(20px)',
        WebkitBackdropFilter: 'blur(20px)',
        borderTopLeftRadius: 28, borderTopRightRadius: 28,
        position: 'relative', zIndex: 2,
        borderTop: '1px solid rgba(255,255,255,0.8)',
      }}>
        <button onClick={onLogin} className="tap" style={{
          width: '100%', padding: '15px',
          background: '#FEE500', color: '#181600',
          borderRadius: 12, border: 'none',
          fontSize: 15, fontWeight: 700,
          display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 10,
          boxShadow: '0 4px 12px rgba(254, 229, 0, 0.4)',
          letterSpacing: '-0.01em',
        }}>
          {/* Generic chat bubble icon — not the actual Kakao logo */}
          <svg width="20" height="20" viewBox="0 0 24 24" fill="#181600">
            <path d="M12 3C6.5 3 2 6.5 2 11c0 2.7 1.6 5 4.2 6.5l-.8 3 3.4-2c1 .2 2.1.3 3.2.3 5.5 0 10-3.5 10-8s-4.5-7.8-10-7.8z"/>
          </svg>
          카카오로 시작하기
        </button>

        <button onClick={onLogin} className="tap" style={{
          width: '100%', marginTop: 10, padding: '15px',
          background: '#fff', color: 'var(--text)',
          borderRadius: 12, border: '1px solid var(--line)',
          fontSize: 14, fontWeight: 500,
        }}>
          이메일로 둘러보기
        </button>

        <div style={{
          marginTop: 18, fontSize: 11, color: 'var(--text-dim)',
          textAlign: 'center', lineHeight: 1.6,
        }}>
          가입 시 <span style={{ textDecoration: 'underline' }}>이용약관</span> 및{' '}
          <span style={{ textDecoration: 'underline' }}>개인정보처리방침</span>에<br/>
          동의하는 것으로 간주됩니다
        </div>
      </div>
    </div>
  );
}

window.LoginScreen = LoginScreen;
