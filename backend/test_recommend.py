import time as time_module

import pytest

from app import dijkstra_recommend

NOW_MS = int(time_module.time() * 1000)
SOON   = NOW_MS + 3  * 24 * 60 * 60 * 1000   # 3 days  — within 7-day bonus window
LATER  = NOW_MS + 15 * 24 * 60 * 60 * 1000   # 15 days — within 30-day bonus window
FAR    = NOW_MS + 60 * 24 * 60 * 60 * 1000   # 60 days — no date bonus
PAST   = NOW_MS - 1  * 24 * 60 * 60 * 1000   # yesterday — excluded


def meetup(id, creator="other", participants=None, breeds=None, dt=SOON, location="Tel Aviv"):
    return {
        "meetupId": id,
        "creatorId": creator,
        "participants": participants or [],
        "dogBreeds": breeds or [],
        "dateTime": dt,
        "location": location,
        "description": "",
    }


# ---------------------------------------------------------------------------
# Basic inclusion / exclusion
# ---------------------------------------------------------------------------

def test_breed_match_included_and_has_score():
    result = dijkstra_recommend(["Labrador"], "user1", [
        meetup("m1", breeds=["Labrador"])
    ])
    assert len(result) == 1
    assert result[0]["meetupId"] == "m1"
    assert "score" in result[0]
    assert 0.0 < result[0]["score"] <= 1.0


def test_open_meetup_included():
    result = dijkstra_recommend(["Labrador"], "user1", [
        meetup("m1", breeds=[])
    ])
    assert len(result) == 1


def test_different_size_group_excluded():
    # Labrador = large, Chihuahua = small → no edge, no path
    result = dijkstra_recommend(["Labrador"], "user1", [
        meetup("m1", breeds=["Chihuahua"])
    ])
    assert result == []


def test_user_own_meetup_excluded():
    result = dijkstra_recommend(["Labrador"], "user1", [
        meetup("m1", creator="user1", breeds=["Labrador"])
    ])
    assert result == []


def test_already_joined_excluded():
    result = dijkstra_recommend(["Labrador"], "user1", [
        meetup("m1", participants=["user1"], breeds=["Labrador"])
    ])
    assert result == []


def test_past_meetup_excluded():
    result = dijkstra_recommend(["Labrador"], "user1", [
        meetup("m1", breeds=["Labrador"], dt=PAST)
    ])
    assert result == []


def test_case_insensitive_match():
    result = dijkstra_recommend(["labrador"], "user1", [
        meetup("m1", breeds=["LABRADOR"])
    ])
    assert len(result) == 1


def test_empty_user_breeds_returns_open_meetups_only():
    # When user has no dogs registered, only open meetups are directly reachable.
    # "breed" meetup has Poodle at a far date/different location → no path to it.
    result = dijkstra_recommend([], "user1", [
        meetup("open",  breeds=[],         dt=SOON,  location="Park"),
        meetup("breed", breeds=["Poodle"], dt=FAR,   location="Beach"),
    ])
    assert len(result) == 1
    assert result[0]["meetupId"] == "open"


# ---------------------------------------------------------------------------
# Sorting / ranking
# ---------------------------------------------------------------------------

def test_exact_match_ranked_before_open():
    result = dijkstra_recommend(["Labrador"], "user1", [
        meetup("open",  breeds=[],           dt=SOON),
        meetup("match", breeds=["Labrador"], dt=LATER),
    ])
    assert result[0]["meetupId"] == "match"
    assert result[1]["meetupId"] == "open"


def test_exact_match_ranked_before_size_group():
    result = dijkstra_recommend(["Labrador"], "user1", [
        meetup("size",  breeds=["Golden Retriever"], dt=SOON),
        meetup("exact", breeds=["Labrador"],          dt=LATER),
    ])
    assert result[0]["meetupId"] == "exact"


# ---------------------------------------------------------------------------
# Size groups
# ---------------------------------------------------------------------------

def test_same_size_group_included():
    # Labrador = large, Golden Retriever = large → size-group match → included
    result = dijkstra_recommend(["Labrador"], "user1", [
        meetup("m1", breeds=["Golden Retriever"])
    ])
    assert len(result) == 1
    assert result[0]["meetupId"] == "m1"


# ---------------------------------------------------------------------------
# Date bonus
# ---------------------------------------------------------------------------

def test_sooner_meetup_scores_higher():
    result = dijkstra_recommend(["Labrador"], "user1", [
        meetup("far",  breeds=["Labrador"], dt=FAR),
        meetup("soon", breeds=["Labrador"], dt=SOON),
    ])
    assert result[0]["meetupId"] == "soon"
    assert result[0]["score"] > result[1]["score"]


def test_score_capped_at_1():
    result = dijkstra_recommend(["Labrador"], "user1", [
        meetup("m1", breeds=["Labrador"], dt=SOON)
    ])
    assert result[0]["score"] <= 1.0


# ---------------------------------------------------------------------------
# Second-hop (graph traversal — the key Dijkstra feature)
# ---------------------------------------------------------------------------

def test_second_hop_meetup_recommended():
    """
    meetupA  — exact breed match for user (Labrador)
    meetupB  — shares 'Mixed Breed' with meetupA but not with user directly
    Dijkstra path: user → A → B  (B recommended via graph traversal)
    """
    result = dijkstra_recommend(["Labrador"], "user1", [
        meetup("A", breeds=["Labrador", "Mixed Breed"], dt=FAR),
        meetup("B", breeds=["Mixed Breed"],             dt=FAR),
    ])
    ids = [r["meetupId"] for r in result]
    assert "A" in ids
    assert "B" in ids


def test_second_hop_scores_lower_than_direct():
    """
    Direct match should always score higher than a second-hop match.
    """
    result = dijkstra_recommend(["Labrador"], "user1", [
        meetup("direct", breeds=["Labrador", "Mixed Breed"], dt=FAR),
        meetup("second", breeds=["Mixed Breed"],             dt=FAR),
    ])
    direct_score = next(r["score"] for r in result if r["meetupId"] == "direct")
    second_score = next(r["score"] for r in result if r["meetupId"] == "second")
    assert direct_score > second_score
