# Constructópolis — Taller del Ingeniero Junior

Aplicación educativa Android (Kotlin + Jetpack Compose) de ingeniería civil para niños y niñas de 10 a 15 años. La Ingeniera Nova guía al jugador por un taller donde se diseñan puentes, columnas, torres y estructuras completas, se someten a cargas simuladas y se recompensan con planos e insignias reales, ligadas a acciones concretas dentro del juego.

> **Estado de compilación:** ver [`docs/BUILD_REPORT.md`](docs/BUILD_REPORT.md). El motor de dominio (`StructureEngine`, `BadgeEngine`, `ProgressEngine`) fue verificado ejecutando su batería de pruebas de forma real sobre una JVM con Kotlin 2.0.20. El build completo de Android (`assembleDebug`) **no pudo verificarse en el entorno de desarrollo** por no tener SDK de Android ni acceso a los repositorios de Google/Gradle; está listo para ejecutarse en Android Studio o mediante el workflow de GitHub Actions incluido en `.github/workflows/android-build.yml`.

## Concepto

- **Área:** Ingeniería Civil (público objetivo 10-15 años).
- **Mundo visual:** taller industrial moderno — planos, grúas, vigas, torres — no una app administrativa.
- **Personaje guía:** Ingeniera Nova, con frases breves y sin interrupciones constantes.
- **Mecánica central:** un Constructor con cuadrícula donde se colocan nodos, se conectan vigas/columnas/diagonales de tres materiales distintos, se añaden cargas (incluyendo viento lateral) y se simula con un motor de reglas reales (`StructureEngine`), no con selección múltiple.

## Módulos

1. Taller / Mapa de proyectos (Home)
2. Conceptos estructurales
3. Materiales
4. Constructor
5. Vigas (capítulo 1)
6. Columnas (capítulo 2)
7. Torres (capítulo 3)
8. Cargas y simulación / viento (capítulo 4)
9. Gran Taller de Retos (capítulo 5)
10. Planos y Logros (insignias + colección de planos)

Más perfil (alias/avatar local) y onboarding de 4 pantallas.

## Stack técnico

Kotlin 1.9.24 · Jetpack Compose (BOM 2024.06.00) · Material 3 · Navigation Compose · MVVM + Repository · Room 2.6.1 · Coroutines/Flow · kotlinx.serialization · Gradle Kotlin DSL · AGP 8.5.2 · JDK 17 · minSdk 24 / target 34.

100% offline: sin Firebase, sin backend, sin login, sin anuncios, sin analítica, sin el permiso `INTERNET`.

## Estructura del repositorio

```
app/                    Proyecto Android (Kotlin/Compose/Room)
database/               schema.sql y sample_data.sql (referencia SQL del esquema Room)
docs/                   Documentación (memoria, manuales, base de datos, build report) + PDFs
tools/                  Scripts usados en el desarrollo (generación de datos semilla e íconos)
.github/workflows/      CI de Android (compila la APK real al hacer push)
deliverables/           Entregables finales (código fuente .zip y PDFs)
```

## Cómo compilar

```bash
./gradlew clean
./gradlew testDebugUnitTest
./gradlew lintDebug
./gradlew assembleDebug
```

Requiere Android Studio (Iguana o superior) o un entorno con Android SDK (compileSdk 34) + JDK 17. Al hacer `git push` a `main`/`master` con este repositorio en GitHub, el workflow `.github/workflows/android-build.yml` compila automáticamente la APK y publica los reportes de pruebas/lint como artefactos descargables (no se hace push automáticamente desde aquí).

## Documentación

- [`docs/MEMORIA_DESCRIPTIVA.md`](docs/MEMORIA_DESCRIPTIVA.md) — objetivos, alcance, arquitectura, limitaciones.
- [`docs/MANUAL_USUARIO.md`](docs/MANUAL_USUARIO.md) — instalación y uso para familias/docentes.
- [`docs/MANUAL_TECNICO.md`](docs/MANUAL_TECNICO.md) — arquitectura técnica, build, mantenimiento.
- [`docs/BASE_DE_DATOS.md`](docs/BASE_DE_DATOS.md) — esquema Room completo (13 entidades) + DER.
- [`docs/BUILD_REPORT.md`](docs/BUILD_REPORT.md) — resultado real de pruebas/lint/build.

## Privacidad

No se solicita nombre real, correo, teléfono, dirección ni ubicación. El perfil usa solo un alias y un avatar local. Todos los datos se guardan exclusivamente en el dispositivo (Room), nunca se transmiten.
