#!/usr/bin/env python3
"""
Generador de datos semilla de Constructópolis.

Produce app/src/main/assets/seed/{materials,challenges,badges}.json a partir
de plantillas paramétricas. Los 40 retos NO son texto de relleno: cada uno
tiene una cuadrícula, presupuesto, materiales permitidos y objetivos
distintos que el StructureEngine evalúa de verdad (altura mínima,
presupuesto máximo, triangulación mínima, peso máximo, estabilidad mínima,
resistencia a carga lateral). Se ejecuta una sola vez en fase de desarrollo;
el resultado versionado es lo que la app realmente empaqueta y lee (Seeder.kt
lee estos JSON de assets/, no ejecuta este script en runtime).
"""
import json
import os

OUT_DIR = os.path.join(os.path.dirname(__file__), "..", "app", "src", "main", "assets", "seed")
os.makedirs(OUT_DIR, exist_ok=True)

# ---------------------------------------------------------------------------
# MATERIALES — deben coincidir exactamente con StructureEngine.MATERIALS
# ---------------------------------------------------------------------------
materials = [
    {
        "id": "MADERA", "displayName": "Madera de Andamio", "colorHex": "#C68B4A",
        "description": "Ligera y barata, ideal para empezar. Se dobla si la pieza es muy larga.",
        "strength": 40.0, "weight": 3.0, "cost": 5, "iconRes": "ic_material_madera"
    },
    {
        "id": "ACERO", "displayName": "Viga de Acero Nova", "colorHex": "#5B7C99",
        "description": "Fuerte y confiable, el favorito de las torres altas. Cuesta más que la madera.",
        "strength": 90.0, "weight": 8.0, "cost": 12, "iconRes": "ic_material_acero"
    },
    {
        "id": "CONCRETO", "displayName": "Bloque de Concreto", "colorHex": "#8D8B85",
        "description": "Pesado y resistente a cargas grandes, perfecto para columnas cortas.",
        "strength": 70.0, "weight": 15.0, "cost": 8, "iconRes": "ic_material_concreto"
    },
]

# ---------------------------------------------------------------------------
# INSIGNIAS — deben coincidir con BadgeEngine.BadgeId
# ---------------------------------------------------------------------------
badges = [
    {"id": "PRIMER_LADRILLO", "title": "Primer Ladrillo", "tier": 1, "iconRes": "ic_badge_primer_ladrillo",
     "description": "Completaste tu primer reto en el Taller. ¡El comienzo de una gran obra!"},
    {"id": "MAESTRA_DEL_ACERO", "title": "Maestra del Acero", "tier": 2, "iconRes": "ic_badge_maestra_acero",
     "description": "Aprobaste 5 retos construyendo solo con Viga de Acero Nova."},
    {"id": "ARQUITECTA_DE_MADERA", "title": "Arquitecta de Madera", "tier": 2, "iconRes": "ic_badge_arquitecta_madera",
     "description": "Aprobaste 5 retos construyendo solo con Madera de Andamio."},
    {"id": "TORRE_AL_CIELO", "title": "Torre al Cielo", "tier": 2, "iconRes": "ic_badge_torre_cielo",
     "description": "Construiste una estructura de 20 metros o más de altura."},
    {"id": "TRIANGULACION_PERFECTA", "title": "Triangulación Perfecta", "tier": 2, "iconRes": "ic_badge_triangulacion",
     "description": "Lograste que la mitad o más de tus piezas fueran diagonales de refuerzo."},
    {"id": "INGENIERA_AHORRADORA", "title": "Ingeniera Ahorradora", "tier": 2, "iconRes": "ic_badge_ahorradora",
     "description": "Aprobaste un reto gastando la mitad o menos del presupuesto disponible."},
    {"id": "RESISTENTE_AL_VIENTO", "title": "Resistente al Viento", "tier": 3, "iconRes": "ic_badge_viento",
     "description": "Superaste 3 retos que incluían cargas laterales de viento."},
    {"id": "CAPITULO_UNO_COMPLETO", "title": "Cimientos Dominados", "tier": 3, "iconRes": "ic_badge_capitulo1",
     "description": "Completaste todos los retos del capítulo Cimientos."},
    {"id": "MAESTRA_CONSTRUCTORA", "title": "Maestra Constructora", "tier": 4, "iconRes": "ic_badge_maestra_constructora",
     "description": "Completaste 20 retos en total. ¡Eres toda una ingeniera junior!"},
    {"id": "PERFECCIONISTA", "title": "Perfeccionista", "tier": 4, "iconRes": "ic_badge_perfeccionista",
     "description": "Conseguiste 3 estrellas en 10 retos distintos."},
]

# ---------------------------------------------------------------------------
# RETOS (40) — organizados en 5 capítulos temáticos del Taller
# ---------------------------------------------------------------------------
CHAPTERS = [
    {"num": 1, "name": "Cimientos", "category": "VIGA",
     "briefing": "La Ingeniera Nova necesita que aprendas a conectar tus piezas al suelo antes de construir nada grande."},
    {"num": 2, "name": "Vigas y Columnas", "category": "COLUMNA",
     "briefing": "Es hora de sostener peso de verdad: elige bien tus columnas para no romper el taller."},
    {"num": 3, "name": "Torres", "category": "TORRE",
     "briefing": "¡Construye hacia el cielo! Cuanto más alto, más cuidado hay que tener con la esbeltez."},
    {"num": 4, "name": "Cargas y Viento", "category": "CARGA",
     "briefing": "El viento empuja de lado. Sin diagonales, hasta la torre más bonita puede tambalear."},
    {"num": 5, "name": "Gran Taller de Retos", "category": "RETO",
     "briefing": "Retos combinados: presupuesto, altura, viento y peso a la vez. ¡Demuestra todo lo aprendido!"},
]

