# LeagueHQ - Design System

## Overview
Design tokens and Tailwind CSS configuration for consistent UI across the application.

**Purpose:** Single source of truth for colors, typography, spacing, and design decisions
**Framework:** Tailwind CSS (utility-first approach)

---

## Color Palette

### Primary Colors
```js
// Indigo - Primary brand color
indigo: {
  50:  '#eef2ff',
  100: '#e0e7ff',
  200: '#c7d2fe',
  300: '#a5b4fc',
  400: '#818cf8',
  500: '#6366f1',   // Default primary
  600: '#4f46e5',   // Primary buttons, links
  700: '#4338ca',   // Hover states
  800: '#3730a3',
  900: '#312e81',
}
```

**Usage:**
- Primary buttons: `bg-indigo-600 hover:bg-indigo-700`
- Links: `text-indigo-600 hover:text-indigo-700`
- Focus rings: `focus:ring-indigo-500`

### Neutral Colors
```js
// Gray - Text, borders, backgrounds
gray: {
  50:  '#f9fafb',   // Light backgrounds
  100: '#f3f4f6',   // Disabled state backgrounds
  200: '#e5e7eb',   // Borders, dividers
  300: '#d1d5db',   // Input borders
  400: '#9ca3af',   // Placeholder text
  500: '#6b7280',   // Secondary text
  600: '#4b5563',   // Body text
  700: '#374151',   // Headings (dark mode safe)
  800: '#1f2937',
  900: '#111827',   // Maximum contrast
}
```

**Usage:**
- Headings: `text-gray-900`
- Body text: `text-gray-700`
- Secondary text: `text-gray-500`
- Borders: `border-gray-300`
- Backgrounds: `bg-gray-50`

### Semantic Colors

**Success (Green)**
```js
green: {
  50:  '#f0fdf4',
  100: '#dcfce7',
  600: '#16a34a',   // Success states
  700: '#15803d',   // Hover
  800: '#166534',
}
```

**Error (Red)**
```js
red: {
  50:  '#fef2f2',
  100: '#fee2e2',
  600: '#dc2626',   // Error states
  700: '#b91c1c',   // Hover
  800: '#991b1b',
}
```

**Warning (Yellow)**
```js
yellow: {
  50:  '#fefce8',
  100: '#fef9c3',
  600: '#ca8a04',   // Warning states
  700: '#a16207',
  800: '#854d0e',
}
```

**Info (Blue)**
```js
blue: {
  50:  '#eff6ff',
  100: '#dbeafe',
  600: '#2563eb',   // Info states
  700: '#1d4ed8',
  800: '#1e40af',
}
```

---

## Typography

### Font Family
```js
fontFamily: {
  sans: [
    'Inter',
    'system-ui',
    '-apple-system',
    'BlinkMacSystemFont',
    'Segoe UI',
    'Roboto',
    'Helvetica Neue',
    'Arial',
    'sans-serif',
  ],
}
```

**Setup:**
```html
<!-- In index.html -->
<link rel="preconnect" href="https://fonts.googleapis.com">
<link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
```

### Font Sizes
```js
fontSize: {
  xs:   ['0.75rem', { lineHeight: '1rem' }],      // 12px
  sm:   ['0.875rem', { lineHeight: '1.25rem' }],  // 14px
  base: ['1rem', { lineHeight: '1.5rem' }],       // 16px (default)
  lg:   ['1.125rem', { lineHeight: '1.75rem' }],  // 18px
  xl:   ['1.25rem', { lineHeight: '1.75rem' }],   // 20px
  '2xl': ['1.5rem', { lineHeight: '2rem' }],      // 24px
  '3xl': ['1.875rem', { lineHeight: '2.25rem' }], // 30px
  '4xl': ['2.25rem', { lineHeight: '2.5rem' }],   // 36px
}
```

**Usage:**
```tsx
// Page title
<h1 className="text-3xl font-bold text-gray-900">

// Section heading
<h2 className="text-2xl font-semibold text-gray-900">

// Card title
<h3 className="text-lg font-semibold text-gray-900">

// Body text
<p className="text-base text-gray-700">

// Small text / captions
<span className="text-sm text-gray-500">

// Labels
<label className="text-sm font-medium text-gray-700">
```

### Font Weights
```js
fontWeight: {
  normal:  400,  // Regular text
  medium:  500,  // Labels, card titles
  semibold: 600, // Headings, buttons
  bold:    700,  // Page titles, emphasis
}
```

---

## Spacing Scale

Uses Tailwind default spacing (4px base unit):

