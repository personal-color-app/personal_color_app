// Shared UI primitives for OliveMe

const { useState, useEffect, useRef, useMemo } = React;

// ─────────────────────────────────────────────────────────────
// OliveMe logo — uses uploaded brand asset
// variant: 'full' (face + wordmark + tagline) | 'mark' (face only) | 'inline' (mark + wordmark side by side)
// ─────────────────────────────────────────────────────────────
function OliveMeLogo({ size = 36, variant = 'inline', tagline = false }) {
  const R = (typeof window !== 'undefined' && window.__resources) || {};
  const LOGO = R.oliveLogo || 'assets/oliveme-logo.png';
  const MARK = R.oliveMark || 'assets/oliveme-mark.png';
  if (variant === 'full') {
    // Hero use — full uploaded logo as a single image (transparent bg)
    return (
      <img src={LOGO} alt="OliveMe"
        style={{
          width: size, height: 'auto', display: 'block',
          // soft blend with background using multiply
          mixBlendMode: 'multiply',
        }} />
    );
  }
  if (variant === 'mark') {
    return (
      <img src={MARK} alt="OliveMe"
        style={{ width: size, height: size, display: 'block', mixBlendMode: 'multiply' }}/>
    );
  }
  // inline — small mark + serif wordmark beside it
  return (
    <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 4 }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
        <img src={MARK} alt=""
          style={{ width: size * 1.1, height: size * 1.1, display: 'block', mixBlendMode: 'multiply' }}/>
        <span className="serif" style={{
          fontSize: size, fontWeight: 400, color: '#8B6B6F', letterSpacing: '0.01em',
          lineHeight: 1,
        }}>Olive<span style={{ fontWeight: 500 }}>Me</span></span>
      </div>
      {tagline && (
        <div style={{
          fontSize: 9, color: '#A89094',
          letterSpacing: '0.32em', textTransform: 'uppercase', fontWeight: 500,
        }}>Discover Your Best Colors</div>
      )}
    </div>
  );
}

