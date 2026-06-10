---
name: Harvest Circle Dark
colors:
  surface: '#121410'
  surface-dim: '#121410'
  surface-bright: '#383a35'
  surface-container-lowest: '#0d0f0b'
  surface-container-low: '#1a1c18'
  surface-container: '#1e201c'
  surface-container-high: '#292b26'
  surface-container-highest: '#333531'
  on-surface: '#e3e3dc'
  on-surface-variant: '#c7c8b3'
  inverse-surface: '#e3e3dc'
  inverse-on-surface: '#2f312d'
  outline: '#90927f'
  outline-variant: '#464838'
  surface-tint: '#bcd05e'
  primary: '#f3ffb8'
  on-primary: '#2b3400'
  primary-container: '#d1e671'
  on-primary-container: '#576700'
  inverse-primary: '#566500'
  secondary: '#b0cfad'
  on-secondary: '#1d361e'
  secondary-container: '#334d33'
  on-secondary-container: '#9fbd9c'
  tertiary: '#f3fbea'
  on-tertiary: '#2c3227'
  tertiary-container: '#d7dece'
  on-tertiary-container: '#5b6255'
  error: '#ffb4ab'
  on-error: '#690005'
  error-container: '#93000a'
  on-error-container: '#ffdad6'
  primary-fixed: '#d8ed77'
  primary-fixed-dim: '#bcd05e'
  on-primary-fixed: '#181e00'
  on-primary-fixed-variant: '#404c00'
  secondary-fixed: '#ccebc7'
  secondary-fixed-dim: '#b0cfad'
  on-secondary-fixed: '#07200b'
  on-secondary-fixed-variant: '#334d33'
  tertiary-fixed: '#dee5d5'
  tertiary-fixed-dim: '#c2c9b9'
  on-tertiary-fixed: '#171d13'
  on-tertiary-fixed-variant: '#42493d'
  background: '#121410'
  on-background: '#e3e3dc'
  surface-variant: '#333531'
typography:
  display-lg:
    fontFamily: Inter
    fontSize: 57px
    fontWeight: '700'
    lineHeight: 64px
    letterSpacing: -0.25px
  headline-lg:
    fontFamily: Inter
    fontSize: 32px
    fontWeight: '600'
    lineHeight: 40px
    letterSpacing: 0px
  headline-lg-mobile:
    fontFamily: Inter
    fontSize: 28px
    fontWeight: '600'
    lineHeight: 36px
    letterSpacing: 0px
  title-lg:
    fontFamily: Inter
    fontSize: 22px
    fontWeight: '500'
    lineHeight: 28px
    letterSpacing: 0px
  body-lg:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
    letterSpacing: 0.5px
  body-md:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
    letterSpacing: 0.25px
  label-lg:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '500'
    lineHeight: 20px
    letterSpacing: 0.1px
  label-sm:
    fontFamily: Inter
    fontSize: 11px
    fontWeight: '500'
    lineHeight: 16px
    letterSpacing: 0.5px
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  base: 8px
  xs: 4px
  sm: 8px
  md: 16px
  lg: 24px
  xl: 32px
  2xl: 48px
  gutter: 16px
  margin-mobile: 16px
  margin-desktop: 32px
---

## Brand & Style
This design system translates the organic, community-focused essence of agriculture into a high-performance digital environment. The brand personality is grounded yet tech-forward, aimed at modern growers and sustainable food advocates who require clarity in low-light environments. 

The design style is **Modern Corporate** with **Tonal Layering**. It prioritizes legibility and functional aesthetics, using deep organic greens as a canvas for high-energy accents. The emotional response is one of stability, precision, and growth, avoiding the harshness of pure black in favor of a sophisticated deep charcoal-green that feels more natural and less fatiguing.

## Colors
The palette is rooted in a deep charcoal-green (`#1A1C18`), providing a rich, organic base that reduces glare. 

- **Primary (Vibrant Lime):** Used for key actions and progress indicators. It provides maximum contrast against the dark background.
- **Secondary (Soft Sage):** Used for secondary UI elements, chips, and grouped content categories.
- **Surface Strategy:** We use a tonal hierarchy rather than pure black. Surfaces "lifted" toward the user become slightly lighter (`#242822`) to indicate proximity and importance.
- **Accessibility:** All text roles are mapped to ensure a minimum contrast ratio of 4.5:1. Primary text uses a high-weighted off-white to prevent "halation" (the glowing effect of white text on dark backgrounds).

## Typography
The design system utilizes **Inter** exclusively to leverage its exceptional legibility and systematic weight distribution. 

- **Headlines:** Use Semi-Bold (600) weights to provide clear structural hierarchy. Letter spacing is tightened slightly on large display sizes to maintain tension.
- **Body:** Standardized at 16px for primary reading to ensure comfort on mobile devices.
- **Labels:** Used for navigation items and small metadata, utilizing Medium (500) weights to ensure clarity even at 11px.
- **Rendering:** On dark surfaces, font-smoothing should be set to `antialiased` to prevent the typeface from appearing too heavy.

## Layout & Spacing
The layout follows an **8px grid system**, ensuring all dimensions, padding, and margins are multiples of 8.

- **Desktop:** A 12-column fluid grid with 24px gutters. Max content width is capped at 1440px to ensure line lengths remain readable.
- **Tablet:** An 8-column grid with 16px gutters.
- **Mobile:** A 4-column grid with 16px margins. 
- **Philosophy:** Content should feel spacious. Use "Negative Space" as a functional tool to separate distinct data sets without the need for heavy dividers.

## Elevation & Depth
In this dark mode environment, depth is communicated through **Tonal Elevation** and **Subtle Inner Borders**.

- **Level 0 (Background):** The base layer at `#1A1C18`.
- **Level 1 (Cards/Navigation):** Surfaces at `#242822`. These should feature a 1px subtle stroke of `#42493D` to define edges where shadows might be invisible.
- **Level 2 (Modals/Menus):** Surfaces at `#2F322B`. 
- **Shadows:** Use large, soft ambient shadows (`blur: 24px, opacity: 0.4`) with a slight green tint (`#000000`) to create a sense of floating without looking "muddy."

## Shapes
The shape language is defined by **Round Eight** logic, providing a friendly and approachable feel that mimics organic forms.

- **Standard Elements:** Buttons, input fields, and small cards use a 0.5rem (8px) radius.
- **Large Elements:** Featured sections and containers use a 1rem (16px) radius.
- **Selection Controls:** Checkboxes use a 4px radius, while radio buttons remain fully circular.

## Components
- **Buttons:** Primary buttons use the Vibrant Lime background with black text for maximum punch. Ghost buttons use the Soft Sage stroke with sage text.
- **Input Fields:** Containers use a dark fill (`#242822`) with a subtle bottom-border or full outline in `#42493D`. The cursor and active state focus ring must use the Vibrant Lime.
- **Chips:** Small, rounded-pill shapes using the Secondary Sage color at 15% opacity for the background and 100% opacity for the text.
- **Lists:** Items are separated by subtle `1px` lines in `#42493D`. Interactive list items should have a hover state that lightens the background to `#2F322B`.
- **Cards:** Cards should be borderless but utilize the Level 1 surface color to distinguish themselves from the background.
- **Progress Bars:** Use the Vibrant Lime for the active "fill" to signify life and completion.