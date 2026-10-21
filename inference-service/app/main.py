"""FastAPI inference service for TCF written expression analysis."""
from __future__ import annotations

from fastapi import FastAPI
from pydantic import BaseModel, Field

from . import __version__
from .features import extract
from .feedback import feedback_for
from .model import build_model, predict

app = FastAPI(title="TCF Writing Inference Service", version=__version__)
model = build_model()


class AnalyzeRequest(BaseModel):
    text: str = Field(min_length=1, max_length=5000)


class AnalyzeResponse(BaseModel):
    score: float = Field(description="Indicative score out of 20")
    band: str = Field(description="Indicative CEFR band (A1 to C2)")
    features: dict
    contributions: dict[str, float]
    feedback: list[str]
    model_version: str


@app.get("/health")
def health() -> dict:
    return {"status": "UP", "model_version": __version__}


@app.post("/analyze", response_model=AnalyzeResponse)
def analyze(request: AnalyzeRequest) -> AnalyzeResponse:
    features = extract(request.text)
    prediction = predict(model, features)
    return AnalyzeResponse(
        score=prediction.score,
        band=prediction.band,
        features=features.as_dict(),
        contributions=prediction.contributions,
        feedback=feedback_for(features),
        model_version=__version__,
    )
