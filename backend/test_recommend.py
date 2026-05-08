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
