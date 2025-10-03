# LeagueHQ - UI Design Specifications

## Overview
MVP UI specifications for 6-7 week launch. Aligned with build plan and frontend coding guidelines.

**Design Principles:**
- Mobile-first responsive design
- Clean, functional interface (not overly designed)
- Accessibility: WCAG 2.1 AA compliance
- Fast load times: minimal animations, optimized images
- Clear user flows: minimize clicks to complete tasks

---

## Layout Structure

### App Shell
All authenticated pages share a consistent layout:

```
┌─────────────────────────────────────┐
│ Header (Navigation)                 │
├─────────────────────────────────────┤
│                                     │
│                                     │
│     Main Content Area               │
│                                     │
│                                     │
└─────────────────────────────────────┘
```

### Header Component
**Desktop (≥768px):**
- Logo/Brand (left) - links to dashboard
- Navigation links (center): Competitions, Teams, Matches
- User menu (right): Avatar + dropdown (Profile, Logout)

**Mobile (<768px):**
- Hamburger menu (left)
- Logo/Brand (center)
- User avatar (right)

**Role-Based Nav:**
- **COMPETITION_OWNER:** My Competitions, Create Competition, Settings
- **COACH:** My Teams, Find Competitions, Settings
- **PLATFORM_OWNER:** Dashboard, All Competitions, All Users, Payouts

---

## Component Library

### 1. Buttons

**Primary Button**
```tsx
// Used for: Submit forms, primary actions
className="bg-indigo-600 text-white px-4 py-2 rounded-md hover:bg-indigo-700
           focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500
           disabled:opacity-50 disabled:cursor-not-allowed"
```

**Secondary Button**
```tsx
// Used for: Cancel, back actions
className="bg-white text-gray-700 border border-gray-300 px-4 py-2 rounded-md
           hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2
           focus:ring-indigo-500"
```

**Danger Button**
```tsx
// Used for: Delete, irreversible actions
className="bg-red-600 text-white px-4 py-2 rounded-md hover:bg-red-700
           focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-red-500"
```

### 2. Form Inputs

**Text Input**
```tsx
<input
  type="text"
  className="mt-1 block w-full border border-gray-300 rounded-md shadow-sm
             py-2 px-3 focus:outline-none focus:ring-indigo-500
             focus:border-indigo-500 sm:text-sm"
/>
```

**Select Dropdown**
```tsx
<select
  className="mt-1 block w-full border border-gray-300 rounded-md shadow-sm
             py-2 px-3 focus:outline-none focus:ring-indigo-500
             focus:border-indigo-500 sm:text-sm"
>
  <option>Option 1</option>
</select>
```

**File Upload (Photo)**
```tsx
<div className="mt-1 flex items-center">
  <input
    type="file"
    accept="image/jpeg,image/png"
    className="sr-only"
    id="file-upload"
  />
  <label
    htmlFor="file-upload"
    className="cursor-pointer bg-white py-2 px-3 border border-gray-300
               rounded-md shadow-sm text-sm font-medium text-gray-700
               hover:bg-gray-50"
  >
    Choose file
  </label>
  <span className="ml-3 text-sm text-gray-500">PNG, JPG up to 5MB</span>
</div>
```

**Error Message**
```tsx
<p className="mt-2 text-sm text-red-600">
  {errorMessage}
</p>
```

### 3. Cards

**Competition Card**
```tsx
<div className="bg-white rounded-lg shadow hover:shadow-lg transition-shadow p-6">
  <h3 className="text-lg font-semibold text-gray-900">{name}</h3>
  <p className="text-sm text-gray-500 mt-1">{description}</p>
  <div className="mt-4 flex items-center justify-between">
    <span className="text-sm text-gray-600">Entry Fee: ${entryFee}</span>
    <span className="px-2 py-1 text-xs rounded-full bg-indigo-100 text-indigo-800">
      {status}
    </span>
  </div>
</div>
```

**Team Card**
```tsx
<div className="bg-white rounded-lg shadow p-6">
  <div className="flex items-center justify-between">
    <h3 className="text-lg font-semibold text-gray-900">{teamName}</h3>
    <span className="text-sm text-gray-500">{rosterSize} players</span>
  </div>
  <div className="mt-2 text-sm text-gray-600">
    Competition: {competitionName}
  </div>
  <div className="mt-4">
    <span className="px-2 py-1 text-xs rounded-full bg-green-100 text-green-800">
      {subscriptionStatus}
    </span>
  </div>
</div>
```

### 4. Tables

