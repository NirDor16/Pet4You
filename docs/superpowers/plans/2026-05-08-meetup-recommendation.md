# Meetup Recommendation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a `POST /recommend-meetups` Flask endpoint and a "For You" tab in MeetupListScreen that shows breed-matched meetups.

**Architecture:** Android fetches meetups + user breeds from Firestore, sends them to the backend, Flask scores each meetup (breed match=1.0, open=0.5, no match=excluded), filters out user's own/joined meetups, and returns a sorted list. No new Android files needed — extend existing ones.

**Tech Stack:** Python/Flask (backend), Kotlin/Jetpack Compose, Retrofit2/Gson, Firebase Firestore

---

## File Map

| File | Change |
|------|--------|
| `backend/app.py` | Add `score_meetups()` helper + `POST /recommend-meetups` endpoint |
| `backend/test_recommend.py` | New — pytest tests for `score_meetups()` |
| `app/.../network/ApiService.kt` | Add `RecommendMeetupsRequest`, `RecommendMeetupsResponse`, new endpoint |
| `app/.../viewmodel/MeetupViewModel.kt` | Add `RecommendState` sealed class + `recommendState` flow + `loadRecommendations()` |
| `app/.../ui/meetup/MeetupListScreen.kt` | Add `TabRow` ("All Meetups" / "For You"), wire recommend tab |

---

## Task 1: Backend — scoring algorithm + test

**Files:**
- Modify: `backend/app.py`
- Create: `backend/test_recommend.py`

- [ ] **Step 1: Write the failing test**

Create `backend/test_recommend.py`:

```python
import pytest
from app import score_meetups

def meetup(id, creator, participants, breeds, dt=1000):
    return {
        "meetupId": id,
        "creatorId": creator,
        "participants": participants,
        "dogBreeds": breeds,
        "dateTime": dt,
        "location": "Tel Aviv",
        "description": ""
    }

def test_breed_match_returns_score_1():
    result = score_meetups(["Labrador"], "user1", [
        meetup("m1", "other", [], ["Labrador"])
    ])
    assert len(result) == 1
    assert result[0]["meetupId"] == "m1"

def test_open_meetup_returns_score_half():
    result = score_meetups(["Labrador"], "user1", [
        meetup("m1", "other", [], [])
    ])
    assert len(result) == 1

def test_no_breed_match_excluded():
    result = score_meetups(["Labrador"], "user1", [
        meetup("m1", "other", [], ["Poodle"])
    ])
    assert result == []

def test_user_own_meetup_excluded():
    result = score_meetups(["Labrador"], "user1", [
        meetup("m1", "user1", [], ["Labrador"])
    ])
    assert result == []

def test_already_joined_excluded():
    result = score_meetups(["Labrador"], "user1", [
        meetup("m1", "other", ["user1"], ["Labrador"])
    ])
    assert result == []

def test_sorted_match_before_open():
    result = score_meetups(["Labrador"], "user1", [
        meetup("open", "other", [], [], dt=500),
        meetup("match", "other", [], ["Labrador"], dt=1000),
    ])
    assert result[0]["meetupId"] == "match"
    assert result[1]["meetupId"] == "open"

def test_case_insensitive_match():
    result = score_meetups(["labrador"], "user1", [
        meetup("m1", "other", [], ["LABRADOR"])
    ])
    assert len(result) == 1

def test_empty_breeds_returns_open_meetups():
    result = score_meetups([], "user1", [
        meetup("open", "other", [], []),
        meetup("breed", "other", [], ["Poodle"]),
    ])
    assert len(result) == 1
    assert result[0]["meetupId"] == "open"
```

- [ ] **Step 2: Run tests — expect FAIL (score_meetups not defined)**

```bash
cd backend && python -m pytest test_recommend.py -v
```

Expected: `ImportError: cannot import name 'score_meetups' from 'app'`

- [ ] **Step 3: Implement `score_meetups` in `backend/app.py`**

Add after the `SYSTEM_PROMPT` definition (before the routes):

```python
def score_meetups(dog_breeds, user_id, meetups):
    user_breeds = {b.lower() for b in dog_breeds}
    scored = []
    for meetup in meetups:
        if meetup.get("creatorId") == user_id:
            continue
        if user_id in meetup.get("participants", []):
            continue
        meetup_breeds = {b.lower() for b in meetup.get("dogBreeds", [])}
        if not meetup_breeds:
            score = 0.5
        elif user_breeds & meetup_breeds:
            score = 1.0
        else:
            continue
        scored.append((score, meetup.get("dateTime", 0), meetup))
    scored.sort(key=lambda x: (-x[0], x[1]))
    return [item[2] for item in scored]
```

