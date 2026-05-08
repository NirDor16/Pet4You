import os
from flask import Flask, request, jsonify
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


def check_api_key():
    return request.headers.get("X-API-Key") == API_KEY


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

    recommendations = score_meetups(dog_breeds, user_id, meetups)
    return jsonify({"recommendations": recommendations})


if __name__ == "__main__":
    app.run(debug=False)
