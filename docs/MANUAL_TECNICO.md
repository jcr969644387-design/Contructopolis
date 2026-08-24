# Manual Técnico — Constructópolis

## 1. Stack y versiones (fijas, no dinámicas)

| Componente | Versión |
|---|---|
| Kotlin | 1.9.24 |
| AGP (Android Gradle Plugin) | 8.5.2 |
| KSP | 1.9.24-1.0.20 |
| Compose BOM | 2024.06.00 |
| Compose Compiler | 1.5.14 |
| Material 3 | (vía BOM) |
| Navigation Compose | 2.7.7 |
| Room | 2.6.1 |
| kotlinx.serialization JSON | 1.7.1 |
| Coroutines | 1.8.1 |
| DataStore Preferences | 1.1.1 |
| JDK | 17 |
| compileSdk / targetSdk | 34 |
| minSdk | 24 |
| Gradle Wrapper | 8.7 |

Todas las versiones están fijadas en `gradle/libs.versions.toml` (catálogo de versiones de Gradle); no se usan rangos (`+`) ni `latest`.

## 2. Arquitectura

```
app/src/main/kotlin/com/educalab/civilestructuras/
├── domain/
│   ├── model/       Modelos puros (StructureDesign, ChallengeModel, SimulationResult...)
│   └── logic/       StructureEngine, BadgeEngine, ProgressEngine — 0 imports de Android
├── data/
│   ├── local/entity  13 entidades Room
│   ├── local/dao      8 DAOs con @Transaction para relaciones
│   ├── local/         ConstructopolisDatabase, Seeder, converters
│   └── repository/   8 repositorios (Profile, Material, Challenge, Design, Simulation, Blueprint, Badge)
├── viewmodel/        HomeViewModel, ChapterListViewModel, BuilderViewModel, MaterialsViewModel,
│                      BlueprintsViewModel, ProfileViewModel + GenericViewModelFactory (DI manual)
├── ui/
│   ├── theme/        Color.kt, Type.kt, Theme.kt (paleta "Taller Industrial")
│   ├── navigation/    Routes.kt, ConstructopolisNavGraph.kt
│   ├── components/    ModuleCard, DemandStateChip, WorkshopProgressBar, ilustraciones Canvas...
│   └── screens/       10 pantallas (splash, onboarding, home, concepts, materials, builder,
│                       challenges [lista por capítulo], blueprints, profile)
├── util/             IconRegistry (mapea nombre de recurso ↔ drawable real)
├── AppContainer.kt   Contenedor de dependencias manual (sin Hilt/Koin)
├── ConstructopolisApp.kt
└── MainActivity.kt
```

**Por qué DI manual:** con 8 repositorios y ~6 ViewModels, el costo de cablear a mano en `AppContainer` es bajo y evita la complejidad/tiempo de compilación adicional de Hilt, manteniendo el build lo más simple posible.

## 3. Motor de dominio: `StructureEngine`

Algoritmo (ver comentarios en el propio archivo para el detalle):

1. **Conectividad:** BFS multi-fuente desde todos los nodos con apoyo sobre el grafo de miembros. Un diseño está "conectado" si todo nodo que participa en al menos un miembro tiene camino hasta un apoyo.
2. **Distancia a apoyo:** BFS que asigna a cada nodo su distancia mínima (en número de miembros) hasta el apoyo más cercano; -1 si es inalcanzable.
3. **Reparto de cargas:** cada carga se "escurre" desde su nodo hacia el suelo, dividiéndose en partes iguales entre los miembros cuyo otro extremo tiene distancia exactamente una unidad menor. Al procesar los nodos en orden estrictamente decreciente de distancia, el algoritmo es un DAG por construcción y nunca entra en bucle, incluso si el grafo original tiene ciclos.
4. **Capacidad de miembro:** resistencia del material multiplicada por un factor base; vigas no penalizan por esbeltez (se apoyan en toda su longitud); columnas/diagonales pierden hasta 50% de capacidad al crecer la longitud (referencia 10 unidades).
5. **Estados de demanda:** SIN_CARGA / BAJA / MEDIA / ALTA / FALLO según el ratio carga/capacidad.
6. **Estabilidad (0-100):** penaliza miembros fallidos, miembros en demanda alta y cargas laterales sin triangulación.
7. **Evaluación de objetivos y aprobación:** compara altura, presupuesto, triangulación, peso y estabilidad contra los objetivos del reto.

Ver `docs/BUILD_REPORT.md` para la verificación real (75 pruebas ejecutadas en JVM).

## 4. Persistencia (Room)

Ver `docs/BASE_DE_DATOS.md` para el esquema completo. Puntos clave:
- Todas las relaciones usan claves foráneas con `onDelete = CASCADE` (borrar un reto borra sus objetivos/apoyos/cargas preconfiguradas; borrar un diseño borra sus nodos/miembros/cargas; borrar una simulación borra sus resultados por miembro).
- `@Transaction` + `@Relation` para leer agregados completos en una sola consulta (`ChallengeWithDetails`, `DesignWithDetails`, `SimulationRunWithMembers`).
- El guardado del Constructor (`DesignDao.replaceDesignContent`) es una transacción atómica: limpia y reinserta nodos/miembros/cargas juntos, nunca deja el diseño a medias.

## 5. Contenido semilla

`tools/generate_seed.py` genera `app/src/main/assets/seed/{materials,challenges,badges}.json` (40 retos reales con presupuesto/objetivos/materiales permitidos variables, 3 materiales, 10 insignias). `Seeder.kt` los carga una sola vez, la primera vez que la base de datos está vacía, usando `kotlinx.serialization`.

`tools/generate_vector_drawables.py` genera los 19 vector drawables temáticos (materiales, categorías de reto, insignias, plano) en `res/drawable/`. `IconRegistry.kt` traduce el nombre de recurso guardado en Room al recurso drawable real.

## 6. Compilación

```bash
./gradlew clean
./gradlew testDebugUnitTest
./gradlew lintDebug
./gradlew assembleDebug
```

Ver `docs/BUILD_REPORT.md` para el estado real de cada paso en este entorno de desarrollo, y `.github/workflows/android-build.yml` para el CI que sí tiene acceso completo a los repositorios de Google/Maven/Gradle.

## 7. Pruebas

- `app/src/test/kotlin/.../domain/logic/StructureEngineTest.kt` — 53 pruebas JUnit4 puras.
- `app/src/test/kotlin/.../domain/logic/BadgeEngineTest.kt` — 22 pruebas JUnit4 puras (BadgeEngine + ProgressEngine).
- `app/src/test/kotlin/.../data/local/ConstructopolisDatabaseTest.kt` — 14 pruebas Robolectric sobre Room en memoria (relaciones, cascada, restricciones de unicidad, agregados para insignias).

## 8. Mantenimiento y ampliación

- **Nuevo reto:** añadir una entrada a `tools/generate_seed.py` (o directamente al JSON) y volver a ejecutar el script; no requiere cambios de código.
- **Nueva insignia:** añadir el `BadgeId` en `BadgeEngine.kt`, su regla en `evaluateEarnedBadges`, su fila en `badges.json` y su ícono en `generate_vector_drawables.py` + `IconRegistry`.
- **Nuevo material:** añadir a `MaterialType` (domain), `StructureEngine.MATERIALS`, `materials.json` y su ícono.
- **Nueva pantalla:** añadir ruta en `Routes.kt`, `composable(...)` en `ConstructopolisNavGraph.kt`, y su propio ViewModel + Screen siguiendo el mismo patrón que las existentes.