**Standings Table**
```tsx
<table className="min-w-full divide-y divide-gray-200">
  <thead className="bg-gray-50">
    <tr>
      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
        Team
      </th>
      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
        P
      </th>
      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
        W
      </th>
      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
        D
      </th>
      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
        L
      </th>
      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
        GD
      </th>
      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
        Pts
      </th>
    </tr>
  </thead>
  <tbody className="bg-white divide-y divide-gray-200">
    {/* Table rows */}
  </tbody>
</table>
```

### 5. Status Badges

**Competition Status**
- `DRAFT`: gray
- `PUBLISHED`: blue
- `ACTIVE`: green
- `COMPLETED`: purple
- `CANCELLED`: red

```tsx
const statusColors = {
  DRAFT: 'bg-gray-100 text-gray-800',
  PUBLISHED: 'bg-blue-100 text-blue-800',
  ACTIVE: 'bg-green-100 text-green-800',
  COMPLETED: 'bg-purple-100 text-purple-800',
  CANCELLED: 'bg-red-100 text-red-800',
}

<span className={`px-2 py-1 text-xs rounded-full ${statusColors[status]}`}>
  {status}
</span>
```

**Subscription Status**
- `ACTIVE`: green
- `PAST_DUE`: yellow
- `CANCELLED`: red

```tsx
const subStatusColors = {
  ACTIVE: 'bg-green-100 text-green-800',
  PAST_DUE: 'bg-yellow-100 text-yellow-800',
  CANCELLED: 'bg-red-100 text-red-800',
}
```

### 6. Modals/Dialogs

**Confirmation Dialog**
```tsx
<div className="fixed inset-0 z-50 overflow-y-auto">
  <div className="flex items-center justify-center min-h-screen px-4">
    {/* Overlay */}
    <div className="fixed inset-0 bg-gray-500 bg-opacity-75 transition-opacity"></div>

    {/* Modal */}
    <div className="relative bg-white rounded-lg px-4 pt-5 pb-4 text-left shadow-xl sm:p-6 sm:w-full sm:max-w-lg">
      <h3 className="text-lg font-medium text-gray-900">Confirm Action</h3>
      <p className="mt-2 text-sm text-gray-500">Are you sure you want to proceed?</p>
      <div className="mt-5 sm:mt-6 sm:flex sm:flex-row-reverse">
        <button className="...">Confirm</button>
        <button className="...">Cancel</button>
      </div>
    </div>
  </div>
</div>
```

### 7. Loading States

**Spinner**
```tsx
<div className="flex justify-center items-center">
  <svg className="animate-spin h-8 w-8 text-indigo-600" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
  </svg>
</div>
```

**Skeleton Loader (Cards)**
```tsx
<div className="bg-white rounded-lg shadow p-6 animate-pulse">
  <div className="h-4 bg-gray-200 rounded w-3/4"></div>
  <div className="mt-3 h-3 bg-gray-200 rounded w-1/2"></div>
  <div className="mt-4 h-3 bg-gray-200 rounded w-1/4"></div>
</div>
```

### 8. Empty States

```tsx
<div className="text-center py-12">
  <svg className="mx-auto h-12 w-12 text-gray-400" /* icon SVG */>
  </svg>
  <h3 className="mt-2 text-sm font-medium text-gray-900">No competitions</h3>
  <p className="mt-1 text-sm text-gray-500">Get started by creating a new competition.</p>
  <div className="mt-6">
    <button className="...">
      <PlusIcon /> New Competition
    </button>
  </div>
</div>
```

---

## Responsive Breakpoints

Following Tailwind defaults:
- **Mobile:** `< 640px` (default)
- **Tablet:** `sm: 640px`
- **Desktop:** `md: 768px`
- **Large Desktop:** `lg: 1024px`

**Grid Layouts:**
```tsx
// Cards grid
<div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
```

**Form Layouts:**
```tsx
// Two-column form (desktop)
<div className="grid grid-cols-1 md:grid-cols-2 gap-4">
```

---

## Accessibility Guidelines

### Keyboard Navigation
- All interactive elements focusable with `Tab`
- Focus visible: `focus:ring-2 focus:ring-indigo-500`
- Escape key closes modals
- Enter key submits forms

### Screen Readers
- Use semantic HTML (`<nav>`, `<main>`, `<footer>`)
- Label all inputs: `<label htmlFor="email">Email</label>`
- Alt text for images: `alt="Player photo"`
- ARIA labels for icon buttons: `aria-label="Delete player"`

