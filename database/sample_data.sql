-- ============================================================
-- Constructópolis — Datos de ejemplo (subconjunto representativo)
-- El contenido real y completo (40 retos, 3 materiales, 10 insignias)
-- se genera con tools/generate_seed.py y se carga en runtime desde
-- app/src/main/assets/seed/*.json vía Seeder.kt. Este archivo sirve
-- como referencia SQL legible de cómo lucen esos datos.
-- ============================================================

INSERT INTO user_profile (id, alias, avatarId, soundEnabled, hapticEnabled, onboardingCompleted, createdAt)
VALUES (1, 'Ingeniera Junior', 0, 1, 1, 1, 1735689600000);

INSERT INTO material (id, displayName, description, strength, weight, cost, colorHex, iconRes) VALUES
('MADERA', 'Madera de Andamio', 'Ligera y barata, ideal para empezar. Se dobla si la pieza es muy larga.', 40.0, 3.0, 5, '#C68B4A', 'ic_material_madera'),
('ACERO', 'Viga de Acero Nova', 'Fuerte y confiable, el favorito de las torres altas. Cuesta más que la madera.', 90.0, 8.0, 12, '#5B7C99', 'ic_material_acero'),
('CONCRETO', 'Bloque de Concreto', 'Pesado y resistente a cargas grandes, perfecto para columnas cortas.', 70.0, 15.0, 8, '#8D8B85', 'ic_material_concreto');

INSERT INTO structure_challenge (id, orderInChapter, worldChapter, title, briefing, category, gridWidth, gridHeight, maxBudget, starThreshold2, starThreshold3, allowedMaterialsCsv, iconRes) VALUES
('c01_viga', 1, 1, 'Primer Puente', 'La Ingeniera Nova necesita que aprendas a conectar tus piezas al suelo antes de construir nada grande.', 'VIGA', 6, 6, 400, 55, 78, 'MADERA', 'ic_challenge_viga'),
('c09_columna', 1, 2, 'Pilar de Entrada', 'Es hora de sostener peso de verdad: elige bien tus columnas para no romper el taller.', 'COLUMNA', 6, 6, 500, 55, 78, 'MADERA', 'ic_challenge_columna'),
('c17_torre', 1, 3, 'Torre Vigía', '¡Construye hacia el cielo! Cuanto más alto, más cuidado hay que tener con la esbeltez.', 'TORRE', 6, 6, 500, 55, 78, 'MADERA', 'ic_challenge_torre'),
('c25_carga', 1, 4, 'Ráfaga Suave', 'El viento empuja de lado. Sin diagonales, hasta la torre más bonita puede tambalear.', 'CARGA', 6, 6, 500, 55, 78, 'MADERA', 'ic_challenge_carga'),
('c33_reto', 1, 5, 'Reto Combinado I', 'Retos combinados: presupuesto, altura, viento y peso a la vez. ¡Demuestra todo lo aprendido!', 'RETO', 6, 6, 500, 55, 78, 'ACERO', 'ic_challenge_reto');

INSERT INTO challenge_goal (challengeId, type, value) VALUES
('c01_viga', 'PRESUPUESTO_MAXIMO', 120),
('c09_columna', 'ALTURA_MINIMA', 6),
('c09_columna', 'PESO_MAXIMO', 80),
('c17_torre', 'ALTURA_MINIMA', 10),
('c17_torre', 'TRIANGULACION_MINIMA', 10),
('c25_carga', 'RESISTIR_CARGA_LATERAL', 1),
('c25_carga', 'TRIANGULACION_MINIMA', 20),
('c33_reto', 'ESTABILIDAD_MINIMA', 55),
('c33_reto', 'PRESUPUESTO_MAXIMO', 300);

INSERT INTO preset_support (challengeId, x, y, supportType) VALUES
('c01_viga', 0, 0, 'FIJO'),
('c01_viga', 5, 0, 'FIJO');

INSERT INTO badge (id, title, description, iconRes, tier) VALUES
('PRIMER_LADRILLO', 'Primer Ladrillo', 'Completaste tu primer reto en el Taller. ¡El comienzo de una gran obra!', 'ic_badge_primer_ladrillo', 1),
('TORRE_AL_CIELO', 'Torre al Cielo', 'Construiste una estructura de 20 metros o más de altura.', 'ic_badge_torre_cielo', 2),
('MAESTRA_CONSTRUCTORA', 'Maestra Constructora', 'Completaste 20 retos en total. ¡Eres toda una ingeniera junior!', 'ic_badge_maestra_constructora', 4);

INSERT INTO blueprint_reward (challengeId, title, description, iconRes, unlockedAt) VALUES
('c01_viga', 'Plano: Primer Puente', 'Se desbloquea al superar el reto "Primer Puente".', 'ic_blueprint_reward', NULL);

-- Ejemplo de una partida en curso: diseño guardado para c01_viga
INSERT INTO structure_design (id, challengeId, createdAt, updatedAt) VALUES (1, 'c01_viga', 1735689700000, 1735689820000);

INSERT INTO structure_node (designId, nodeKey, x, y, supportType) VALUES
(1, 'S0', 0, 0, 'FIJO'),
(1, 'S1', 5, 0, 'FIJO'),
(1, 'n0', 2, 3, 'NINGUNO'),
(1, 'n1', 3, 3, 'NINGUNO');

INSERT INTO structure_member (designId, memberKey, nodeAKey, nodeBKey, material, role) VALUES
(1, 'm0', 'S0', 'n0', 'MADERA', 'VIGA'),
(1, 'm1', 'n0', 'n1', 'MADERA', 'VIGA'),
(1, 'm2', 'n1', 'S1', 'MADERA', 'VIGA');

INSERT INTO load (designId, loadKey, nodeKey, magnitude, isLateral) VALUES (1, 'l0', 'n0', 20, 0);

-- Resultado de una simulación aprobada con 2 estrellas
INSERT INTO simulation_run (id, designId, challengeId, ranAt, isConnected, isStable, passed, starsEarned, maxHeight, totalCost, totalWeight, stabilityScore, triangulationPercent, feedbackKey)
VALUES (1, 1, 'c01_viga', 1735689825000, 1, 1, 1, 2, 3, 95, 15.0, 82, 0, 'feedback_solido');

INSERT INTO member_result (simulationRunId, memberKey, assignedLoad, capacity, demandRatio, state) VALUES
(1, 'm0', 10.0, 480.0, 0.02, 'BAJA'),
(1, 'm1', 0.0, 480.0, 0.0, 'SIN_CARGA'),
(1, 'm2', 10.0, 480.0, 0.02, 'BAJA');

INSERT INTO progress (challengeId, started, completed, bestStars, attempts, lastPlayedAt)
VALUES ('c01_viga', 1, 1, 2, 1, 1735689825000);

INSERT INTO user_badge (badgeId, unlockedAt) VALUES ('PRIMER_LADRILLO', 1735689825000);