```js
spacing: {
  px: '1px',
  0:  '0',
  1:  '0.25rem',  // 4px
  2:  '0.5rem',   // 8px
  3:  '0.75rem',  // 12px
  4:  '1rem',     // 16px  <- Most common
  5:  '1.25rem',  // 20px
  6:  '1.5rem',   // 24px
  8:  '2rem',     // 32px
  10: '2.5rem',   // 40px
  12: '3rem',     // 48px
  16: '4rem',     // 64px
  20: '5rem',     // 80px
  24: '6rem',     // 96px
}
```

**Common Patterns:**
```tsx
// Card padding
className="p-6"              // 24px all sides

// Form input spacing
className="mt-1 block"       // 4px top margin

// Section spacing
className="space-y-6"        // 24px vertical gap

// Button padding
className="px-4 py-2"        // 16px horizontal, 8px vertical

// Page container
className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8"
```

---

## Border Radius

```js
borderRadius: {
  none: '0',
  sm:   '0.125rem',  // 2px
  DEFAULT: '0.25rem', // 4px
  md:   '0.375rem',  // 6px
  lg:   '0.5rem',    // 8px   <- Cards, modals
  xl:   '0.75rem',   // 12px
  '2xl': '1rem',     // 16px
  full: '9999px',    // Circles, pills
}
```

**Usage:**
```tsx
// Cards
className="rounded-lg"

// Buttons
className="rounded-md"

// Badges, pills
className="rounded-full"

// Avatars
className="rounded-full"

// Inputs
className="rounded-md"
```

---

## Shadows

```js
boxShadow: {
  sm:   '0 1px 2px 0 rgb(0 0 0 / 0.05)',
  DEFAULT: '0 1px 3px 0 rgb(0 0 0 / 0.1), 0 1px 2px -1px rgb(0 0 0 / 0.1)',
  md:   '0 4px 6px -1px rgb(0 0 0 / 0.1), 0 2px 4px -2px rgb(0 0 0 / 0.1)',
  lg:   '0 10px 15px -3px rgb(0 0 0 / 0.1), 0 4px 6px -4px rgb(0 0 0 / 0.1)',
  xl:   '0 20px 25px -5px rgb(0 0 0 / 0.1), 0 8px 10px -6px rgb(0 0 0 / 0.1)',
}
```

**Usage:**
```tsx
// Cards (default)
className="shadow"

// Cards on hover
className="shadow hover:shadow-lg transition-shadow"

// Modals, dropdowns
className="shadow-xl"

// Input focus (use ring instead)
className="focus:ring-2 focus:ring-indigo-500"
```

---

## Breakpoints (Responsive Design)

```js
screens: {
  sm: '640px',   // Tablet portrait
  md: '768px',   // Tablet landscape
  lg: '1024px',  // Desktop
  xl: '1280px',  // Large desktop
  '2xl': '1536px', // Extra large
}
```

**Mobile-First Approach:**
```tsx
// Base styles apply to mobile
// Add breakpoints for larger screens

<div className="
  grid
  grid-cols-1         /* Mobile: 1 column */
  md:grid-cols-2      /* Tablet: 2 columns */
  lg:grid-cols-3      /* Desktop: 3 columns */
  gap-4
">
```

**Common Patterns:**
```tsx
// Container max-width
className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8"

// Text size responsive
className="text-2xl sm:text-3xl lg:text-4xl"

// Hidden on mobile
className="hidden md:block"

// Show on mobile only
className="block md:hidden"

// Padding responsive
className="p-4 md:p-6 lg:p-8"
```

---

## Component Tokens

### Buttons

**Primary Button**
```tsx
const primaryButton = `
  bg-indigo-600 text-white
  px-4 py-2
  rounded-md
  font-medium
  hover:bg-indigo-700
  focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500
  disabled:opacity-50 disabled:cursor-not-allowed
  transition-colors
`
```

**Secondary Button**
```tsx
const secondaryButton = `
  bg-white text-gray-700
  border border-gray-300
  px-4 py-2
  rounded-md
  font-medium
  hover:bg-gray-50
  focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500
`
```

**Danger Button**
```tsx
const dangerButton = `
  bg-red-600 text-white
  px-4 py-2
  rounded-md
  font-medium
  hover:bg-red-700
  focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-red-500
`
```

### Form Inputs

**Text Input**
```tsx
const textInput = `
  mt-1 block w-full
  border border-gray-300
  rounded-md
  shadow-sm
  py-2 px-3
  text-gray-900
  placeholder-gray-400
  focus:outline-none focus:ring-indigo-500 focus:border-indigo-500
  sm:text-sm
