# Usmentz — QA Testing Checklist
**Version:** v1.0 (Post Photo Booth + Calendar Redesign + Scroll Animation)
**Build:** `assembleDebug`
**Date:** 2026-05-21

---

## 1. PHOTO BOOTH DIALOG (Add Moment)

### 1.1 Dialog Opens
- [/] Tap FAB on Home screen → dialog opens
- [/] Tap FAB on Categories screen → dialog opens
- [/] Tap FAB on Calendar screen → dialog opens
- [/] Dialog appears as bottom sheet with rounded top corners
- [/] Photo area shows purple placeholder with camera icon + "Tap to capture" text

### 1.2 Photo Capture
- [/] Tap photo area → "Add Photo" dialog appears with "Take Photo" and "Choose from Gallery"
- [x] **Take Photo** → camera opens → capture → photo appears in preview area
- [ after saving it crashes] **Choose from Gallery** → gallery opens → select image → photo appears in preview area
- [ ] After photo is picked:
  - [ ] Placeholder disappears
  - [ ] Photo fills the 200dp area with rounded corners
  - [ ] "Change" button appears bottom-right of photo
  - [ ] Border picker section appears below photo area
- [ ] Tap "Change" → photo picker dialog reopens → new photo replaces old

### 1.3 Border Selection
- [/] Border picker shows exactly 3 options: Clean, Polaroid, Vintage
- [x] Default: "Clean" is selected (purple ring indicator around it)
- [x] Tap "Polaroid" → purple ring moves to Polaroid, photo gets white frame
- [x] Tap "Vintage" → purple ring moves to Vintage, photo gets sepia frame
- [x] Tap "Clean" → purple ring moves to Clean, photo gets no frame
- [ ] Haptic feedback on each border tap (if device supports vibration)

### 1.4 Details Fields
- [/] "Name this moment" field — type text, no error
- [/] "How did it feel?" — mood chips (Cozy, Romantic, Fun, Adventurous, Relaxing, Exciting)
- [/] Tap a mood chip → chip highlights, haptic feedback
- [/] "Cost (optional)" field — enter numbers, accepts decimals
- [/] "A quick note..." field — multiline text input works
- [/] Leave all optional fields empty → save still works

### 1.5 Save / Cancel
- [x] Tap "Cancel" → dialog dismisses, no data saved
- [x] Tap "Save Memory" with name filled → moment saved, toast "Memory saved to [category]"
- [x] Tap "Save Memory" with name empty → error "Enter a name" on the field
- [x] After save → moment appears in the correct category's moment list

---

## 2. CALENDAR SCREEN

### 2.1 Header
- [ ] Header shows "July 2026" (not uppercase, not bold — soft title style)
- [ ] Menu icon (hamburger) on right → opens sidebar drawer
- [ ] Sidebar drawer opens/closes properly
- [ ] Sidebar view mode switching (list/grid/board/calendar) works

### 2.2 Calendar Grid
- [ ] Day-of-week row: S M T W T F S (single letters, gray, centered)
- [ ] Prev/Today/Next row:
  - [ ] Left arrow → goes to previous month
  - [ ] "Today" pill → returns to current month
  - [ ] Right arrow → goes to next month
- [ ] Grid displays correct dates for the month
- [ ] Today's date has a purple ring around it
- [ ] Boundary dates (first/last row) have lighter text color
- [ ] Days with moments show labels when selected/today

### 2.3 Day Selection & Moment List
- [ ] Tap a day with moments → day highlights, list section shows moments for that day
- [ ] List header shows selected date (e.g., "Jul 15")
- [ ] Moment cards display: name, date, location, trailing ellipsis icon
- [ ] Each card has a thin purple accent line (1dp) at the bottom
- [ ] Tap a moment card → opens DetailActivity
- [ ] Tap a day with NO moments → list shows "No moments on Jul 15"
- [ ] Deselect day (tap empty day) → list resets to "Moments" header
- [ ] "No moments this month" shows when no moments exist for the entire month

### 2.4 FAB & Navigation
- [ ] FAB (purple + icon) at bottom-right → opens photo booth dialog
- [ ] Left-aligned pill navbar: Home, Categories, Calendar (Calendar active)
- [ ] Tap Home → navigates to HomeActivity
- [ ] Tap Categories → navigates to MainActivity
- [ ] Tap Calendar → stays on calendar (no navigation)

