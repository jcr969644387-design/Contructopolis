#!/usr/bin/env python3
"""
Genera los vector drawables de identidad visual de Constructópolis:
materiales, iconos de categoría de reto e insignias ilustradas. Se prioriza
(según la especificación) vector drawables sobre iconos de Material Design
para dar identidad propia; los Material Icons se usan solo como apoyo en la
UI (botones, navegación), nunca como sustituto de toda la identidad visual.
"""
import math
import os

OUT = os.path.join(os.path.dirname(__file__), "..", "app", "src", "main", "res", "drawable")
os.makedirs(OUT, exist_ok=True)

VB = 24  # viewport 24x24, estándar Android


def rect_path(x, y, w, h, rx=0):
    return f"M{x},{y} h{w} v{h} h{-w} z"


def circle_path(cx, cy, r):
    return f"M{cx - r},{cy} a{r},{r} 0 1,0 {2*r},0 a{r},{r} 0 1,0 {-2*r},0"


def triangle_path(x1, y1, x2, y2, x3, y3):
    return f"M{x1},{y1} L{x2},{y2} L{x3},{y3} Z"


def polyline_stroke(points, width, color):
    pts = " L".join(f"{x},{y}" for x, y in points)
    return f'<path android:strokeColor="{color}" android:strokeWidth="{width}" android:fillColor="#00000000" android:pathData="M{pts}"/>'


def write_vector(filename, paths, size=108):
    body = "\n    ".join(paths)
    content = f'''<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="{size}dp" android:height="{size}dp"
    android:viewportWidth="{VB}" android:viewportHeight="{VB}">
    {body}
</vector>
'''
    with open(os.path.join(OUT, filename), "w", encoding="utf-8") as f:
        f.write(content)


def path(d, color, opacity=None):
    op = f' android:fillAlpha="{opacity}"' if opacity else ""
    return f'<path android:fillColor="{color}"{op} android:pathData="{d}"/>'


# ---------------------------------------------------------------------------
# MATERIALES
# ---------------------------------------------------------------------------
def material_madera():
    paths = [
        path(rect_path(2, 7, 20, 10), "#C68B4A"),
        path(f"M2,10 h20 v1.4 h-20 z", "#A9713A"),
        path(f"M2,14 h20 v1.4 h-20 z", "#A9713A"),
        path(circle_path(7, 12, 0.8), "#8A5A2C"),
        path(circle_path(16, 16, 0.8), "#8A5A2C"),
    ]
    write_vector("ic_material_madera.xml", paths)


def material_acero():
    # Perfil de viga en I estilizado
    paths = [
        path(rect_path(4, 3, 16, 3), "#5B7C99"),
        path(rect_path(4, 18, 16, 3), "#5B7C99"),
        path(rect_path(10.5, 6, 3, 12), "#3D5A73"),
    ]
    write_vector("ic_material_acero.xml", paths)


def material_concreto():
    paths = [path(rect_path(2, 5, 20, 14), "#8D8B85")]
    # Motas de textura
    import random
    random.seed(7)
    for _ in range(14):
        x = round(random.uniform(3, 20), 1)
        y = round(random.uniform(6, 17), 1)
        paths.append(path(circle_path(x, y, 0.35), "#6E6C67"))
    write_vector("ic_material_concreto.xml", paths)


# ---------------------------------------------------------------------------
# CATEGORÍAS DE RETO
# ---------------------------------------------------------------------------
def challenge_viga():
    paths = [
        path(rect_path(2, 11, 20, 3), "#3D5A73"),
        path(rect_path(4, 14, 2, 6), "#8D8B85"),
        path(rect_path(18, 14, 2, 6), "#8D8B85"),
    ]
    write_vector("ic_challenge_viga.xml", paths)


def challenge_columna():
    paths = [
        path(rect_path(9, 2, 6, 2), "#5B7C99"),
        path(rect_path(10, 4, 4, 16), "#3D5A73"),
        path(rect_path(9, 20, 6, 2), "#5B7C99"),
    ]
    write_vector("ic_challenge_columna.xml", paths)


def challenge_torre():
    paths = [
        triangle_path(12, 2, 20, 21, 4, 21),
        path(rect_path(9, 9, 6, 1.2), "#FFC93C"),
        path(rect_path(7, 14, 10, 1.2), "#FFC93C"),
    ]
    result = [path(paths[0], "#FF6B1A")] + paths[1:]
    write_vector("ic_challenge_torre.xml", result)


def challenge_carga():
    paths = [
        polyline_stroke([(3, 8), (9, 8), (7, 6)], 1.6, "#5B7C99"),
        polyline_stroke([(3, 8), (9, 8), (7, 10)], 1.6, "#5B7C99"),
        polyline_stroke([(6, 14), (18, 14), (16, 12)], 1.8, "#FF6B1A"),
        polyline_stroke([(6, 14), (18, 14), (16, 16)], 1.8, "#FF6B1A"),
        polyline_stroke([(4, 20), (14, 20), (12, 18)], 1.4, "#5B7C99"),
        polyline_stroke([(4, 20), (14, 20), (12, 22)], 1.4, "#5B7C99"),
    ]
    write_vector("ic_challenge_carga.xml", paths)


