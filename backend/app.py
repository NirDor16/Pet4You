import heapq
import os
import time

from flask import Flask, jsonify, request
from openai import OpenAI
from dotenv import load_dotenv

load_dotenv()

app = Flask(__name__)
client = OpenAI(api_key=os.environ["OPENAI_API_KEY"])
API_KEY = os.environ["API_KEY"]

SYSTEM_PROMPT = (
    "You are a helpful pet care assistant for dog owners. "
    "You provide friendly, accurate advice about dog health, training, nutrition, and care. "
    "Always be concise and practical."
)

# ---------------------------------------------------------------------------
# Breed size groups — used for partial matching when breeds don't match exactly
# ---------------------------------------------------------------------------

BREED_SIZES = {
    "small": {
        "affenpinscher", "bichon frise", "boston terrier", "cairn terrier",
        "chihuahua", "havanese", "italian greyhound", "jack russell terrier",
        "maltese", "miniature poodle", "miniature schnauzer", "papillon",
        "pomeranian", "pug", "scottish terrier", "shih tzu", "toy poodle",
        "west highland white terrier", "yorkshire terrier",
    },
    "medium": {
        "american staffordshire terrier", "australian shepherd", "basenji",
        "basset hound", "beagle", "border collie", "bull terrier", "bulldog",
        "cavalier king charles spaniel", "chow chow", "cocker spaniel",
        "dalmatian", "english springer spaniel", "french bulldog",
        "shetland sheepdog", "shiba inu", "siberian husky",
        "staffordshire bull terrier", "vizsla", "whippet",
    },
    "large": {
        "afghan hound", "airedale terrier", "akita", "alaskan malamute",
        "belgian malinois", "bernese mountain dog", "bloodhound", "boxer",
        "collie", "doberman", "doberman pinscher", "german shepherd",
        "german shorthaired pointer", "golden", "golden retriever",
        "great dane", "great pyrenees", "greyhound", "irish setter",
        "labrador", "labrador retriever", "mastiff", "newfoundland",
        "poodle", "rottweiler", "saint bernard", "samoyed",
        "standard poodle", "weimaraner",
    },
}


def _size_group(breed):
    b = breed.lower()
    for group, breeds in BREED_SIZES.items():
        if b in breeds:
            return group
    return None


# ---------------------------------------------------------------------------
# Graph construction
# ---------------------------------------------------------------------------

def _build_graph(meetups, user_breeds, user_id, now_ms):
    """
    Weighted directed graph:
      - "user" node → meetup nodes  (direct match edges)
      - meetup node → meetup node   (shared-breed similarity edges)

    Edge weights represent "distance" (lower = more relevant).
    Meetups not reachable from "user" won't appear in Dijkstra output.
    """
    user_breed_set = {b.lower() for b in user_breeds}
    user_sizes = {_size_group(b) for b in user_breed_set} - {None}

    graph = {"user": []}
    nodes = {}   # meetupId → meetup dict
    valid = []   # all eligible meetups (not creator, not joined, not past)

    # Pass 1 — collect all eligible meetups into graph as isolated nodes
    for m in meetups:
        mid = m.get("meetupId", "")
        if not mid:
            continue
        if m.get("creatorId") == user_id:
            continue
        if user_id in m.get("participants", []):
            continue
        if m.get("dateTime", 0) <= now_ms:
            continue
        nodes[mid] = m
        graph[mid] = []
        valid.append(m)

    # Pass 2 — user → meetup direct edges
    for m in valid:
        mid = m["meetupId"]
        meetup_breeds = {b.lower() for b in m.get("dogBreeds", [])}
        meetup_sizes = {_size_group(b) for b in meetup_breeds} - {None}
        dt = m.get("dateTime", 0)

        if not meetup_breeds:
            base = 0.65          # open meetup — no breed filter
        elif user_breed_set & meetup_breeds:
            base = 0.10          # exact breed match — closest
        elif user_sizes & meetup_sizes:
            base = 0.35          # same size group — partial match
        else:
            continue             # no direct edge; still in graph for second-hop

        # Date proximity bonus: sooner meetup → smaller distance → higher score
        days = (dt - now_ms) / 86_400_000
        if days <= 7:
            date_bonus = 0.20
        elif days <= 30:
            date_bonus = 0.10
        else:
            date_bonus = 0.0

        dist = max(base - date_bonus, 0.05)
        graph["user"].append((dist, mid))

    # Pass 3 — meetup → meetup similarity edges (shared breeds only)
    for i, m1 in enumerate(valid):
        breeds1 = {b.lower() for b in m1.get("dogBreeds", [])}
        if not breeds1:
            continue  # open meetups don't propagate similarity
        mid1 = m1["meetupId"]

        for m2 in valid[i + 1:]:
            breeds2 = {b.lower() for b in m2.get("dogBreeds", [])}
            if breeds1 & breeds2:
                graph[mid1].append((0.20, m2["meetupId"]))
                graph[m2["meetupId"]].append((0.20, mid1))

    return graph, nodes