`
```

**Input Error State**
```tsx
const inputError = `
  border-red-300
  text-red-900
  placeholder-red-300
  focus:ring-red-500 focus:border-red-500
`
```

### Cards

**Default Card**
```tsx
const card = `
  bg-white
  rounded-lg
  shadow
  p-6
`
```

**Interactive Card (clickable)**
```tsx
const interactiveCard = `
  bg-white
  rounded-lg
  shadow
  hover:shadow-lg
  transition-shadow
  p-6
  cursor-pointer
`
```

### Status Badges

```tsx
const badge = (variant: string) => {
  const variants = {
    gray:   'bg-gray-100 text-gray-800',
    blue:   'bg-blue-100 text-blue-800',
    green:  'bg-green-100 text-green-800',
    yellow: 'bg-yellow-100 text-yellow-800',
    red:    'bg-red-100 text-red-800',
    purple: 'bg-purple-100 text-purple-800',
  }

  return `
    px-2 py-1
    text-xs
    font-medium
    rounded-full
    ${variants[variant]}
  `
}
```

---

## Tailwind Configuration

### tailwind.config.js

```js
/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      fontFamily: {
        sans: [
          'Inter',
          'system-ui',
          '-apple-system',
          'BlinkMacSystemFont',
          'Segoe UI',
          'Roboto',
          'sans-serif',
        ],
      },
      colors: {
        // Add custom colors if needed (Tailwind defaults cover most)
      },
      // Custom animations (if needed)
      keyframes: {
        'fade-in': {
          '0%': { opacity: '0' },
          '100%': { opacity: '1' },
        },
        'slide-in': {
          '0%': { transform: 'translateY(-10px)', opacity: '0' },
          '100%': { transform: 'translateY(0)', opacity: '1' },
        },
      },
      animation: {
        'fade-in': 'fade-in 0.2s ease-out',
        'slide-in': 'slide-in 0.2s ease-out',
      },
    },
  },
  plugins: [
    require('@tailwindcss/forms'),  // Better form styles
  ],
}
```

### postcss.config.js

```js
export default {
  plugins: {
    tailwindcss: {},
    autoprefixer: {},
  },
}
```

### src/index.css

```css
@tailwind base;
@tailwind components;
@tailwind utilities;

/* Custom base styles */
@layer base {
  body {
    @apply font-sans text-gray-700 bg-gray-50;
  }

  h1, h2, h3, h4, h5, h6 {
    @apply text-gray-900;
  }
}

/* Custom component classes (optional) */
@layer components {
  .btn-primary {
    @apply bg-indigo-600 text-white px-4 py-2 rounded-md font-medium
           hover:bg-indigo-700 focus:outline-none focus:ring-2
           focus:ring-offset-2 focus:ring-indigo-500
           disabled:opacity-50 disabled:cursor-not-allowed transition-colors;
  }

  .btn-secondary {
    @apply bg-white text-gray-700 border border-gray-300 px-4 py-2
           rounded-md font-medium hover:bg-gray-50 focus:outline-none
           focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500;
  }

  .input {
    @apply mt-1 block w-full border border-gray-300 rounded-md shadow-sm
           py-2 px-3 focus:outline-none focus:ring-indigo-500
           focus:border-indigo-500 sm:text-sm;
  }

  .card {
    @apply bg-white rounded-lg shadow p-6;
  }
}

/* Custom utilities (optional) */
@layer utilities {
  .scrollbar-hide {
    -ms-overflow-style: none;
    scrollbar-width: none;
  }

  .scrollbar-hide::-webkit-scrollbar {
    display: none;
  }
}
```

---

## Icon System

### Heroicons (Recommended)

**Installation:**
```bash
npm install @heroicons/react
```

**Usage:**
```tsx
import {
  PlusIcon,
  PencilIcon,
  TrashIcon,
  CheckCircleIcon,
  XMarkIcon,
  Bars3Icon,
  UserCircleIcon,
} from '@heroicons/react/24/outline'

// Solid variants
import { CheckCircleIcon as CheckCircleSolid } from '@heroicons/react/24/solid'

// Usage
<PlusIcon className="h-5 w-5 text-gray-400" />
```

**Icon Sizes:**
- Small: `h-4 w-4` (16px) - inline with text
- Medium: `h-5 w-5` (20px) - buttons, badges
- Large: `h-6 w-6` (24px) - headers, emphasis
- Extra Large: `h-8 w-8` (32px) - empty states

