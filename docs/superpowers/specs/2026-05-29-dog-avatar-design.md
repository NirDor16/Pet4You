# Dog Avatar System — Design Spec
**Date:** 2026-05-29
**Status:** Approved

## Overview

A Pou-style cartoon dog avatar system for Pet4You. When a user adds or edits a dog, they can optionally create a customizable cartoon avatar for it instead of (or in addition to) uploading a real photo. The avatar is built from layered vector components and has a breed preset for every supported breed.

---

## Goals

- Give each dog a fun, unique cartoon identity
- Optional — never replaces the existing photo flow, just adds a second path
- Breed presets so the avatar is meaningful out of the box
- Full customization layer on top of the preset

---

## Avatar Data Model

### `DogAvatar.kt` (new file — `data/model/`)

```kotlin
enum class BodyShape { ROUND, STOCKY, SLIM }
enum class FurColor  { GOLDEN, BLACK, WHITE, BROWN, GRAY, SPOTTED }
enum class EarType   { FLOPPY, POINTY, ROUND }
enum class EyeType   { NORMAL, BIG, SLEEPY }
enum class TailType  { STRAIGHT, CURLY, STUB }
enum class Accessory { NONE, COLLAR, BOW, HAT, BANDANA }

data class DogAvatar(
    val bodyShape: BodyShape = BodyShape.ROUND,
    val furColor:  FurColor  = FurColor.GOLDEN,
    val earType:   EarType   = EarType.FLOPPY,
    val eyeType:   EyeType   = EyeType.NORMAL,
    val tailType:  TailType  = TailType.STRAIGHT,
    val accessory: Accessory = Accessory.NONE
)
```

Stored in Firestore as a nested map on the `dogs` document. Backward-compatible — existing dogs default to `null` (no avatar).

### Breed Presets (partial list — expand as needed)

| Breed | Body | Color | Ears | Eyes | Tail | Accessory |
|-------|------|-------|------|------|------|-----------|
| Labrador Retriever | ROUND | GOLDEN | FLOPPY | NORMAL | STRAIGHT | COLLAR |
| Golden Retriever | ROUND | GOLDEN | FLOPPY | BIG | STRAIGHT | NONE |
| Siberian Husky | STOCKY | GRAY | POINTY | NORMAL | CURLY | NONE |
| German Shepherd | STOCKY | BROWN | POINTY | NORMAL | STRAIGHT | COLLAR |
| Poodle | ROUND | WHITE | ROUND | BIG | CURLY | BOW |
| Chihuahua | SLIM | BROWN | POINTY | BIG | STRAIGHT | NONE |
| Rottweiler | STOCKY | BLACK | FLOPPY | SLEEPY | STUB | COLLAR |
| Beagle | ROUND | SPOTTED | FLOPPY | NORMAL | STRAIGHT | NONE |
| Shih Tzu | ROUND | WHITE | ROUND | BIG | CURLY | BOW |
| French Bulldog | STOCKY | GRAY | POINTY | BIG | STUB | BANDANA |
| *others* | ROUND | GOLDEN | FLOPPY | NORMAL | STRAIGHT | NONE |

---

## Updated Dog Model

```kotlin
// Dog.kt — add one nullable field
data class Dog(
    ...
    val photoUrl: String    = "",    // existing
    val avatar:   DogAvatar? = null  // new — null = not set
)
```

### Display Priority in DogListScreen

1. `photoUrl` non-empty → real photo (`AsyncImage`)
2. `avatar != null` → `DogAvatarCanvas` composable
3. fallback → Dog CEO API breed photo → letter circle

---

## New Files

### `ui/components/DogAvatarCanvas.kt`

A pure Compose `Canvas`-based composable. Accepts a `DogAvatar` and renders it as a layered cartoon illustration:

```
Layer order (bottom → top):
  1. Body      — filled circle/oval shape, colored by furColor
  2. Ears      — two shapes behind/above the head
  3. Face      — snout circle + nose dot
  4. Eyes      — two circles with pupils, shape varies by eyeType
  5. Tail      — bezier curve or shape, positioned at back
  6. Accessory — drawn at collar/head position
```

Two size variants:
- `size = AvatarSize.SMALL` (72dp) — DogListScreen cards
- `size = AvatarSize.LARGE` (200dp) — DogAvatarScreen preview

Tail wag animation: `InfiniteTransition` rotates the tail ±15° every 1.5s. Active only in LARGE mode.