# ---------------------------------------------------------------------------
# Dijkstra — finds shortest (lowest-distance) path from "user" to each meetup
# ---------------------------------------------------------------------------

def dijkstra_recommend(dog_breeds, user_id, meetups):
    now_ms = int(time.time() * 1000)
    graph, nodes = _build_graph(meetups, dog_breeds, user_id, now_ms)

    dist = {"user": 0.0}
    heap = [(0.0, "user")]
    visited = set()

    while heap:
        d, node = heapq.heappop(heap)
        if node in visited:
            continue
        visited.add(node)
        for weight, neighbor in graph.get(node, []):
            new_dist = d + weight
            if new_dist < dist.get(neighbor, float("inf")):
                dist[neighbor] = new_dist
                heapq.heappush(heap, (new_dist, neighbor))

    # Convert distances → scores; drop anything with score ≤ 0
    results = []
    for mid, d in dist.items():
        if mid == "user":
            continue
        score = round(max(1.0 - d, 0.0), 2)
        if score <= 0.0:
            continue
        meetup = dict(nodes[mid])
        meetup["score"] = score
        results.append((d, meetup.get("dateTime", 0), meetup))

    results.sort(key=lambda x: (x[0], x[1]))
    return [item[2] for item in results]


# ---------------------------------------------------------------------------
# API helpers
# ---------------------------------------------------------------------------

def check_api_key():
    return request.headers.get("X-API-Key") == API_KEY


# ---------------------------------------------------------------------------
# Routes
# ---------------------------------------------------------------------------

@app.route("/health", methods=["GET"])
def health():
    return jsonify({"status": "ok"})


@app.route("/chat", methods=["POST"])
def chat():
    if not check_api_key():
        return jsonify({"error": "Unauthorized"}), 401

    data = request.get_json(silent=True) or {}
    message = data.get("message", "").strip()
    if not message:
        return jsonify({"error": "message is required"}), 400

    history = data.get("history", [])
    messages = [{"role": "system", "content": SYSTEM_PROMPT}]
    messages.extend(history)
    messages.append({"role": "user", "content": message})

    try:
        response = client.chat.completions.create(
            model="gpt-4o-mini",
            messages=messages,
        )
        reply = response.choices[0].message.content
        return jsonify({"reply": reply})
    except Exception as e:
        return jsonify({"error": f"OpenAI error: {e}"}), 502


@app.route("/recommend-meetups", methods=["POST"])
def recommend_meetups():
    if not check_api_key():
        return jsonify({"error": "Unauthorized"}), 401

    data = request.get_json(silent=True) or {}
    dog_breeds = data.get("dog_breeds", [])
    user_id = data.get("user_id", "")
    meetups = data.get("meetups", [])

    recommendations = dijkstra_recommend(dog_breeds, user_id, meetups)
    return jsonify({"recommendations": recommendations})


if __name__ == "__main__":
    app.run(debug=False)