- [ ] **Step 4: Run tests — expect PASS**

```bash
cd backend && python -m pytest test_recommend.py -v
```

Expected: 8 tests PASSED

- [ ] **Step 5: Add `POST /recommend-meetups` endpoint to `backend/app.py`**

Add after the `/chat` route:

```python
@app.route("/recommend-meetups", methods=["POST"])
def recommend_meetups():
    if not check_api_key():
        return jsonify({"error": "Unauthorized"}), 401

    data = request.get_json(silent=True) or {}
    dog_breeds = data.get("dog_breeds", [])
    user_id = data.get("user_id", "")
    meetups = data.get("meetups", [])

    recommendations = score_meetups(dog_breeds, user_id, meetups)
    return jsonify({"recommendations": recommendations})
```

- [ ] **Step 6: Commit**

```bash
cd backend && git add app.py test_recommend.py && git commit -m "feat: add POST /recommend-meetups with breed-scoring algorithm"
```

---

## Task 2: Android — ApiService additions

**Files:**
- Modify: `app/src/main/java/com/example/pet4you/network/ApiService.kt`

- [ ] **Step 1: Add request/response types and endpoint**

Replace the full file content with:

```kotlin
package com.example.pet4you.network

import com.example.pet4you.data.model.ChatMessage
import com.example.pet4you.data.model.Meetup
import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.POST

data class ChatRequest(val message: String, val history: List<ChatMessage>)
data class ChatResponse(val reply: String)

data class RecommendMeetupsRequest(
    @SerializedName("dog_breeds") val dogBreeds: List<String>,
    @SerializedName("user_id") val userId: String,
    @SerializedName("meetups") val meetups: List<Meetup>
)

data class RecommendMeetupsResponse(
    @SerializedName("recommendations") val recommendations: List<Meetup>
)

interface ApiService {
    @POST("chat")
    suspend fun sendMessage(@Body request: ChatRequest): ChatResponse

    @POST("recommend-meetups")
    suspend fun recommendMeetups(@Body request: RecommendMeetupsRequest): RecommendMeetupsResponse
}
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/example/pet4you/network/ApiService.kt
git commit -m "feat: add RecommendMeetups request/response + Retrofit endpoint"
```

---

## Task 3: Android — MeetupViewModel recommendation support

**Files:**
- Modify: `app/src/main/java/com/example/pet4you/viewmodel/MeetupViewModel.kt`

- [ ] **Step 1: Add `RecommendState` sealed class and `loadRecommendations()`**

Add the `RecommendState` sealed class after `MeetupActionState` (before `class MeetupViewModel`):

```kotlin
sealed class RecommendState {
    object Idle : RecommendState()
    object Loading : RecommendState()
    data class Success(val meetups: List<Meetup>, val currentUserId: String?) : RecommendState()
    data class Error(val message: String) : RecommendState()
}
```

Add imports at the top of the file:

```kotlin
import com.example.pet4you.network.ApiClient
import com.example.pet4you.network.RecommendMeetupsRequest
import com.example.pet4you.repository.DogRepository
import com.google.firebase.auth.FirebaseAuth
```

Inside `class MeetupViewModel`, add after the `_meetupActionState` declaration:

```kotlin
private val dogRepository = DogRepository()

private val _recommendState = MutableStateFlow<RecommendState>(RecommendState.Idle)
val recommendState: StateFlow<RecommendState> = _recommendState
```

Add `loadRecommendations()` method inside `class MeetupViewModel` (before `resetActionState()`):

```kotlin
fun loadRecommendations() {
    viewModelScope.launch {
        _recommendState.value = RecommendState.Loading
        try {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            val breeds = dogRepository.getDogsForCurrentUser()
                .getOrNull()
                ?.map { it.breed }
                ?.filter { it.isNotBlank() }
                ?: emptyList()
            val meetups = repository.getAllMeetups().getOrNull() ?: emptyList()
            val response = ApiClient.apiService.recommendMeetups(
                RecommendMeetupsRequest(
                    dogBreeds = breeds,
                    userId = uid,
                    meetups = meetups
                )
            )
            _recommendState.value = RecommendState.Success(
                meetups = response.recommendations,
                currentUserId = uid
            )
        } catch (e: Exception) {
            _recommendState.value = RecommendState.Error(
                e.message ?: "Could not load recommendations"
            )
        }
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/example/pet4you/viewmodel/MeetupViewModel.kt
git commit -m "feat: add RecommendState + loadRecommendations() to MeetupViewModel"
```

---

## Task 4: Android — MeetupListScreen tabs

**Files:**
- Modify: `app/src/main/java/com/example/pet4you/ui/meetup/MeetupListScreen.kt`

