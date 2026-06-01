// Minimal stroke icon set — outline style, fits beauty/soft aesthetic
const Icon = ({ d, size = 24, stroke = 'currentColor', fill = 'none', sw = 1.6, children, vb = '0 0 24 24' }) => (
  <svg width={size} height={size} viewBox={vb} fill={fill} stroke={stroke}
       strokeWidth={sw} strokeLinecap="round" strokeLinejoin="round">
    {d ? <path d={d} /> : children}
  </svg>
);

const IconMenu = (p) => <Icon {...p} d="M3 6h18M3 12h18M3 18h18" />;
const IconBell = (p) => <Icon {...p}>
  <path d="M18 8a6 6 0 1 0-12 0c0 7-3 9-3 9h18s-3-2-3-9" />
  <path d="M13.73 21a2 2 0 0 1-3.46 0" />
</Icon>;
const IconBack = (p) => <Icon {...p} d="M19 12H5M12 19l-7-7 7-7" />;
const IconClose = (p) => <Icon {...p} d="M18 6 6 18M6 6l12 12" />;
const IconCamera = (p) => <Icon {...p}>
  <path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z"/>
  <circle cx="12" cy="13" r="4"/>
</Icon>;
const IconGallery = (p) => <Icon {...p}>
  <rect x="3" y="3" width="18" height="18" rx="2"/>
  <circle cx="9" cy="9" r="2"/>
  <path d="m21 15-5-5L5 21"/>
</Icon>;
const IconMap = (p) => <Icon {...p}>
  <path d="M1 6v15l7-3 8 3 7-3V3l-7 3-8-3-7 3z"/>
  <path d="M8 3v15M16 6v15"/>
</Icon>;
const IconPin = (p) => <Icon {...p}>
  <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/>
  <circle cx="12" cy="10" r="3"/>
</Icon>;
const IconUser = (p) => <Icon {...p}>
  <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
  <circle cx="12" cy="7" r="4"/>
</Icon>;
const IconHeart = (p) => <Icon {...p} d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>;
const IconStar = (p) => <Icon {...p} d="M12 2 15.09 8.26 22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/>;
const IconDownload = (p) => <Icon {...p}>
  <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
  <path d="M7 10l5 5 5-5M12 15V3"/>
</Icon>;
const IconShare = (p) => <Icon {...p}>
  <circle cx="18" cy="5" r="3"/><circle cx="6" cy="12" r="3"/><circle cx="18" cy="19" r="3"/>
  <path d="m8.59 13.51 6.83 3.98M15.41 6.51l-6.82 3.98"/>
</Icon>;
const IconArrow = (p) => <Icon {...p} d="M5 12h14M12 5l7 7-7 7"/>;
const IconCheck = (p) => <Icon {...p} d="m5 12 5 5L20 7"/>;
const IconSparkle = (p) => <Icon {...p}>
  <path d="M12 3v3M12 18v3M3 12h3M18 12h3M5.6 5.6l2.1 2.1M16.3 16.3l2.1 2.1M5.6 18.4l2.1-2.1M16.3 7.7l2.1-2.1"/>
  <circle cx="12" cy="12" r="2"/>
</Icon>;
const IconSearch = (p) => <Icon {...p}>
  <circle cx="11" cy="11" r="7"/><path d="m21 21-4.35-4.35"/>
</Icon>;
const IconClock = (p) => <Icon {...p}>
  <circle cx="12" cy="12" r="10"/><path d="M12 6v6l4 2"/>
</Icon>;
const IconChevron = (p) => <Icon {...p} d="m9 18 6-6-6-6"/>;
const IconHome = (p) => <Icon {...p} d="M3 12 12 3l9 9M5 10v10h14V10"/>;
const IconLayers = (p) => <Icon {...p}>
  <path d="m12 2 10 6-10 6L2 8l10-6z"/>
  <path d="m2 17 10 6 10-6M2 12l10 6 10-6"/>
</Icon>;
const IconLocate = (p) => <Icon {...p}>
  <circle cx="12" cy="12" r="3"/>
  <path d="M12 2v3M12 19v3M2 12h3M19 12h3"/>
</Icon>;
const IconPlus = (p) => <Icon {...p} d="M12 5v14M5 12h14"/>;
const IconRefresh = (p) => <Icon {...p}>
  <path d="M3 12a9 9 0 0 1 15-6.7L21 8M21 3v5h-5M21 12a9 9 0 0 1-15 6.7L3 16M3 21v-5h5"/>
</Icon>;
const IconChat = (p) => <Icon {...p} d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>;
const IconSettings = (p) => <Icon {...p}>
  <circle cx="12" cy="12" r="3"/>
  <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 1 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 1 1-4 0v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 1 1-2.83-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 1 1 0-4h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 1 1 2.83-2.83l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 1 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 1 1 2.83 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 1 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z"/>
</Icon>;
const IconLogout = (p) => <Icon {...p}>
  <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4M16 17l5-5-5-5M21 12H9"/>
</Icon>;
const IconBookmark = (p) => <Icon {...p} d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z"/>;
const IconPalette = (p) => <Icon {...p}>
  <path d="M12 22a10 10 0 1 1 0-20c5.5 0 10 4 10 9 0 3-2.5 5-5 5h-1.5a1.5 1.5 0 0 0-1.5 1.5 1.5 1.5 0 0 0 1.5 1.5 1.5 1.5 0 0 1 0 3z"/>
  <circle cx="7.5" cy="10.5" r="1" fill="currentColor"/>
  <circle cx="12" cy="7.5" r="1" fill="currentColor"/>
  <circle cx="16.5" cy="10.5" r="1" fill="currentColor"/>
</Icon>;
const IconShirt = (p) => <Icon {...p} d="M20.38 3.46 16 2a4 4 0 0 1-8 0L3.62 3.46a2 2 0 0 0-1.34 2.23l.58 3.47a1 1 0 0 0 .99.84H6v10c0 1.1.9 2 2 2h8a2 2 0 0 0 2-2V10h2.15a1 1 0 0 0 .99-.84l.58-3.47a2 2 0 0 0-1.34-2.23z"/>;
const IconLipstick = (p) => <Icon {...p}>
  <path d="M8 21h8v-9H8z"/>
  <path d="M9 12V8l3-5 3 5v4"/>
</Icon>;
const IconList = (p) => <Icon {...p} d="M8 6h13M8 12h13M8 18h13M3 6h.01M3 12h.01M3 18h.01"/>;

Object.assign(window, {
  Icon, IconMenu, IconBell, IconBack, IconClose, IconCamera, IconGallery,
  IconMap, IconPin, IconUser, IconHeart, IconStar, IconDownload, IconShare,
  IconArrow, IconCheck, IconSparkle, IconSearch, IconClock, IconChevron,
  IconHome, IconLayers, IconLocate, IconPlus, IconRefresh, IconChat,
  IconSettings, IconLogout, IconBookmark, IconPalette, IconShirt, IconLipstick,
  IconList,
});
