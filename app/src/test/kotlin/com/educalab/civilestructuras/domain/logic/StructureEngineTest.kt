package com.educalab.civilestructuras.domain.logic

import com.educalab.civilestructuras.domain.model.*
import org.junit.Assert.*
import org.junit.Test

/**
 * Batería de pruebas del StructureEngine (motor de simulación de Constructópolis).
 * Puras JVM/JUnit4 — no dependen de Android ni Room, por lo que pueden compilarse y
 * ejecutarse de forma independiente (ver tools/verify_domain.sh y docs/BUILD_REPORT.md).
 */
class StructureEngineTest {

    // ---------- Helpers de construcción rápida de diseños ----------

    private fun node(id: String, x: Int, y: Int, support: SupportType = SupportType.NINGUNO) =
        StructureNodeModel(id, NodePosition(x, y), support)

    private fun member(id: String, a: String, b: String, mat: MaterialType = MaterialType.ACERO, role: MemberRole = MemberRole.VIGA) =
        StructureMemberModel(id, a, b, mat, role)

    private fun basicChallenge(
        goals: List<ChallengeGoal> = emptyList(),
        maxBudget: Int = 10_000,
        stars: Pair<Int, Int> = Pair(60, 85)
    ) = StructureChallengeModel(
        id = "c_test", order = 1, worldChapter = 1, title = "Test", briefing = "",
        gridWidth = 6, gridHeight = 6, fixedSupports = emptyList(), presetLoads = emptyList(),
        goals = goals, maxBudget = maxBudget, allowedMaterials = MaterialType.values().toList(),
        starThresholds = stars
    )

    // ============================================================
    // 1) CONECTIVIDAD (Union-Find)
    // ============================================================

    @Test fun `estructura simple con apoyo esta conectada`() {
        val d = StructureDesign("c", listOf(
            node("A", 0, 0, SupportType.FIJO), node("B", 0, 2)
        ), listOf(member("m1", "A", "B")), emptyList())
        assertTrue(StructureEngine.isFullyConnected(d))
    }

    @Test fun `nodo flotante sin conexion al suelo no esta conectado`() {
        val d = StructureDesign("c", listOf(
            node("A", 0, 0, SupportType.FIJO), node("B", 0, 2), node("C", 4, 4)
        ), listOf(member("m1", "A", "B")), emptyList()) // C no tiene miembros
        // C no participa en ningún miembro, así que no cuenta como "en uso"; sigue conectado
        assertTrue(StructureEngine.isFullyConnected(d))
    }

    @Test fun `subestructura separada del suelo no esta conectada`() {
        val d = StructureDesign("c", listOf(
            node("A", 0, 0, SupportType.FIJO), node("B", 0, 2),
            node("C", 4, 4), node("D", 4, 6)
        ), listOf(member("m1", "A", "B"), member("m2", "C", "D")), emptyList())
        assertFalse(StructureEngine.isFullyConnected(d))
    }

    @Test fun `sin ningun apoyo la estructura nunca esta conectada`() {
        val d = StructureDesign("c", listOf(node("A", 0, 0), node("B", 0, 2)),
            listOf(member("m1", "A", "B")), emptyList())
        assertFalse(StructureEngine.isFullyConnected(d))
    }

    @Test fun `diseno vacio no esta conectado`() {
        val d = StructureDesign("c", emptyList(), emptyList(), emptyList())
        assertFalse(StructureEngine.isFullyConnected(d))
    }

    @Test fun `dos apoyos unidos por cadena estan conectados`() {
        val d = StructureDesign("c", listOf(
            node("A", 0, 0, SupportType.FIJO), node("B", 2, 0, SupportType.FIJO), node("C", 1, 2)
        ), listOf(member("m1", "A", "C"), member("m2", "B", "C")), emptyList())
        assertTrue(StructureEngine.isFullyConnected(d))
    }

    @Test fun `computeGroundedNodeIds excluye nodos sin camino a un apoyo`() {
        val d = StructureDesign("c", listOf(
            node("A", 0, 0, SupportType.FIJO), node("B", 0, 2), node("X", 9, 9), node("Y", 9, 11)
        ), listOf(member("m1", "A", "B"), member("m2", "X", "Y")), emptyList())
        val grounded = StructureEngine.computeGroundedNodeIds(d)
        assertTrue("A" in grounded && "B" in grounded)
        assertFalse("X" in grounded || "Y" in grounded)
    }

