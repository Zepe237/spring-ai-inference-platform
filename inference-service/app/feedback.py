"""Actionable feedback derived from the features."""
from __future__ import annotations

from .features import TextFeatures

MIN_WORDS = 120
MAX_WORDS = 180


def feedback_for(f: TextFeatures) -> list[str]:
    tips: list[str] = []
    if f.word_count < MIN_WORDS:
        tips.append(f"Texte trop court : {f.word_count} mots, visez entre {MIN_WORDS} et {MAX_WORDS}.")
    elif f.word_count > MAX_WORDS:
        tips.append(f"Texte trop long : {f.word_count} mots, restez sous {MAX_WORDS}.")
    if f.avg_sentence_length > 28:
        tips.append("Phrases très longues : découpez-les pour gagner en clarté.")
    elif 0 < f.avg_sentence_length < 8:
        tips.append("Phrases très courtes : reliez certaines idées entre elles.")
    if f.connector_count < 3:
        tips.append("Ajoutez des connecteurs logiques (d'abord, cependant, en effet, donc, en conclusion).")
    if f.lexical_diversity < 0.5 and f.word_count >= 40:
        tips.append("Vocabulaire répétitif : variez les mots et utilisez des synonymes.")
    if not tips:
        tips.append("Bonne structure générale. Relisez l'orthographe et les accords avant de soumettre.")
    return tips