// ─────────────────────────────────────────────────────────────
// Top app bar (used in all post-login screens)
// ─────────────────────────────────────────────────────────────
function AppBar({ title, leading, trailing, transparent = false, dense = false, subtitle, dark = false }) {
  const bg = transparent ? 'transparent' : 'var(--bg)';
  const fg = dark ? '#fff' : 'var(--text)';
  return (
    <div style={{
      background: bg,
      padding: dense ? '8px 4px' : '12px 4px 8px',
      display: 'flex', alignItems: 'center', minHeight: 56, gap: 4,
    }}>
      <div style={{ width: 48, height: 48, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
        {leading}
      </div>
      <div style={{ flex: 1, minWidth: 0 }}>
        {title && (
          <div style={{
            fontSize: 17, fontWeight: 600, color: fg, lineHeight: 1.2,
            letterSpacing: '-0.01em',
          }}>{title}</div>
        )}
        {subtitle && (
          <div style={{ fontSize: 11, color: dark ? 'rgba(255,255,255,0.7)' : 'var(--text-dim)', marginTop: 2 }}>
            {subtitle}
          </div>
        )}
      </div>
      <div style={{ display: 'flex', alignItems: 'center', gap: 2 }}>
        {trailing}
      </div>
    </div>
  );
}

function IconButton({ children, onClick, size = 44, color = 'var(--text)' }) {
  return (
    <button onClick={onClick} className="tap" style={{
      width: size, height: size, borderRadius: '50%',
      display: 'flex', alignItems: 'center', justifyContent: 'center',
      color,
    }}>{children}</button>
  );
}

// ─────────────────────────────────────────────────────────────
// Pill / chip
// ─────────────────────────────────────────────────────────────
function Pill({ children, color = 'var(--primary)', bg = 'var(--primary-soft)', dot }) {
  return (
    <span style={{
      display: 'inline-flex', alignItems: 'center', gap: 6,
      padding: '5px 10px', borderRadius: 100,
      background: bg, color,
      fontSize: 11, fontWeight: 600, letterSpacing: '0.02em',
      whiteSpace: 'nowrap',
    }}>
      {dot && <span style={{ width: 6, height: 6, borderRadius: '50%', background: color }} />}
      {children}
    </span>
  );
}

// ─────────────────────────────────────────────────────────────
// Card
// ─────────────────────────────────────────────────────────────
function Card({ children, style, onClick, noPad = false }) {
  return (
    <div onClick={onClick} className={onClick ? 'tap' : ''} style={{
      background: 'var(--card)',
      borderRadius: 20,
      padding: noPad ? 0 : 18,
      boxShadow: 'var(--shadow-sm)',
      border: '1px solid var(--line-soft)',
      ...style,
    }}>{children}</div>
  );
}

// ─────────────────────────────────────────────────────────────
// Big CTA button
// ─────────────────────────────────────────────────────────────
function CTAButton({ children, onClick, variant = 'primary', icon, full = true, disabled = false }) {
  const v = {
    primary: { bg: 'linear-gradient(135deg, var(--primary) 0%, var(--primary-deep) 100%)', color: '#fff' },
    secondary: { bg: 'var(--secondary-soft)', color: 'var(--text)' },
    outline: { bg: 'transparent', color: 'var(--text)', border: '1.5px solid var(--line)' },
    dark: { bg: 'var(--text)', color: '#fff' },
    kakao: { bg: '#FEE500', color: '#191919' },
  }[variant];
  return (
    <button onClick={disabled ? null : onClick} className="tap" style={{
      width: full ? '100%' : 'auto',
      padding: '15px 22px',
      borderRadius: 14,
      background: v.bg,
      color: v.color,
      border: v.border || 'none',
      fontSize: 15, fontWeight: 600,
      display: 'inline-flex', alignItems: 'center', justifyContent: 'center', gap: 8,
      letterSpacing: '-0.01em',
      opacity: disabled ? 0.5 : 1,
      boxShadow: variant === 'primary' ? '0 6px 16px rgba(216, 126, 146, 0.32)' : 'none',
    }}>
      {icon}{children}
    </button>
  );
}

// ─────────────────────────────────────────────────────────────
// Color swatch (round, named)
// ─────────────────────────────────────────────────────────────
function Swatch({ color, name, hex, size = 72, showCode = false }) {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 8 }}>
      <div className="swatch-ring" style={{
        width: size, height: size, background: color, borderRadius: '50%',
      }}/>
      <div style={{ textAlign: 'center' }}>
        <div style={{ fontSize: 12, fontWeight: 600, color: 'var(--text)' }}>{name}</div>
        {showCode && (
          <div style={{ fontSize: 10, color: 'var(--text-dim)', fontFamily: 'JetBrains Mono, monospace', marginTop: 2 }}>
            {hex}
          </div>
        )}
      </div>
    </div>
  );
}

// ─────────────────────────────────────────────────────────────
// Decorative pastel blob (for backgrounds)
// ─────────────────────────────────────────────────────────────
function Blob({ color, size = 200, top, left, right, bottom, opacity = 0.5, delay = 0 }) {
  return (
    <div className="blob-float" style={{
      position: 'absolute',
      width: size, height: size,
      borderRadius: '50%',
      background: `radial-gradient(circle at 35% 35%, ${color}, transparent 70%)`,
      opacity,
      filter: 'blur(20px)',
      pointerEvents: 'none',
      top, left, right, bottom,
      animationDelay: `${delay}s`,
    }} />
  );
}

// ─────────────────────────────────────────────────────────────
// Persona avatar (placeholder gradient with initials)
// ─────────────────────────────────────────────────────────────
function Avatar({ name = 'M', size = 40, bg = 'linear-gradient(135deg, var(--primary), var(--secondary))' }) {
  return (
    <div style={{
      width: size, height: size, borderRadius: '50%',
      background: bg,
      display: 'flex', alignItems: 'center', justifyContent: 'center',
      color: '#fff', fontWeight: 600, fontSize: size * 0.4,
      letterSpacing: '-0.02em',
      boxShadow: '0 4px 12px rgba(216, 126, 146, 0.25)',
    }}>{name.charAt(0)}</div>
  );
}