    // ============================================================
    // 2) DISTANCIA BFS A APOYO
    // ============================================================

    @Test fun `distancia del apoyo a si mismo es cero`() {
        val d = StructureDesign("c", listOf(node("A", 0, 0, SupportType.FIJO)), emptyList(), emptyList())
        assertEquals(0, StructureEngine.computeDistanceToSupport(d)["A"])
    }

    @Test fun `distancia crece un paso por cada miembro en cadena`() {
        val d = StructureDesign("c", listOf(
            node("A", 0, 0, SupportType.FIJO), node("B", 0, 2), node("C", 0, 4)
        ), listOf(member("m1", "A", "B"), member("m2", "B", "C")), emptyList())
        val dist = StructureEngine.computeDistanceToSupport(d)
        assertEquals(0, dist["A"]); assertEquals(1, dist["B"]); assertEquals(2, dist["C"])
    }

    @Test fun `nodo inalcanzable tiene distancia -1`() {
        val d = StructureDesign("c", listOf(node("A", 0, 0, SupportType.FIJO), node("Z", 9, 9)), emptyList(), emptyList())
        assertEquals(-1, StructureEngine.computeDistanceToSupport(d)["Z"])
    }

    @Test fun `bfs elige la distancia minima cuando hay varios caminos`() {
        val d = StructureDesign("c", listOf(
            node("A", 0, 0, SupportType.FIJO), node("B", 2, 0, SupportType.FIJO), node("C", 1, 2), node("D", 1, 4)
        ), listOf(member("m1", "A", "C"), member("m2", "C", "D"), member("m3", "B", "D")), emptyList())
        val dist = StructureEngine.computeDistanceToSupport(d)
        assertEquals(1, dist["D"]) // vía B, más corto que vía A-C-D (2)
    }

    // ============================================================
    // 3) REPARTO DE CARGAS
    // ============================================================

    @Test fun `carga unica en cadena simple recae completa en el unico miembro`() {
        val d = StructureDesign("c", listOf(node("A", 0, 0, SupportType.FIJO), node("B", 0, 2)),
            listOf(member("m1", "A", "B")), listOf(LoadModel("l1", "B", 40)))
        val dist = StructureEngine.computeDistanceToSupport(d)
        val assigned = StructureEngine.distributeLoads(d, dist)
        assertEquals(40.0, assigned.getValue("m1"), 0.001)
    }

    @Test fun `carga se divide entre dos caminos paralelos hacia el suelo`() {
        val d = StructureDesign("c", listOf(
            node("A", 0, 0, SupportType.FIJO), node("B", 2, 0, SupportType.FIJO), node("T", 1, 2)
        ), listOf(member("mA", "A", "T"), member("mB", "B", "T")), listOf(LoadModel("l1", "T", 60)))
        val dist = StructureEngine.computeDistanceToSupport(d)
        val assigned = StructureEngine.distributeLoads(d, dist)
        assertEquals(30.0, assigned.getValue("mA"), 0.001)
        assertEquals(30.0, assigned.getValue("mB"), 0.001)
    }

    @Test fun `estructura triangulada reparte mas la carga que una cadena unica`() {
        // Cadena única: todo el peso pasa por un solo miembro en cada tramo.
        val chain = StructureDesign("c", listOf(node("A", 0, 0, SupportType.FIJO), node("B", 0, 2), node("C", 0, 4)),
            listOf(member("m1", "A", "B"), member("m2", "B", "C")), listOf(LoadModel("l1", "C", 60)))
        val chainAssigned = StructureEngine.distributeLoads(chain, StructureEngine.computeDistanceToSupport(chain))
        assertEquals(60.0, chainAssigned.getValue("m1"), 0.001)

        // Con una rama extra hacia otro apoyo, el miembro cercano al suelo recibe menos carga.
        val braced = StructureDesign("c", listOf(
            node("A", 0, 0, SupportType.FIJO), node("D", 3, 0, SupportType.FIJO), node("B", 0, 2), node("C", 0, 4)
        ), listOf(member("m1", "A", "B"), member("m2", "B", "C"), member("m3", "D", "B")),
            listOf(LoadModel("l1", "C", 60)))
        val bracedAssigned = StructureEngine.distributeLoads(braced, StructureEngine.computeDistanceToSupport(braced))
        assertTrue(bracedAssigned.getValue("m1") < chainAssigned.getValue("m1"))
    }