---

## 3. SCROLL-BASED NAVBAR ANIMATION (All Screens)

### 3.1 HomeActivity
- [ ] Scroll recent moments list DOWN → navbar and FAB slide down (translate Y)
- [ ] Scroll UP → navbar and FAB slide back up to original position
- [ ] Animation is smooth (~250ms), not jarring
- [ ] On activity resume → navbar and FAB are visible (not hidden)

### 3.2 MainActivity (Categories Mode)
- [ ] Scroll categories grid → navbar and FAB respond to scroll direction
- [ ] Scroll works on both categories and moments list

### 3.3 MainActivity (Moments Mode)
- [ ] Enter a category → moments list scrolls → navbar/FAB slide down on scroll down
- [ ] Scroll up → navbar/FAB slide back up
- [ ] Back button → exits moments mode, navbar resets to visible

### 3.4 CalendarActivity
- [ ] Scroll calendar grid → navbar/FAB slide down
- [ ] Scroll moment list → navbar/FAB slide down
- [ ] Scroll up → navbar/FAB slide back up

### 3.5 FavoritesActivity
- [ ] Scroll favorites list → navbar/FAB slide down on scroll down
- [ ] Scroll up → navbar/FAB slide back up

---

## 4. NAVBAR CONSISTENCY (All Screens)

- [ ] All 4 screens use the same left-aligned pill navbar pattern
- [ ] Navbar container: 60dp height, 32dp corner radius, frosted glass background
- [ ] Active slot: purple pill with icon + text (14sp, sans-serif-medium)
- [ ] Inactive slot: icon only (gray tint)
- [ ] Active/inactive states switch correctly on tap
- [ ] FAB is always bottom-right on all screens (not overlapping navbar)

---

## 5. PURPLE HEADER CURVES

- [ ] Home screen header: purple gradient with curved bottom corners (16dp)
- [ ] MainActivity header: purple gradient with curved bottom corners (16dp)
- [ ] Curve is subtle, not exaggerated
- [ ] No clipping or rendering artifacts on the curve

---

## 6. EDGE CASES & ERROR HANDLING

### 6.1 Photo Booth
- [ ] Camera permission denied → graceful fallback (gallery still works)
- [ ] Gallery permission denied → graceful fallback (camera still works)
- [ ] No storage space → photo save fails, no crash
- [ ] Pick a very large image → Glide handles it without OOM
- [ ] Rapidly tap photo area multiple times → no duplicate pickers

### 6.2 Calendar
- [ ] Navigate to month with no moments → grid displays correctly, list shows "No moments this month"
- [ ] Navigate to month with many moments → grid and list handle it
- [ ] Select today → today ring appears, moments for today show in list
- [ ] Rotate screen during calendar use → state preserved (month, selected day)

### 6.3 Scroll Animation
- [ ] Fast fling scroll → navbar animation doesn't stutter
- [ ] Scroll to bottom, then scroll up → navbar reappears smoothly
- [ ] Activity pause/resume → navbar is visible (not stuck hidden)

---

## 7. KNOWN ISSUES (Not Blocking)

| # | Issue | Severity | Notes |
|---|-------|----------|-------|
| 1 | Photo booth dialog is tall on small screens (5") | Low | Keyboard may cover save button |
| 2 | MainActivity attaches scroll listener to 2 RecyclerViews | Low | Minor performance waste, not a bug |
| 3 | Border system is hardcoded to 3 options | Low | Adding new borders requires code change |
| 4 | Firestore security rules not published | Medium | Cloud sync blocked until manual publish |

---

## 8. TEST MATRIX

| Device | Android Version | Screen Size | Status |
|--------|----------------|-------------|--------|
| Pixel 6 | Android 13 | 6.4" | [ ] Pass |
| Pixel 4a | Android 12 | 5.8" | [ ] Pass |
| Tablet (optional) | Android 13 | 10" | [ ] Pass |

---

## 9. SIGN-OFF

| Tester | Date | Status | Notes |
|--------|------|--------|-------|
| | | ☐ Pass / ☐ Fail | |
