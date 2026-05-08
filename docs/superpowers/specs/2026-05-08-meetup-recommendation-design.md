# Meetup Recommendation — Design Spec

## Context

Meetups have a `dogBreeds: List<String>` field added specifically for breed-based matching.
Dog owners have dogs with a `breed: String` field. This feature connects them: recommend meetups
whose target breeds overlap with the user's dogs.

---

## Scope

**In scope:**
- Backend: `POST /recommend-meetups` Flask endpoint with scoring algorithm
- Android: Tab "For You" added to MeetupListScreen
- Scoring: breed match = 1.0, open meetup (empty dogBreeds) = 0.5, no match = excluded
- Filtering: exclude meetups user created or already joined

**Out of scope:**
- Location-based proximity scoring
- Push notifications for new matching meetups
- SERVICE_PROVIDER access to recommendations

---

## Architecture

**Android sends data → Backend scores → Android displays results**

The Android app fetches meetups and user breeds from Firestore, sends them to the backend,
and the backend returns a sorted recommendation list. Backend stays stateless — no Firebase Admin SDK needed.

---

## Backend Design

### Endpoint: `POST /recommend-meetups`

**Request:**
```json
{
  "dog_breeds": ["Labrador", "Poodle"],
  "user_id": "uid123",
  "meetups": [
    {
      "meetupId": "abc",
      "creatorId": "uid456",
      "location": "Tel Aviv",
      "dateTime": 1234567890000,
      "description": "Dog park meetup",
      "participants": ["uid789"],
      "dogBreeds": ["Labrador"]
    }
  ]
}
```

**Algorithm:**
```python
for each meetup:
  - skip if user_id == creatorId (user created it)
  - skip if user_id in participants (already joined)
  - meetup_breeds empty → score = 0.5 (open to all)
  - any user breed matches meetup breed (case-insensitive) → score = 1.0
  - no match → score = 0, skip

sort by: score DESC, dateTime ASC
```

**Response:**
```json
{ "recommendations": [ ...meetup objects... ] }
```

**Auth:** Same `X-API-Key` header as `/chat`.

---

## Android Design

### Files changed (no new files)

| File | Change |
|------|--------|
| `network/ApiService.kt` | Add `RecommendMeetupsRequest`, `RecommendMeetupsResponse`, `POST recommend-meetups` |
| `viewmodel/MeetupViewModel.kt` | Add `recommendState: StateFlow<RecommendState>`, `loadRecommendations()` |
| `ui/meetup/MeetupListScreen.kt` | Add `TabRow` with "All Meetups" / "For You" tabs |

### ApiService additions

```kotlin
data class RecommendMeetupsRequest(
    @SerializedName("dog_breeds") val dogBreeds: List<String>,
    @SerializedName("user_id") val userId: String,
    @SerializedName("meetups") val meetups: List<Meetup>
)

data class RecommendMeetupsResponse(
    @SerializedName("recommendations") val recommendations: List<Meetup>
)

@POST("recommend-meetups")
suspend fun recommendMeetups(@Body request: RecommendMeetupsRequest): RecommendMeetupsResponse
```

### MeetupViewModel additions

New sealed state:
```kotlin
sealed class RecommendState {
    object Idle : RecommendState()
    object Loading : RecommendState()
    data class Success(val meetups: List<Meetup>) : RecommendState()
    data class Error(val message: String) : RecommendState()
}
```

`loadRecommendations()` method:
1. Get current user UID from FirebaseAuth
2. Fetch user's dogs via `DogRepository().getDogsForOwner(uid)` → extract `breed` values
3. Fetch all meetups via existing `repository.getAllMeetups()`
4. POST to `/recommend-meetups` via ApiClient
5. Emit `RecommendState.Success(response.recommendations)`

### MeetupListScreen changes

- Add `var selectedTab by remember { mutableStateOf(0) }` 
- Add `TabRow` with tabs: "All Meetups" (0), "For You" (1)
- Tab 0: existing meetup list (no change)
- Tab 1: calls `LaunchedEffect` → `viewModel.loadRecommendations()`, shows recommendation cards
- Recommendation cards: same `MeetupCard` composable, reuse existing UI

---

## Data Flow

```
User opens "For You" tab
→ MeetupViewModel.loadRecommendations()
  → DogRepository: get user's dogs → extract breeds
  → MeetupRepository: getAllMeetups() → all meetups
  → POST /recommend-meetups { dog_breeds, user_id, meetups }
  → backend scores + filters + sorts
  → RecommendState.Success(recommendations)
→ LazyColumn renders recommended meetups (same MeetupCard)
```

---

## Error Handling

- No dogs registered → send empty `dog_breeds` list → backend returns open meetups only (score 0.5)
- Backend unreachable (cold start ~30s) → `RecommendState.Error("Could not load recommendations")`
- Empty recommendations → show "No recommendations yet — add your dog's breed to your profile"