- [ ] **Step 1: Add imports and tab state**

Add these imports (replace the existing import block):

```kotlin
package com.example.pet4you.ui.meetup

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pet4you.data.model.Meetup
import com.example.pet4you.viewmodel.MeetupActionState
import com.example.pet4you.viewmodel.MeetupListState
import com.example.pet4you.viewmodel.MeetupViewModel
import com.example.pet4you.viewmodel.RecommendState
import java.text.SimpleDateFormat
import java.util.*
```

- [ ] **Step 2: Add tab state + collect recommendState inside `MeetupListScreen`**

Inside `MeetupListScreen` composable, add after the existing state collections:

```kotlin
val recommendState by viewModel.recommendState.collectAsState()
var selectedTab by remember { mutableStateOf(0) }
val tabs = listOf("All Meetups", "For You")
```

- [ ] **Step 3: Add `LaunchedEffect` for "For You" tab loading**

Add after the existing `LaunchedEffect(meetupActionState)`:

```kotlin
LaunchedEffect(selectedTab) {
    if (selectedTab == 1) viewModel.loadRecommendations()
}
```

- [ ] **Step 4: Add `TabRow` inside `Scaffold` content**

Replace the `Column` inside `Scaffold { padding -> ... }` with:

```kotlin
Column(
    modifier = Modifier
        .fillMaxSize()
        .padding(padding)
) {
    TabRow(selectedTabIndex = selectedTab) {
        tabs.forEachIndexed { index, title ->
            Tab(
                selected = selectedTab == index,
                onClick = { selectedTab = index },
                text = { Text(title) }
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (meetupActionState is MeetupActionState.Error) {
            Text(
                text = (meetupActionState as MeetupActionState.Error).message,
                color = Color.Red,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        if (selectedTab == 0) {
            when (val state = meetupListState) {
                is MeetupListState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is MeetupListState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(state.message, color = Color.Red)
                    }
                }
                is MeetupListState.Success -> {
                    if (state.meetups.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No meetups yet. Create one!")
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(state.meetups, key = { it.meetupId }) { meetup ->
                                MeetupCard(
                                    meetup = meetup,
                                    currentUserId = state.currentUserId,
                                    onJoin = { viewModel.joinMeetup(meetup.meetupId) },
                                    onLeave = { viewModel.leaveMeetup(meetup.meetupId) },
                                    onDelete = { viewModel.deleteMeetup(meetup.meetupId) }
                                )
                            }
                        }
                    }
                }
                else -> {}
            }
        } else {
            when (val state = recommendState) {
                is RecommendState.Loading, RecommendState.Idle -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is RecommendState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(state.message, color = Color.Red)
                    }
                }
                is RecommendState.Success -> {
                    if (state.meetups.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No recommendations yet.\nAdd your dog's breed to see matching meetups.")
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(state.meetups, key = { it.meetupId }) { meetup ->
                                MeetupCard(
                                    meetup = meetup,
                                    currentUserId = state.currentUserId,
                                    onJoin = { viewModel.joinMeetup(meetup.meetupId) },
                                    onLeave = { viewModel.leaveMeetup(meetup.meetupId) },
                                    onDelete = { viewModel.deleteMeetup(meetup.meetupId) }
                                )
                            }
                        }
                    }
                }
                else -> {}
            }
        }
    }
}
```

- [ ] **Step 5: Compile check**

```bash
cd C:\Users\dresh\AndroidStudioProjects\LockApp\Pet4You
git stash -- app/google-services.json
./gradlew assembleDebug
git stash pop
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/example/pet4you/ui/meetup/MeetupListScreen.kt
git commit -m "feat: add For You tab to MeetupListScreen with breed-based recommendations"
```

---

## Task 5: Push backend + open PR

- [ ] **Step 1: Push backend changes**

```bash
cd backend && git push
```

(Backend deploys automatically on Render when pushed to master via PR)

- [ ] **Step 2: Create feature branch and open Android PR**

```bash
cd C:\Users\dresh\AndroidStudioProjects\LockApp\Pet4You
git checkout -b feature/meetup-recommendation
git push -u origin feature/meetup-recommendation
gh pr create \
  --title "feat: meetup recommendation — For You tab with breed matching" \
  --body "..."
```

---

## Verification

1. Backend tests: `cd backend && python -m pytest test_recommend.py -v` → 8 PASSED
2. Android compile: `./gradlew assembleDebug` → BUILD SUCCESSFUL
3. On device: open Meetups → tap "For You" → spinner → recommendations appear
4. Meetups user created or joined do NOT appear in "For You"
5. Meetup with matching breed appears before open (no-breed) meetups
6. No dogs registered → only open meetups shown (or empty list if none)