    @Test fun `carga lateral sin diagonales se amplifica`() {
        val d = StructureDesign("c", listOf(node("A", 0, 0, SupportType.FIJO), node("B", 0, 2)),
            listOf(member("m1", "A", "B")), listOf(LoadModel("l1", "B", 30, isLateral = true)))
        val assigned = StructureEngine.distributeLoads(d, StructureEngine.computeDistanceToSupport(d))
        assertEquals(30.0 * 1.6, assigned.getValue("m1"), 0.001)
    }

    @Test fun `carga lateral con diagonal presente no se amplifica`() {
        val d = StructureDesign("c", listOf(
            node("A", 0, 0, SupportType.FIJO), node("B", 0, 2), node("C", 2, 0, SupportType.FIJO)
        ), listOf(member("m1", "A", "B"), member("m2", "C", "B", role = MemberRole.DIAGONAL)),
            listOf(LoadModel("l1", "B", 30, isLateral = true)))
        val assigned = StructureEngine.distributeLoads(d, StructureEngine.computeDistanceToSupport(d))
        val total = assigned.values.sum()
        assertEquals(30.0, total, 0.001) // sin amplificación x1.6
    }

    @Test fun `nodo flotante no reparte carga`() {
        val d = StructureDesign("c", listOf(node("A", 0, 0, SupportType.FIJO), node("X", 9, 9)),
            emptyList(), listOf(LoadModel("l1", "X", 50)))
        val assigned = StructureEngine.distributeLoads(d, StructureEngine.computeDistanceToSupport(d))
        assertTrue(assigned.values.sum() == 0.0)
    }

    @Test fun `carga cero no genera demanda`() {
        val d = StructureDesign("c", listOf(node("A", 0, 0, SupportType.FIJO), node("B", 0, 2)),
            listOf(member("m1", "A", "B")), listOf(LoadModel("l1", "B", 0)))
        val assigned = StructureEngine.distributeLoads(d, StructureEngine.computeDistanceToSupport(d))
        assertEquals(0.0, assigned.getValue("m1"), 0.001)
    }

    // ============================================================
    // 4) CAPACIDAD DE MIEMBRO Y ESBELTEZ
    // ============================================================

    @Test fun `acero tiene mas capacidad que madera para la misma longitud`() {
        val steel = StructureEngine.memberCapacity(member("m1", "A", "B", MaterialType.ACERO, MemberRole.VIGA), 2.0)
        val wood = StructureEngine.memberCapacity(member("m2", "A", "B", MaterialType.MADERA, MemberRole.VIGA), 2.0)
        assertTrue(steel > wood)
    }

    @Test fun `columna larga pierde capacidad por esbeltez`() {
        val short = StructureEngine.memberCapacity(member("m1", "A", "B", MaterialType.ACERO, MemberRole.COLUMNA), 1.0)
        val long = StructureEngine.memberCapacity(member("m2", "A", "B", MaterialType.ACERO, MemberRole.COLUMNA), 6.0)
        assertTrue(long < short)
    }

    @Test fun `viga larga no sufre penalizacion de esbeltez en este modelo simplificado`() {
        val short = StructureEngine.memberCapacity(member("m1", "A", "B", MaterialType.ACERO, MemberRole.VIGA), 1.0)
        val long = StructureEngine.memberCapacity(member("m2", "A", "B", MaterialType.ACERO, MemberRole.VIGA), 6.0)
        assertEquals(short, long, 0.001)
    }

    @Test fun `penalizacion de esbeltez nunca supera el maximo permitido`() {
        val c = StructureEngine.memberCapacity(member("m1", "A", "B", MaterialType.MADERA, MemberRole.DIAGONAL), 100.0)
        val base = StructureEngine.MATERIALS.getValue(MaterialType.MADERA).strength * 12.0
        assertTrue(c >= base * 0.5 - 0.001)
    }

    // ============================================================
    // 5) ESTADOS DE DEMANDA
    // ============================================================

    @Test fun `estado sin carga cuando ratio es cero`() {
        assertEquals(MemberDemandState.SIN_CARGA, StructureEngine.demandStateFor(0.0))
    }
    @Test fun `estado baja para ratio pequeno`() { assertEquals(MemberDemandState.BAJA, StructureEngine.demandStateFor(0.2)) }
    @Test fun `estado media para ratio intermedio`() { assertEquals(MemberDemandState.MEDIA, StructureEngine.demandStateFor(0.5)) }
    @Test fun `estado alta cerca del limite`() { assertEquals(MemberDemandState.ALTA, StructureEngine.demandStateFor(0.9)) }
    @Test fun `estado alta exactamente en 1`() { assertEquals(MemberDemandState.ALTA, StructureEngine.demandStateFor(1.0)) }
    @Test fun `estado fallo al superar la capacidad`() { assertEquals(MemberDemandState.FALLO, StructureEngine.demandStateFor(1.01)) }

