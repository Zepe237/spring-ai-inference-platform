"""PyTorch scoring model.

The baseline is a single linear layer + sigmoid over the normalised linguistic
features, initialised with rubric weights (length, sentence structure, vocabulary,
cohesion, lexical richness). It is a transparent baseline, not a validated TCF scorer:
train it on graded essays and load the checkpoint with MODEL_WEIGHTS_PATH.
"""
from __future__ import annotations

import os
from dataclasses import dataclass

import torch
from torch import nn

from .features import TextFeatures

FEATURE_NAMES = ["length", "sentence_structure", "vocabulary", "cohesion", "lexical_richness"]
RUBRIC_WEIGHTS = [1.6, 1.0, 1.4, 1.2, 0.8]
RUBRIC_BIAS = -4.5
MAX_SCORE = 20.0

# Indicative bands for the TCF written expression score out of 20.
BANDS = [(16, "C2"), (14, "C1"), (10, "B2"), (6, "B1"), (4, "A2"), (0, "A1")]


class WritingScorer(nn.Module):
    def __init__(self, n_features: int = len(FEATURE_NAMES)) -> None:
        super().__init__()
        self.linear = nn.Linear(n_features, 1)

    def forward(self, x: torch.Tensor) -> torch.Tensor:
        return torch.sigmoid(self.linear(x)).squeeze(-1) * MAX_SCORE


@dataclass(frozen=True)
class Prediction:
    score: float
    band: str
    contributions: dict[str, float]


def band_for(score: float) -> str:
    for threshold, band in BANDS:
        if score >= threshold:
            return band
    return "A1"


def build_model(weights_path: str | None = None) -> WritingScorer:
    model = WritingScorer()
    path = weights_path or os.getenv("MODEL_WEIGHTS_PATH")
    if path:
        model.load_state_dict(torch.load(path, map_location="cpu", weights_only=True))
    else:
        with torch.no_grad():
            model.linear.weight.copy_(torch.tensor([RUBRIC_WEIGHTS]))
            model.linear.bias.fill_(RUBRIC_BIAS)
    model.eval()
    return model


@torch.inference_mode()
def predict(model: WritingScorer, features: TextFeatures) -> Prediction:
    x = torch.tensor([features.vector()], dtype=torch.float32)
    score = float(model(x)[0])
    weights = model.linear.weight[0]
    contributions = {name: round(float(w * v), 3) for name, w, v in zip(FEATURE_NAMES, weights, x[0])}
    score = round(score, 1)
    return Prediction(score=score, band=band_for(score), contributions=contributions)