def challenge_reto():
    # Estrella de 5 puntas
    cx, cy, r_out, r_in = 12, 12, 10, 4.2
    pts = []
    for i in range(10):
        angle = math.pi / 2 + i * math.pi / 5
        r = r_out if i % 2 == 0 else r_in
        pts.append((cx + r * math.cos(angle), cy - r * math.sin(angle)))
    d = "M" + " L".join(f"{x:.1f},{y:.1f}" for x, y in pts) + " Z"
    write_vector("ic_challenge_reto.xml", [path(d, "#FFC93C")])


# ---------------------------------------------------------------------------
# INSIGNIAS (medalla circular + símbolo interno distinto por insignia)
# ---------------------------------------------------------------------------
def badge_medal(filename, ring_color, inner_paths):
    base = [
        path(circle_path(12, 12, 10.5), ring_color),
        path(circle_path(12, 12, 8), "#FFFDF8"),
    ] + inner_paths
    write_vector(filename, base)


def badge_primer_ladrillo():
    inner = [path(rect_path(8, 10, 8, 5), "#C68B4A"), path(rect_path(8, 10, 8, 1.2), "#A9713A")]
    badge_medal("ic_badge_primer_ladrillo.xml", "#FF6B1A", inner)


def badge_maestra_acero():
    inner = [path(rect_path(6, 8, 12, 2), "#5B7C99"), path(rect_path(6, 14, 12, 2), "#5B7C99"), path(rect_path(11, 8, 2, 8), "#3D5A73")]
    badge_medal("ic_badge_maestra_acero.xml", "#5B7C99", inner)


def badge_arquitecta_madera():
    inner = [path(rect_path(7, 9, 10, 6), "#C68B4A"), path(rect_path(7, 11.5, 10, 1), "#8A5A2C")]
    badge_medal("ic_badge_arquitecta_madera.xml", "#C68B4A", inner)


def badge_torre_cielo():
    inner = [path(triangle_path(12, 6, 17, 18, 7, 18), "#FF6B1A")]
    badge_medal("ic_badge_torre_cielo.xml", "#FFC93C", inner)


def badge_triangulacion():
    inner = [path(triangle_path(12, 7, 17, 17, 7, 17), "#3FAE5C")]
    badge_medal("ic_badge_triangulacion.xml", "#3FAE5C", inner)


def badge_ahorradora():
    inner = [path(circle_path(12, 12, 5), "#FFC93C"), path("M11,9 v6 M9.5,10.2 h3.2 M9.5,13.8 h3.2", "#8A6A00")]
    badge_medal("ic_badge_ahorradora.xml", "#FFC93C", inner)


def badge_resistente_viento():
    inner = [
        polyline_stroke([(6, 9), (15, 9)], 1.6, "#5B7C99"),
        polyline_stroke([(6, 13), (17, 13)], 1.6, "#5B7C99"),
        polyline_stroke([(6, 17), (13, 17)], 1.6, "#5B7C99"),
    ]
    badge_medal("ic_badge_viento.xml", "#5B7C99", inner)


def badge_capitulo1():
    inner = [path(rect_path(7, 7, 10, 10), "#3D5A73"), path(rect_path(9.5, 9.5, 5, 5), "#FFFDF8")]
    badge_medal("ic_badge_capitulo1.xml", "#3D5A73", inner)


def badge_maestra_constructora():
    inner = [path(triangle_path(12, 6, 18, 18, 6, 18), "#FF6B1A"), path(rect_path(10.5, 18, 3, 2), "#8D8B85")]
    badge_medal("ic_badge_maestra_constructora.xml", "#FF6B1A", inner)


def badge_perfeccionista():
    cx, cy, r_out, r_in = 12, 12, 6, 2.6
    pts = []
    for i in range(10):
        angle = math.pi / 2 + i * math.pi / 5
        r = r_out if i % 2 == 0 else r_in
        pts.append((cx + r * math.cos(angle), cy - r * math.sin(angle)))
    d = "M" + " L".join(f"{x:.1f},{y:.1f}" for x, y in pts) + " Z"
    badge_medal("ic_badge_perfeccionista.xml", "#FFC93C", [path(d, "#FF6B1A")])


def blueprint_reward():
    paths = [
        path(rect_path(3, 3, 18, 18), "#1B2A38"),
        polyline_stroke([(6, 8), (18, 8)], 0.6, "#5B7C99"),
        polyline_stroke([(6, 12), (14, 12)], 0.6, "#5B7C99"),
        polyline_stroke([(6, 16), (16, 16)], 0.6, "#5B7C99"),
        path(circle_path(17, 17, 2), "#FFC93C"),
    ]
    write_vector("ic_blueprint_reward.xml", paths)


def main():
    material_madera(); material_acero(); material_concreto()
    challenge_viga(); challenge_columna(); challenge_torre(); challenge_carga(); challenge_reto()
    badge_primer_ladrillo(); badge_maestra_acero(); badge_arquitecta_madera(); badge_torre_cielo()
    badge_triangulacion(); badge_ahorradora(); badge_resistente_viento(); badge_capitulo1()
    badge_maestra_constructora(); badge_perfeccionista()
    blueprint_reward()
    count = len(os.listdir(OUT))
    print(f"vector drawables generados en {OUT} (total en carpeta: {count})")


if __name__ == "__main__":
    main()