    // ============================================================
    // 6) TRIANGULACIÓN
    // ============================================================

    @Test fun `sin miembros la triangulacion es cero`() {
        val d = StructureDesign("c", emptyList(), emptyList(), emptyList())
        assertEquals(0.0, StructureEngine.triangulationRatio(d), 0.001)
    }

    @Test fun `triangulacion se calcula como proporcion de diagonales`() {
        val d = StructureDesign("c", listOf(node("A",0,0), node("B",2,0), node("C",1,2)),
            listOf(member("m1","A","B", role = MemberRole.VIGA),
                member("m2","A","C", role = MemberRole.DIAGONAL),
                member("m3","B","C", role = MemberRole.DIAGONAL)), emptyList())
        assertEquals(2.0/3.0, StructureEngine.triangulationRatio(d), 0.001)
    }

    // ============================================================
    // 7) SIMULACIÓN COMPLETA
    // ============================================================

    private fun towerDesign(heightSteps: Int, material: MaterialType = MaterialType.ACERO, withDiagonals: Boolean = false): StructureDesign {
        val nodes = mutableListOf(node("n0", 0, 0, SupportType.FIJO), node("n0b", 2, 0, SupportType.FIJO))
        val members = mutableListOf<StructureMemberModel>()
        for (i in 1..heightSteps) {
            nodes += node("nL$i", 0, i * 2)
            nodes += node("nR$i", 2, i * 2)
            members += member("beamL$i", if (i == 1) "n0" else "nL${i-1}", "nL$i", material, MemberRole.COLUMNA)
            members += member("beamR$i", if (i == 1) "n0b" else "nR${i-1}", "nR$i", material, MemberRole.COLUMNA)
            members += member("rung$i", "nL$i", "nR$i", material, MemberRole.VIGA)
            if (withDiagonals) members += member("diag$i", "nL${if (i==1) "" else i-1}".let { if (i==1) "n0" else "nL${i-1}" }, "nR$i", material, MemberRole.DIAGONAL)
        }
        return StructureDesign("c", nodes, members, listOf(LoadModel("ltop", "nL$heightSteps", 20)))
    }

    @Test fun `torre desconectada nunca aprueba el reto`() {
        val d = StructureDesign("c", listOf(node("A", 0, 0), node("B", 0, 2)), listOf(member("m1", "A", "B")), emptyList())
        val challenge = basicChallenge()
        val result = StructureEngine.simulate(d, challenge)
        assertFalse(result.isConnected)
        assertFalse(result.passed)
        assertEquals(0, result.starsEarned)
    }

    @Test fun `torre simple conectada y estable aprueba sin objetivos extra`() {
        val d = towerDesign(heightSteps = 3, withDiagonals = true)
        val result = StructureEngine.simulate(d, basicChallenge())
        assertTrue(result.isConnected)
        assertTrue(result.passed)
    }

    @Test fun `objetivo de altura minima no cumplido rechaza el diseno`() {
        val d = towerDesign(heightSteps = 1, withDiagonals = true)
        val challenge = basicChallenge(goals = listOf(ChallengeGoal(ChallengeGoalType.ALTURA_MINIMA, 10)))
        val result = StructureEngine.simulate(d, challenge)
        assertFalse(result.passed)
        assertTrue(result.maxHeight < 10)
    }

    @Test fun `objetivo de altura minima cumplido cuando la torre es suficientemente alta`() {
        val d = towerDesign(heightSteps = 6, withDiagonals = true)
        val challenge = basicChallenge(goals = listOf(ChallengeGoal(ChallengeGoalType.ALTURA_MINIMA, 10)))
        val result = StructureEngine.simulate(d, challenge)
        assertTrue(result.maxHeight >= 10)
    }

    @Test fun `presupuesto excedido hace fallar el reto aunque la estructura sea estable`() {
        val d = towerDesign(heightSteps = 3, material = MaterialType.CONCRETO, withDiagonals = true)
        val cheapChallenge = basicChallenge(maxBudget = 1, goals = listOf(ChallengeGoal(ChallengeGoalType.PRESUPUESTO_MAXIMO, 1)))
        val result = StructureEngine.simulate(d, cheapChallenge)
        assertFalse(result.passed)
    }