### Color Contrast
- Text on white: minimum `gray-700` (#374151)
- Colored text: passes WCAG AA (4.5:1 for normal text)
- Error states: red-600 on white background

### Touch Targets
- Minimum 44x44px for mobile buttons
- Adequate spacing between interactive elements

---

## Animation & Transitions

**Subtle Transitions:**
```tsx
// Hover effects
className="transition-shadow hover:shadow-lg"
className="transition-colors hover:bg-indigo-700"

// Loading states
className="transition-opacity opacity-0 data-[loaded]:opacity-100"
```

**Avoid:**
- Complex animations (slow devices)
- Autoplay animations (accessibility)
- Unnecessary micro-interactions

---

## Image Handling

### Player Photos
- **Display Size:** 200x200px in roster grid, 80x80px in lists
- **Upload Max:** 5MB
- **Processed:** <500KB, max 1080px width
- **Fallback:** Initials avatar (colored background)

```tsx
// Initials Avatar Fallback
<div className="w-20 h-20 rounded-full bg-indigo-500 flex items-center justify-center">
  <span className="text-2xl font-medium text-white">JD</span>
</div>
```

### Competition/Team Logos (Future)
- Not in MVP
- Placeholder: use emoji or colored circle

---

## Forms UX

### Validation
- **Inline validation:** Show errors on blur
- **Submit validation:** Show all errors at once
- **Success feedback:** Toast notification or success message

### Input States
```tsx
// Default
className="border-gray-300 focus:border-indigo-500"

// Error
className="border-red-300 focus:border-red-500"

// Disabled
className="bg-gray-100 cursor-not-allowed"
```

### Progressive Disclosure
- Multi-step forms: show progress indicator
- Collapsible sections for advanced options
- Example: Create Competition (Basic Info → Venues → Policy Settings)

---

## Notifications

**Toast Notifications**
```tsx
// Success
<div className="bg-green-50 border-l-4 border-green-400 p-4">
  <p className="text-sm text-green-800">Team registered successfully!</p>
</div>

// Error
<div className="bg-red-50 border-l-4 border-red-400 p-4">
  <p className="text-sm text-red-800">Payment failed. Please try again.</p>
</div>

// Warning
<div className="bg-yellow-50 border-l-4 border-yellow-400 p-4">
  <p className="text-sm text-yellow-800">Subscription past due. Update payment method.</p>
</div>
```

**Position:** Top-right, auto-dismiss after 5 seconds

---

## Data Display Patterns

### Currency
```tsx
const formatCurrency = (cents: number) => {
  return `$${(cents / 100).toFixed(2)}`
}
```

### Dates
```tsx
import { format } from 'date-fns'

// Match date: "Mar 15, 2025"
format(date, 'MMM dd, yyyy')

// Full datetime: "Mar 15, 2025 at 3:00 PM"
format(date, 'MMM dd, yyyy \'at\' h:mm a')
```

### Truncated Text
```tsx
// Max 2 lines with ellipsis
className="line-clamp-2"

// Single line truncate
className="truncate"
```

---

## Performance Considerations

### Image Optimization
- Use `loading="lazy"` for images below the fold
- Serve WebP format with JPEG fallback
- Responsive images: `srcset` for different screen sizes

### Code Splitting
- Not needed for MVP (<500KB bundle)
- Consider lazy loading routes post-launch

### Perceived Performance
- Show loading skeletons instead of spinners
- Optimistic UI updates (update UI before API response)
- Debounce search inputs (300ms delay)

---

## Dark Mode (Deferred)

Not in MVP. Prepare by:
- Using Tailwind color utilities (not hardcoded colors)
- Semantic color naming in design system
- Add dark mode in V2 with `dark:` prefix classes

---

## Icon Library

**Heroicons** (Tailwind recommended)
```bash
npm install @heroicons/react
```

**Common Icons:**
- Navigation: Bars3Icon (menu), XMarkIcon (close)
- Actions: PlusIcon, PencilIcon, TrashIcon
- Status: CheckCircleIcon, XCircleIcon, ExclamationTriangleIcon
- Social: UserIcon, UsersIcon, CalendarIcon

**Usage:**
```tsx
import { PlusIcon } from '@heroicons/react/24/outline'

<PlusIcon className="h-5 w-5 text-gray-400" />
```

---

## Component Naming Conventions

Follow frontend coding guidelines:
- Components: `PascalCase` (CompetitionCard.tsx)
- Props interfaces: `{ComponentName}Props`
- Organize by feature in `components/` folder

---

## Notes

- Specs align with `CODING_GUIDELINES_FRONTEND.md`
- Colors match Tailwind defaults (indigo primary)
- Focus on usability over aesthetics for MVP
- Iterate based on user feedback post-launch