---

## Accessibility Tokens

### Focus Styles
```tsx
// Default focus ring
className="focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2"

// Focus within (for form groups)
className="focus-within:ring-2 focus-within:ring-indigo-500"
```

### Screen Reader Only
```tsx
// Hide visually but keep for screen readers
className="sr-only"
```

### Color Contrast
All text/background combinations meet WCAG AA standards:
- `text-gray-700` on `bg-white`: 10.4:1 (AAA)
- `text-indigo-600` on `bg-white`: 4.5:1 (AA)
- `text-red-600` on `bg-white`: 5.3:1 (AA)

---

## Dark Mode (Future)

**Preparation:**
```js
// tailwind.config.js
module.exports = {
  darkMode: 'class', // Enable class-based dark mode
  // ...
}
```

**Usage (when implemented):**
```tsx
<div className="bg-white dark:bg-gray-800 text-gray-900 dark:text-gray-100">
```

**Not in MVP** - defer to post-launch

---

## Animation Standards

### Transitions
```tsx
// Color transitions (buttons, links)
className="transition-colors duration-150"

// Shadow transitions (cards)
className="transition-shadow duration-200"

// All properties (use sparingly)
className="transition-all duration-200"
```

### Loading Spinners
```tsx
<svg className="animate-spin h-5 w-5 text-indigo-600" ...>
```

### Skeleton Loaders
```tsx
<div className="animate-pulse">
  <div className="h-4 bg-gray-200 rounded w-3/4"></div>
</div>
```

---

## Utilities & Helpers

### Container Widths
```tsx
// Centered container with responsive padding
className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8"

// Content width
className="max-w-4xl mx-auto"

// Narrow content (forms)
className="max-w-2xl mx-auto"
```

### Text Truncation
```tsx
// Single line
className="truncate"

// Multi-line (requires @tailwindcss/line-clamp plugin)
className="line-clamp-2"  // 2 lines max
```

### Aspect Ratios
```tsx
// Square (avatars, images)
className="aspect-square"

// 16:9 (video)
className="aspect-video"
```

---

## Design Tokens Summary

| Token Type | Value | Usage |
|------------|-------|-------|
| **Primary Color** | `indigo-600` | Buttons, links, focus states |
| **Text Color** | `gray-700` | Body text |
| **Heading Color** | `gray-900` | Headings |
| **Border Color** | `gray-300` | Input borders, dividers |
| **Background** | `gray-50` | Page background |
| **Card Background** | `white` | Cards, modals |
| **Border Radius** | `rounded-lg` | Cards (8px) |
| **Border Radius** | `rounded-md` | Buttons, inputs (6px) |
| **Shadow** | `shadow` | Cards |
| **Shadow (hover)** | `shadow-lg` | Interactive cards |
| **Font Family** | `Inter` | All text |
| **Spacing Unit** | `4px` | Base spacing |
| **Container Max** | `max-w-7xl` | Page container |

---

## Component Implementation Guide

### Creating a Reusable Component

```tsx
// src/components/common/Button.tsx
interface ButtonProps {
  variant?: 'primary' | 'secondary' | 'danger'
  size?: 'sm' | 'md' | 'lg'
  children: React.ReactNode
  onClick?: () => void
  disabled?: boolean
  type?: 'button' | 'submit' | 'reset'
}

export function Button({
  variant = 'primary',
  size = 'md',
  children,
  ...props
}: ButtonProps) {
  const baseStyles = 'rounded-md font-medium focus:outline-none focus:ring-2 focus:ring-offset-2 transition-colors'

  const variants = {
    primary: 'bg-indigo-600 text-white hover:bg-indigo-700 focus:ring-indigo-500',
    secondary: 'bg-white text-gray-700 border border-gray-300 hover:bg-gray-50 focus:ring-indigo-500',
    danger: 'bg-red-600 text-white hover:bg-red-700 focus:ring-red-500',
  }

  const sizes = {
    sm: 'px-3 py-1.5 text-sm',
    md: 'px-4 py-2 text-sm',
    lg: 'px-6 py-3 text-base',
  }

  return (
    <button
      className={`${baseStyles} ${variants[variant]} ${sizes[size]}`}
      {...props}
    >
      {children}
    </button>
  )
}
```

---

## Notes

- All tokens align with Tailwind CSS defaults where possible
- Custom tokens extend Tailwind, don't replace
- Design system supports MVP scope (no overengineering)
- Prepared for dark mode but not implemented in MVP
- Focus on consistency over pixel-perfection