Eye blink animation: random blink (close + open) every 3–5s. Active only in LARGE mode.

### `ui/dog/DogAvatarScreen.kt`

Navigation route: `dog_avatar/{dogId}?isNew={true|false}`

Layout:
```
┌─────────────────────────┐
│   Pet4YouTopBar          │  ← "Customize Avatar" + back
├─────────────────────────┤
│                         │
│   DogAvatarCanvas       │  ← 200dp, live preview, animated
│       (LARGE)           │
│                         │
├─────────────────────────┤
│  [ Body ] [Color] [Ears]│  ← TabRow (5 tabs)
│  [ Tail ] [Extras]      │
├─────────────────────────┤
│  ← scrollable options   │  ← chips/cards per tab
│                         │
├─────────────────────────┤
│      [ Save Avatar ]    │  ← FilledButton, saves to Firestore
└─────────────────────────┘
```

Tab content (each rendered as a row of tappable chips with icon previews):
- **Body** — 3 shape chips (Round / Stocky / Slim)
- **Color** — 6 colored circle chips
- **Ears** — 3 ear-type chips
- **Tail** — 3 tail-type chips
- **Extras** — 5 accessory chips (None + 4 options)

Selected chip highlighted with `primaryContainer` background.

On **Save**: calls `DogViewModel.saveAvatar(dogId, avatar)` → Firestore update → `SuccessOverlay` → navigate back.

---

## Modified Files

### `AddEditDogScreen.kt`

Below the `BreedSelector`, add a two-button row:

```
[ 📷  Upload Photo ]    [ 🐾  Create Avatar ]
```

- **Upload Photo** — existing photo picker flow (unchanged)
- **Create Avatar** — navigate to `DogAvatarScreen`; when breed is already selected, preload that breed's preset as starting point
- If the dog already has an avatar saved, show a small preview (80dp) next to "Create Avatar" button with label "Edit Avatar"
- Neither button is required — both remain optional

### `DogListScreen.kt`

Update `DogAvatar` rendering priority (see Display Priority above). Replace the existing avatar resolution logic with a `when` expression checking `photoUrl` → `avatar` → fallback.

### `DogRepository.kt`

- `createDog` / `updateDog`: serialize `DogAvatar?` to/from Firestore map
- Add `saveAvatar(dogId: String, avatar: DogAvatar)`: partial update — only updates the `avatar` field

### `DogViewModel.kt`

- Add `saveAvatar(dogId: String, avatar: DogAvatar)` — calls repository, emits `DogActionState.Success`

### `NavGraph.kt`

- New route: `DOG_AVATAR = "dog_avatar/{dogId}"` with optional `isNew` query param
- Navigates from `AddEditDogScreen`, returns via back stack

---

## "Pou Interaction" — Optional Touch Response

In `DogListScreen`, a **long-press** on a dog card triggers:
1. Tail wag animation (fast, 3 cycles)
2. Eye blink twice
3. `Snackbar`: *"[Dog name] is happy to see you! 🐾"*

Implemented via `Modifier.combinedClickable(onLongClick = {...})` on the card.
This is a fun extra — does not affect any data.

---

## Firestore Impact

No schema migration needed. `avatar` field is a new optional map:
```json
{
  "dogId": "...",
  "avatar": {
    "bodyShape": "ROUND",
    "furColor": "GOLDEN",
    "earType": "FLOPPY",
    "eyeType": "NORMAL",
    "tailType": "STRAIGHT",
    "accessory": "COLLAR"
  }
}
```
Dogs without the field default to `null` — no backfill required.

---

## Out of Scope

- Backend changes — avatar is client-side only
- Sharing avatar as image
- Animated GIF export
- Avatar visible in Meetups / ServiceRequests (can be added later)

---

## Verification Checklist

- [ ] AddEditDogScreen shows two-button row below BreedSelector
- [ ] Selecting a breed auto-loads the breed preset in DogAvatarScreen
- [ ] All 5 customization tabs work; preview updates in real time
- [ ] Save persists to Firestore; existing dogs are unaffected (no avatar field = no crash)
- [ ] DogListScreen respects priority: photo > avatar > fallback
- [ ] Long-press on dog card triggers animation + Snackbar
- [ ] Both photo and avatar can exist on the same dog (photo takes priority)
- [ ] `./gradlew assembleDebug` passes with no new errors