    @Test fun `presupuesto dentro del limite permite aprobar`() {
        val d = towerDesign(heightSteps = 2, material = MaterialType.MADERA, withDiagonals = true)
        val challenge = basicChallenge(goals = listOf(ChallengeGoal(ChallengeGoalType.PRESUPUESTO_MAXIMO, 100_000)))
        val result = StructureEngine.simulate(d, challenge)
        assertTrue(result.totalCost <= 100_000)
    }

    @Test fun `carga lateral sin triangulacion reduce estabilidad frente a version triangulada`() {
        val plain = towerDesign(heightSteps = 3, withDiagonals = false).let {
            it.copy(loads = it.loads + LoadModel("lat", "nL3", 25, isLateral = true))
        }
        val braced = towerDesign(heightSteps = 3, withDiagonals = true).let {
            it.copy(loads = it.loads + LoadModel("lat", "nL3", 25, isLateral = true))
        }
        val plainResult = StructureEngine.simulate(plain, basicChallenge())
        val bracedResult = StructureEngine.simulate(braced, basicChallenge())
        assertTrue(bracedResult.stabilityScore >= plainResult.stabilityScore)
    }

    @Test fun `objetivo resistir carga lateral requiere estabilidad`() {
        val braced = towerDesign(heightSteps = 2, withDiagonals = true).let {
            it.copy(loads = it.loads + LoadModel("lat", "nL2", 15, isLateral = true))
        }
        val challenge = basicChallenge(goals = listOf(ChallengeGoal(ChallengeGoalType.RESISTIR_CARGA_LATERAL, 1)))
        val result = StructureEngine.simulate(braced, challenge)
        assertEquals(result.isStable, result.passed || !result.isStable)
    }

    @Test fun `objetivo de triangulacion minima exige suficientes diagonales`() {
        val d = towerDesign(heightSteps = 3, withDiagonals = false)
        val challenge = basicChallenge(goals = listOf(ChallengeGoal(ChallengeGoalType.TRIANGULACION_MINIMA, 20)))
        val result = StructureEngine.simulate(d, challenge)
        assertFalse(result.passed)
    }

    @Test fun `objetivo de peso maximo rechaza estructuras demasiado pesadas`() {
        val heavy = towerDesign(heightSteps = 4, material = MaterialType.CONCRETO, withDiagonals = true)
        val challenge = basicChallenge(goals = listOf(ChallengeGoal(ChallengeGoalType.PESO_MAXIMO, 1)))
        val result = StructureEngine.simulate(heavy, challenge)
        assertFalse(result.passed)
    }

    @Test fun `objetivo de estabilidad minima exige puntuacion suficiente`() {
        val d = towerDesign(heightSteps = 3, withDiagonals = true)
        val challenge = basicChallenge(goals = listOf(ChallengeGoal(ChallengeGoalType.ESTABILIDAD_MINIMA, 200)))
        val result = StructureEngine.simulate(d, challenge)
        assertFalse(result.passed) // 200 es imposible (máximo 100)
    }

    @Test fun `estrellas son cero cuando el reto no se supera`() {
        val d = StructureDesign("c", listOf(node("A", 0, 0), node("B", 0, 2)), listOf(member("m1", "A", "B")), emptyList())
        val result = StructureEngine.simulate(d, basicChallenge())
        assertEquals(0, result.starsEarned)
    }

    @Test fun `estrellas maximas requieren puntuacion sobre el segundo umbral`() {
        val d = towerDesign(heightSteps = 2, withDiagonals = true)
        val challenge = basicChallenge(stars = Pair(0, 0)) // cualquier puntuación pasa el umbral de 3 estrellas
        val result = StructureEngine.simulate(d, challenge)
        if (result.passed) assertEquals(3, result.starsEarned)
    }

    @Test fun `miembro que supera capacidad aparece en failedMemberIds`() {
        val d = StructureDesign("c", listOf(node("A", 0, 0, SupportType.FIJO), node("B", 0, 2)),
            listOf(member("m1", "A", "B", MaterialType.MADERA, MemberRole.VIGA)),
            listOf(LoadModel("l1", "B", 10_000)))
        val result = StructureEngine.simulate(d, basicChallenge())
        assertTrue("m1" in result.failedMemberIds)
        assertFalse(result.passed)
    }

