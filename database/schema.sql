-- ============================================================
-- Constructópolis — Esquema de referencia (SQLite / Room 2.6.1)
-- Generado a mano a partir de las entidades Kotlin en
-- app/src/main/kotlin/.../data/local/entity/*.kt (fuente de verdad).
-- Room genera este DDL automáticamente al compilar (ver
-- app/schemas/ tras un build real); este archivo es la referencia
-- legible y versionada del mismo esquema.
-- ============================================================

PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS user_profile (
    id INTEGER NOT NULL PRIMARY KEY,
    alias TEXT NOT NULL,
    avatarId INTEGER NOT NULL,
    soundEnabled INTEGER NOT NULL DEFAULT 1,
    hapticEnabled INTEGER NOT NULL DEFAULT 1,
    onboardingCompleted INTEGER NOT NULL DEFAULT 0,
    createdAt INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS material (
    id TEXT NOT NULL PRIMARY KEY,
    displayName TEXT NOT NULL,
    description TEXT NOT NULL,
    strength REAL NOT NULL,
    weight REAL NOT NULL,
    cost INTEGER NOT NULL,
    colorHex TEXT NOT NULL,
    iconRes TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS structure_challenge (
    id TEXT NOT NULL PRIMARY KEY,
    orderInChapter INTEGER NOT NULL,
    worldChapter INTEGER NOT NULL,
    title TEXT NOT NULL,
    briefing TEXT NOT NULL,
    category TEXT NOT NULL,
    gridWidth INTEGER NOT NULL,
    gridHeight INTEGER NOT NULL,
    maxBudget INTEGER NOT NULL,
    starThreshold2 INTEGER NOT NULL,
    starThreshold3 INTEGER NOT NULL,
    allowedMaterialsCsv TEXT NOT NULL,
    iconRes TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS challenge_goal (
    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    challengeId TEXT NOT NULL,
    type TEXT NOT NULL,
    value INTEGER NOT NULL,
    FOREIGN KEY (challengeId) REFERENCES structure_challenge(id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS index_challenge_goal_challengeId ON challenge_goal(challengeId);

CREATE TABLE IF NOT EXISTS preset_support (
    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    challengeId TEXT NOT NULL,
    x INTEGER NOT NULL,
    y INTEGER NOT NULL,
    supportType TEXT NOT NULL,
    FOREIGN KEY (challengeId) REFERENCES structure_challenge(id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS index_preset_support_challengeId ON preset_support(challengeId);

CREATE TABLE IF NOT EXISTS preset_load (
    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    challengeId TEXT NOT NULL,
    x INTEGER NOT NULL,
    y INTEGER NOT NULL,
    magnitude INTEGER NOT NULL,
    isLateral INTEGER NOT NULL,
    FOREIGN KEY (challengeId) REFERENCES structure_challenge(id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS index_preset_load_challengeId ON preset_load(challengeId);

CREATE TABLE IF NOT EXISTS structure_design (
    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    challengeId TEXT NOT NULL,
    createdAt INTEGER NOT NULL,
    updatedAt INTEGER NOT NULL,
    FOREIGN KEY (challengeId) REFERENCES structure_challenge(id) ON DELETE CASCADE
);
CREATE UNIQUE INDEX IF NOT EXISTS index_structure_design_challengeId ON structure_design(challengeId);

CREATE TABLE IF NOT EXISTS structure_node (
    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    designId INTEGER NOT NULL,
    nodeKey TEXT NOT NULL,
    x INTEGER NOT NULL,
    y INTEGER NOT NULL,
    supportType TEXT NOT NULL,
    FOREIGN KEY (designId) REFERENCES structure_design(id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS index_structure_node_designId ON structure_node(designId);

CREATE TABLE IF NOT EXISTS structure_member (
    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    designId INTEGER NOT NULL,
    memberKey TEXT NOT NULL,
    nodeAKey TEXT NOT NULL,
    nodeBKey TEXT NOT NULL,
    material TEXT NOT NULL,
    role TEXT NOT NULL,
    FOREIGN KEY (designId) REFERENCES structure_design(id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS index_structure_member_designId ON structure_member(designId);

CREATE TABLE IF NOT EXISTS load (
    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    designId INTEGER NOT NULL,
    loadKey TEXT NOT NULL,
    nodeKey TEXT NOT NULL,
    magnitude INTEGER NOT NULL,
    isLateral INTEGER NOT NULL,
    FOREIGN KEY (designId) REFERENCES structure_design(id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS index_load_designId ON load(designId);

CREATE TABLE IF NOT EXISTS simulation_run (
    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    designId INTEGER NOT NULL,
    challengeId TEXT NOT NULL,
    ranAt INTEGER NOT NULL,
    isConnected INTEGER NOT NULL,
    isStable INTEGER NOT NULL,
    passed INTEGER NOT NULL,
    starsEarned INTEGER NOT NULL,
    maxHeight INTEGER NOT NULL,
    totalCost INTEGER NOT NULL,
    totalWeight REAL NOT NULL,
    stabilityScore INTEGER NOT NULL,
    triangulationPercent INTEGER NOT NULL,
    feedbackKey TEXT NOT NULL,
    FOREIGN KEY (designId) REFERENCES structure_design(id) ON DELETE CASCADE,
    FOREIGN KEY (challengeId) REFERENCES structure_challenge(id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS index_simulation_run_designId ON simulation_run(designId);
CREATE INDEX IF NOT EXISTS index_simulation_run_challengeId ON simulation_run(challengeId);

CREATE TABLE IF NOT EXISTS member_result (
    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    simulationRunId INTEGER NOT NULL,
    memberKey TEXT NOT NULL,
    assignedLoad REAL NOT NULL,
    capacity REAL NOT NULL,
    demandRatio REAL NOT NULL,
    state TEXT NOT NULL,
    FOREIGN KEY (simulationRunId) REFERENCES simulation_run(id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS index_member_result_simulationRunId ON member_result(simulationRunId);

CREATE TABLE IF NOT EXISTS progress (
    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    challengeId TEXT NOT NULL,
    started INTEGER NOT NULL DEFAULT 0,
    completed INTEGER NOT NULL DEFAULT 0,
    bestStars INTEGER NOT NULL DEFAULT 0,
    attempts INTEGER NOT NULL DEFAULT 0,
    lastPlayedAt INTEGER NOT NULL DEFAULT 0,
    FOREIGN KEY (challengeId) REFERENCES structure_challenge(id) ON DELETE CASCADE
);
CREATE UNIQUE INDEX IF NOT EXISTS index_progress_challengeId ON progress(challengeId);

CREATE TABLE IF NOT EXISTS blueprint_reward (
    challengeId TEXT NOT NULL PRIMARY KEY,
    title TEXT NOT NULL,
    description TEXT NOT NULL,
    iconRes TEXT NOT NULL,
    unlockedAt INTEGER,
    FOREIGN KEY (challengeId) REFERENCES structure_challenge(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS badge (
    id TEXT NOT NULL PRIMARY KEY,
    title TEXT NOT NULL,
    description TEXT NOT NULL,
    iconRes TEXT NOT NULL,
    tier INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS user_badge (
    badgeId TEXT NOT NULL PRIMARY KEY,
    unlockedAt INTEGER NOT NULL,
    FOREIGN KEY (badgeId) REFERENCES badge(id) ON DELETE CASCADE
);
