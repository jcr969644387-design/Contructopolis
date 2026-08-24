package com.educalab.civilestructuras.util

import androidx.annotation.DrawableRes
import com.educalab.civilestructuras.R

/**
 * Traduce los nombres de recurso guardados en Room (String, ej. "ic_material_madera")
 * a los drawables vectoriales reales generados en res/drawable (ver
 * tools/generate_vector_drawables.py). Se usa un mapa explícito en vez de
 * Resources.getIdentifier() para evitar reflexión y detectar en compilación
 * si falta algún icono.
 */
object IconRegistry {
    private val map: Map<String, Int> = mapOf(
        "ic_material_madera" to R.drawable.ic_material_madera,
        "ic_material_acero" to R.drawable.ic_material_acero,
        "ic_material_concreto" to R.drawable.ic_material_concreto,
        "ic_challenge_viga" to R.drawable.ic_challenge_viga,
        "ic_challenge_columna" to R.drawable.ic_challenge_columna,
        "ic_challenge_torre" to R.drawable.ic_challenge_torre,
        "ic_challenge_carga" to R.drawable.ic_challenge_carga,
        "ic_challenge_reto" to R.drawable.ic_challenge_reto,
        "ic_badge_primer_ladrillo" to R.drawable.ic_badge_primer_ladrillo,
        "ic_badge_maestra_acero" to R.drawable.ic_badge_maestra_acero,
        "ic_badge_arquitecta_madera" to R.drawable.ic_badge_arquitecta_madera,
        "ic_badge_torre_cielo" to R.drawable.ic_badge_torre_cielo,
        "ic_badge_triangulacion" to R.drawable.ic_badge_triangulacion,
        "ic_badge_ahorradora" to R.drawable.ic_badge_ahorradora,
        "ic_badge_viento" to R.drawable.ic_badge_viento,
        "ic_badge_capitulo1" to R.drawable.ic_badge_capitulo1,
        "ic_badge_maestra_constructora" to R.drawable.ic_badge_maestra_constructora,
        "ic_badge_perfeccionista" to R.drawable.ic_badge_perfeccionista,
        "ic_blueprint_reward" to R.drawable.ic_blueprint_reward
    )

    @DrawableRes
    fun resolve(name: String): Int = map[name] ?: R.drawable.ic_blueprint_reward
}
