from fastapi.testclient import TestClient

from app.features import extract
from app.main import app
from app.model import band_for, build_model, predict

client = TestClient(app)

GOOD_TEXT = (
    "Je vous écris pour partager mon avis sur le télétravail. D'abord, il permet de gagner "
    "beaucoup de temps, car on évite les longs trajets quotidiens. Ensuite, les employés "
    "peuvent mieux organiser leur journée et concilier vie professionnelle et vie familiale. "
    "Cependant, le travail à distance présente aussi des inconvénients importants. En effet, "
    "certaines personnes se sentent isolées et perdent le contact avec leurs collègues. "
    "De plus, la frontière entre le bureau et la maison devient parfois difficile à respecter. "
    "Par conséquent, je pense qu'une formule hybride représente la meilleure solution : deux "
    "ou trois jours à la maison et le reste au bureau. Ainsi, chacun profite des avantages "
    "sans subir l'isolement. En conclusion, le télétravail est une évolution positive, à "
    "condition de l'encadrer intelligemment et d'écouter les besoins réels des équipes."
)


def test_extract_counts_words_sentences_and_connectors():
    f = extract("D'abord, je mange. Ensuite, je dors. Enfin, je travaille.")
    assert f.word_count == 9
    assert f.sentence_count == 3
    assert f.connector_count == 3


def test_band_thresholds():
    assert band_for(17) == "C2"
    assert band_for(12) == "B2"
    assert band_for(5) == "A2"
    assert band_for(1) == "A1"


def test_structured_text_scores_higher_than_short_text():
    model = build_model()
    good = predict(model, extract(GOOD_TEXT))
    short = predict(model, extract("Le télétravail est bien. Je aime."))
    assert good.score > short.score
    assert 0 <= short.score <= 20 and 0 <= good.score <= 20


def test_analyze_endpoint_returns_score_band_and_feedback():
    response = client.post("/analyze", json={"text": GOOD_TEXT})
    assert response.status_code == 200
    body = response.json()
    assert 0 <= body["score"] <= 20
    assert body["band"] in {"A1", "A2", "B1", "B2", "C1", "C2"}
    assert body["features"]["word_count"] > 100
    assert body["feedback"]
    assert set(body["contributions"]) == {"length", "sentence_structure", "vocabulary", "cohesion", "lexical_richness"}


def test_short_text_gets_length_feedback():
    body = client.post("/analyze", json={"text": "Bonjour, je suis content."}).json()
    assert any("trop court" in tip for tip in body["feedback"])


def test_empty_text_is_rejected():
    assert client.post("/analyze", json={"text": ""}).status_code == 422


def test_health():
    assert client.get("/health").json()["status"] == "UP"
