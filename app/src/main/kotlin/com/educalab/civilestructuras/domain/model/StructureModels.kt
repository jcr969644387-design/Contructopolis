package com.educalab.civilestructuras.domain.model

/**
 * Modelos de dominio PUROS de Constructópolis.
 *
 * Deliberadamente no dependen de Android ni de Room: son los objetos que
 * consume [com.educalab.civilestructuras.domain.logic.StructureEngine].
 * La capa data/repository se encarga de convertir entre las entidades Room
 * (persistencia) y estos modelos (reglas de negocio), así el motor se puede
 * probar en un JVM plano sin arrancar Android.
 */

/** Posición de un nodo dentro de la cuadrícula del constructor (unidades = "metros de juego"). */
data class NodePosition(val x: Int, val y: Int)

/** Tipo de apoyo de un nodo. NINGUNO = nodo libre (no ancla la estructura al suelo). */
enum class SupportType { NINGUNO, FIJO, RODILLO }

/** Material educativo simplificado disponible en el Taller. */
enum class MaterialType { MADERA, ACERO, CONCRETO }

/** Rol estructural de un miembro (pieza) dentro del diseño. */
enum class MemberRole { VIGA, COLUMNA, DIAGONAL }

/** Nodo del diseño: punto donde se pueden unir vigas, columnas o diagonales. */
data class StructureNodeModel(
    val id: String,
    val position: NodePosition,
    val support: SupportType = SupportType.NINGUNO
)

/** Miembro (pieza) que conecta dos nodos con un material y un rol estructural. */
data class StructureMemberModel(
    val id: String,
    val nodeAId: String,
    val nodeBId: String,
    val material: MaterialType = MaterialType.ACERO,
    val role: MemberRole = MemberRole.VIGA
)

/** Carga aplicada sobre un nodo. isLateral=true simula viento/sismo (empuje horizontal). */
data class LoadModel(
    val id: String,
    val nodeId: String,
    val magnitude: Int,
    val isLateral: Boolean = false
)

/** Diseño completo que el niño construye en el Constructor para un reto concreto. */
data class StructureDesign(
    val challengeId: String,
    val nodes: List<StructureNodeModel>,
    val members: List<StructureMemberModel>,
    val loads: List<LoadModel>
) {
    fun nodeById(id: String): StructureNodeModel? = nodes.firstOrNull { it.id == id }
}