GOAL_LIBRARY = {
    "VIGA": [("PRESUPUESTO_MAXIMO", [120, 160, 200, 260, 300, 340, 380, 420])],
    "COLUMNA": [("ALTURA_MINIMA", [6, 8, 8, 10, 10, 12, 12, 14]),
                ("PESO_MAXIMO", [80, 90, 100, 110, 120, 130, 140, 150])],
    "TORRE": [("ALTURA_MINIMA", [10, 12, 14, 16, 18, 20, 22, 24]),
              ("TRIANGULACION_MINIMA", [10, 15, 15, 20, 20, 25, 25, 30])],
    "CARGA": [("RESISTIR_CARGA_LATERAL", [1] * 8),
              ("TRIANGULACION_MINIMA", [20, 25, 25, 30, 30, 35, 35, 40])],
    "RETO": [("ESTABILIDAD_MINIMA", [55, 60, 60, 65, 65, 70, 70, 75]),
             ("PRESUPUESTO_MAXIMO", [300, 350, 400, 450, 500, 550, 600, 650])],
}

MATERIAL_SETS = [
    ["MADERA"], ["MADERA", "ACERO"], ["ACERO"], ["MADERA", "ACERO", "CONCRETO"],
    ["ACERO", "CONCRETO"], ["MADERA", "CONCRETO"], ["MADERA", "ACERO", "CONCRETO"], ["ACERO"],
]

TITLES = {
    "VIGA": ["Primer Puente", "Pasarela del Taller", "Balcón de Nova", "Puente de Andamios",
             "Viga Maestra", "Pasarela Doble", "Puente del Río Datos", "Viga sin Miedo"],
    "COLUMNA": ["Pilar de Entrada", "Columnas Gemelas", "Soporte Central", "Columna Robusta",
                "Pilares del Depósito", "Columna de Concreto", "Soporte Triple", "Pilar Definitivo"],
    "TORRE": ["Torre Vigía", "Torre de Señales", "Torre del Faro", "Torre Estelar",
              "Torre Skyline", "Torre Gemela", "Torre Cuadrada", "Torre Corona"],
    "CARGA": ["Ráfaga Suave", "Viento del Norte", "Tormenta Ligera", "Viento Cruzado",
              "Vendaval de Prueba", "Viento y Altura", "Tormenta del Taller", "Huracán Junior"],
    "RETO": ["Reto Combinado I", "Reto Combinado II", "Desafío de Nova", "Reto del Ingeniero",
             "Gran Desafío", "Reto Final de Capítulo", "Desafío Presupuestario", "El Gran Proyecto"],
}

GRID_SIZES = [(6, 6), (6, 8), (7, 8), (7, 9), (8, 9), (8, 10), (9, 10), (9, 11)]


def build_challenges():
    challenges = []
    challenge_id_counter = 1
    for chapter in CHAPTERS:
        cat = chapter["category"]
        goal_defs = GOAL_LIBRARY[cat]
        for i in range(8):
            cid = f"c{challenge_id_counter:02d}_{cat.lower()}"
            gw, gh = GRID_SIZES[i]
            goals = []
            for goal_type, values in goal_defs:
                goals.append({"type": goal_type, "value": values[i]})
            max_budget = 500 + i * 60 if cat != "VIGA" else 400 + i * 40
            star2 = 55 + i
            star3 = 78 + i
            allowed = MATERIAL_SETS[i]
            supports = [
                {"x": 0, "y": 0, "supportType": "FIJO"},
                {"x": gw - 1, "y": 0, "supportType": "FIJO"},
            ]
            preset_loads = []
            if cat == "CARGA":
                preset_loads.append({"x": gw // 2, "y": gh - 1, "magnitude": 20 + i * 4, "isLateral": True})
            challenge = {
                "id": cid,
                "orderInChapter": i + 1,
                "worldChapter": chapter["num"],
                "title": TITLES[cat][i],
                "briefing": chapter["briefing"],
                "category": cat,
                "gridWidth": gw,
                "gridHeight": gh,
                "maxBudget": max_budget,
                "starThreshold2": star2,
                "starThreshold3": star3,
                "allowedMaterials": allowed,
                "iconRes": f"ic_challenge_{cat.lower()}",
                "goals": goals,
                "presetSupports": supports,
                "presetLoads": preset_loads,
            }
            challenges.append(challenge)
            challenge_id_counter += 1
    return challenges


def main():
    with open(os.path.join(OUT_DIR, "materials.json"), "w", encoding="utf-8") as f:
        json.dump(materials, f, ensure_ascii=False, indent=2)
    with open(os.path.join(OUT_DIR, "badges.json"), "w", encoding="utf-8") as f:
        json.dump(badges, f, ensure_ascii=False, indent=2)
    challenges = build_challenges()
    with open(os.path.join(OUT_DIR, "challenges.json"), "w", encoding="utf-8") as f:
        json.dump(challenges, f, ensure_ascii=False, indent=2)
    print(f"materials={len(materials)} badges={len(badges)} challenges={len(challenges)}")


if __name__ == "__main__":
    main()