// ─────────────────────────────────────────────────────────────
// Face photo placeholder (sketched silhouette)
// ─────────────────────────────────────────────────────────────
function FacePlaceholder({ size = 220 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 220 220">
      <defs>
        <linearGradient id="facegrad" x1="0" y1="0" x2="1" y2="1">
          <stop offset="0%" stopColor="#FCE2E8" />
          <stop offset="100%" stopColor="#ECE4F8" />
        </linearGradient>
      </defs>
      <circle cx="110" cy="110" r="105" fill="url(#facegrad)" />
      <ellipse cx="110" cy="95" rx="40" ry="50" fill="none" stroke="rgba(216,126,146,0.5)" strokeWidth="1.5" strokeDasharray="4 4"/>
      <circle cx="95" cy="88" r="3" fill="rgba(216,126,146,0.5)"/>
      <circle cx="125" cy="88" r="3" fill="rgba(216,126,146,0.5)"/>
      <path d="M95 115 Q110 122 125 115" stroke="rgba(216,126,146,0.5)" strokeWidth="1.5" fill="none" strokeLinecap="round"/>
      <path d="M70 165 Q110 180 150 165" stroke="rgba(216,126,146,0.3)" strokeWidth="1" fill="none"/>
    </svg>
  );
}

// ─────────────────────────────────────────────────────────────
// Garment / lipstick placeholder tile
// ─────────────────────────────────────────────────────────────
function PlaceholderTile({ kind = 'garment', color, label }) {
  if (kind === 'garment') {
    return (
      <svg viewBox="0 0 100 120" style={{ width: '100%', height: '100%', display: 'block' }}>
        <path d="M20 25 L40 15 Q50 25 60 15 L80 25 L85 45 L75 50 L75 110 L25 110 L25 50 L15 45 Z"
          fill={color} stroke="rgba(0,0,0,0.08)" strokeWidth="0.5"/>
        <path d="M40 15 Q50 30 60 15" fill="none" stroke="rgba(0,0,0,0.12)" strokeWidth="0.5"/>
      </svg>
    );
  }
  if (kind === 'lipstick') {
    return (
      <svg viewBox="0 0 100 120" style={{ width: '100%', height: '100%', display: 'block' }}>
        <rect x="35" y="55" width="30" height="55" rx="2" fill="#3D3137"/>
        <rect x="35" y="50" width="30" height="8" fill="#5a474f"/>
        <path d="M40 50 L40 25 Q40 18 50 14 Q60 18 60 25 L60 50 Z" fill={color}/>
        <path d="M40 25 Q50 22 60 25" stroke="rgba(255,255,255,0.4)" strokeWidth="0.5" fill="none"/>
      </svg>
    );
  }
  if (kind === 'cushion') {
    return (
      <svg viewBox="0 0 100 120" style={{ width: '100%', height: '100%', display: 'block' }}>
        <rect x="15" y="35" width="70" height="55" rx="6" fill="#3D3137"/>
        <ellipse cx="50" cy="62" rx="26" ry="20" fill={color}/>
        <ellipse cx="44" cy="56" rx="4" ry="3" fill="rgba(255,255,255,0.3)"/>
      </svg>
    );
  }
  if (kind === 'eyeshadow') {
    return (
      <svg viewBox="0 0 100 120" style={{ width: '100%', height: '100%', display: 'block' }}>
        <rect x="15" y="30" width="70" height="60" rx="6" fill="#2a2227"/>
        <circle cx="35" cy="55" r="10" fill={color}/>
        <circle cx="65" cy="55" r="10" fill={color} opacity="0.7"/>
        <circle cx="35" cy="78" r="10" fill={color} opacity="0.85"/>
        <circle cx="65" cy="78" r="10" fill={color} opacity="0.55"/>
      </svg>
    );
  }
}

Object.assign(window, {
  OliveMeLogo, AppBar, IconButton, Pill, Card, CTAButton, Swatch, Blob, Avatar,
  FacePlaceholder, PlaceholderTile,
});
