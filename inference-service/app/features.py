"""Linguistic features extracted from a French text.

Every feature is normalised to roughly [0, 1] so the scoring model can combine them.
"""
from __future__ import annotations

import re
from dataclasses import dataclass, asdict

WORD_RE = re.compile(r"[A-Za-zÀ-ÖØ-öø-ÿŒœÆæ'’-]+")
SENTENCE_RE = re.compile(r"[^.!?…]+[.!?…]*")

# Discourse connectors expected in a structured TCF written answer.
CONNECTORS = {
    "d'abord", "ensuite", "puis", "enfin", "cependant", "pourtant", "toutefois",
    "néanmoins", "donc", "ainsi", "alors", "car", "parce", "puisque", "mais",
    "en effet", "par ailleurs", "de plus", "en revanche", "en conclusion",
    "par conséquent", "bien que", "afin", "pour conclure", "premièrement", "deuxièmement",
}


@dataclass(frozen=True)
class TextFeatures:
    word_count: int
    sentence_count: int
    avg_sentence_length: float
    lexical_diversity: float
    connector_count: int
    long_word_ratio: float

    def as_dict(self) -> dict:
        return asdict(self)

    def vector(self) -> list[float]:
        """Normalised feature vector fed to the scoring model."""
        return [
            min(self.word_count / 180.0, 1.0),                       # TCF task 3 expects ~120-180 words
            1.0 - min(abs(self.avg_sentence_length - 17.0) / 17.0, 1.0),  # readable sentences
            self.lexical_diversity,
            min(self.connector_count / 5.0, 1.0),
            min(self.long_word_ratio / 0.3, 1.0),
        ]


def words(text: str) -> list[str]:
    return [w.lower() for w in WORD_RE.findall(text)]


def sentences(text: str) -> list[str]:
    return [s.strip() for s in SENTENCE_RE.findall(text) if s.strip()]


def extract(text: str) -> TextFeatures:
    tokens = words(text)
    sents = sentences(text)
    word_count = len(tokens)
    sentence_count = max(len(sents), 1) if word_count else 0
    lowered = " " + " ".join(tokens) + " "
    connectors = sum(lowered.count(" " + c + " ") for c in CONNECTORS)
    return TextFeatures(
        word_count=word_count,
        sentence_count=sentence_count,
        avg_sentence_length=round(word_count / sentence_count, 2) if sentence_count else 0.0,
        lexical_diversity=round(len(set(tokens)) / word_count, 3) if word_count else 0.0,
        connector_count=connectors,
        long_word_ratio=round(sum(1 for t in tokens if len(t) >= 8) / word_count, 3) if word_count else 0.0,
    )