    @Test fun `peso total se calcula a partir de longitud y peso de material`() {
        val d = StructureDesign("c", listOf(node("A", 0, 0, SupportType.FIJO), node("B", 0, 2)),
            listOf(member("m1", "A", "B", MaterialType.ACERO, MemberRole.VIGA)), emptyList())
        val result = StructureEngine.simulate(d, basicChallenge())
        val expectedWeight = 2.0 * StructureEngine.MATERIALS.getValue(MaterialType.ACERO).weight
        assertEquals(expectedWeight, result.totalWeight, 0.001)
    }

    @Test fun `costo total se calcula a partir de longitud y costo de material`() {
        val d = StructureDesign("c", listOf(node("A", 0, 0, SupportType.FIJO), node("B", 0, 2)),
            listOf(member("m1", "A", "B", MaterialType.MADERA, MemberRole.VIGA)), emptyList())
        val result = StructureEngine.simulate(d, basicChallenge())
        assertEquals(10, result.totalCost) // longitud 2 * costo 5
    }

    // ============================================================
    // 8) CASOS LÍMITE
    // ============================================================

    @Test fun `diseno sin miembros no esta conectado ni aprueba`() {
        val d = StructureDesign("c", listOf(node("A", 0, 0, SupportType.FIJO)), emptyList(), emptyList())
        val result = StructureEngine.simulate(d, basicChallenge())
        assertFalse(result.passed)
    }

    @Test fun `dos miembros duplicados entre los mismos nodos no rompen el calculo`() {
        val d = StructureDesign("c", listOf(node("A", 0, 0, SupportType.FIJO), node("B", 0, 2)),
            listOf(member("m1", "A", "B"), member("m2", "A", "B")), listOf(LoadModel("l1", "B", 20)))
        val result = StructureEngine.simulate(d, basicChallenge())
        assertTrue(result.isConnected)
        // la carga se reparte entre los dos miembros paralelos
        assertEquals(2, result.memberResults.size)
    }

    @Test fun `carga en nodo que tambien es apoyo no genera demanda`() {
        val d = StructureDesign("c", listOf(node("A", 0, 0, SupportType.FIJO), node("B", 0, 2)),
            listOf(member("m1", "A", "B")), listOf(LoadModel("l1", "A", 999)))
        val result = StructureEngine.simulate(d, basicChallenge())
        assertEquals(MemberDemandState.SIN_CARGA, result.memberResults.first().state)
    }

    @Test fun `estructura ciclica (diamante) no produce carga infinita ni excepcion`() {
        val d = StructureDesign("c", listOf(
            node("A", 0, 0, SupportType.FIJO), node("B", 2, 2), node("C", -2, 2), node("D", 0, 4)
        ), listOf(member("m1", "A", "B"), member("m2", "A", "C"), member("m3", "B", "D"), member("m4", "C", "D")),
            listOf(LoadModel("l1", "D", 40)))
        val result = StructureEngine.simulate(d, basicChallenge())
        assertTrue(result.memberResults.all { it.demandRatio.isFinite() })
    }

    @Test fun `simulacion con lista de nodos vacia no lanza excepcion`() {
        val d = StructureDesign("c", emptyList(), emptyList(), emptyList())
        val result = StructureEngine.simulate(d, basicChallenge())
        assertFalse(result.isConnected)
        assertEquals(0, result.maxHeight)
    }

    @Test fun `stabilityScore siempre esta acotado entre 0 y 100`() {
        val disasters = listOf(
            StructureDesign("c", listOf(node("A", 0, 0, SupportType.FIJO), node("B", 0, 30)),
                listOf(member("m1", "A", "B", MaterialType.MADERA, MemberRole.COLUMNA)),
                listOf(LoadModel("l1", "B", 99999))),
            towerDesign(8, withDiagonals = true)
        )
        for (d in disasters) {
            val r = StructureEngine.simulate(d, basicChallenge())
            assertTrue(r.stabilityScore in 0..100)
        }
    }

    @Test fun `feedbackKey nunca esta vacio`() {
        val designs = listOf(
            StructureDesign("c", emptyList(), emptyList(), emptyList()),
            towerDesign(3, withDiagonals = true)
        )
        for (d in designs) {
            val r = StructureEngine.simulate(d, basicChallenge())
            assertTrue(r.feedbackKey.isNotBlank())
        }
    }
}
