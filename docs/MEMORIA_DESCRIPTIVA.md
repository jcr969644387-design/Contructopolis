# Memoria Descriptiva — Constructópolis

## 1. Identificación

| Campo | Valor |
|---|---|
| Nombre | Constructópolis — Taller del Ingeniero Junior |
| Paquete | `com.educalab.civilestructuras` |
| Versión | 1.0.0 |
| Plataforma | Android nativo (Kotlin + Jetpack Compose) |
| Público objetivo | Niñas y niños de 10 a 15 años |
| Área temática | Ingeniería civil / estructuras |

## 2. Problema y justificación

Los conceptos de ingeniería civil (cargas, apoyos, vigas, columnas, triangulación, esbeltez) suelen enseñarse de forma abstracta o puramente teórica, sin posibilidad de experimentar con las consecuencias de una mala decisión de diseño. Constructópolis traduce estas ideas a un taller interactivo donde el error no es un fracaso sino información: una viga que falla muestra por qué falló y cómo corregirla, reforzando el razonamiento de causa-efecto propio del pensamiento de ingeniería.

## 3. Objetivos

**General:** ofrecer una experiencia lúdica y visualmente atractiva donde niños de 10-15 años aprendan, mediante construcción y simulación real (no cuestionarios), los fundamentos de estructuras: cargas, apoyos, materiales, vigas, columnas, triangulación y estabilidad.

**Específicos:**
- Implementar un motor de simulación estructural conceptual, determinista y testeable (`StructureEngine`).
- Ofrecer progresión gradual en 5 capítulos temáticos (40 retos) con dificultad creciente.
- Proveer gamificación vinculada a acciones reales: XP implícito en estrellas, insignias y planos coleccionables, nunca aleatorios.
- Garantizar privacidad infantil total: cero datos personales, cero conexión a Internet.
- Construir una base de persistencia real (Room) que permita continuar sesiones de 5-20 minutos.

## 4. Público y alcance

**Dentro de alcance:** diseño y simulación conceptual de estructuras 2D (vigas, columnas, diagonales), tres materiales (madera, acero, concreto), cargas verticales y laterales (viento), 40 retos en 5 capítulos, sistema de insignias y planos, perfil local con alias/avatar, modo claro/oscuro, accesibilidad básica.

**Fuera de alcance (explícitamente):** cálculo estructural profesional (matrices de rigidez, análisis de elementos finitos), estructuras 3D, multijugador o tablas de clasificación en línea, compras, anuncios, cuentas de usuario en la nube.

## 5. Requisitos funcionales (resumen)

- RF-01: el sistema debe permitir crear/editar un diseño estructural (nodos, miembros, cargas) por reto.
- RF-02: el sistema debe simular el diseño y devolver conectividad, reparto de carga, capacidad por miembro, costo, peso, altura, triangulación, estabilidad y objetivos cumplidos.
- RF-03: el sistema debe persistir el diseño y el resultado de cada intento.
- RF-04: el sistema debe desbloquear insignias y planos únicamente a partir de estadísticas reales derivadas de intentos guardados.
- RF-05: el sistema debe mostrar el progreso por capítulo y global.
- RF-06: el sistema debe funcionar sin conexión a Internet en todo momento.
- RF-07: el sistema no debe solicitar datos personales identificables.

## 6. Requisitos no funcionales

- RNF-01: la aplicación debe iniciar y ser usable en sesiones de 5 a 20 minutos, con guardado y continuación.
- RNF-02: el motor de simulación no debe superar unos pocos milisegundos para diseños típicos (decenas de nodos/miembros).
- RNF-03: el código de dominio debe ser testeable sin depender de Android (JVM puro).
- RNF-04: minSdk 24 (cobertura amplia de dispositivos reales en el segmento educativo).

## 7. Casos de uso principales

1. **Resolver un reto:** el jugador entra a un capítulo → elige un reto disponible → el Constructor carga los apoyos preconfigurados → coloca nodos/piezas/cargas → guarda y/o simula → recibe feedback educativo → si aprueba, se desbloquean plano e insignias correspondientes → el progreso del capítulo se actualiza.
2. **Retomar sesión:** el jugador reabre la app → el Home muestra progreso global y el siguiente reto sugerido → puede continuar cualquier reto iniciado, que conserva su diseño guardado.
3. **Personalizar perfil:** el jugador elige alias y avatar local, ajusta sonido/háptica.
4. **Explorar conceptos y materiales:** el jugador consulta explicaciones ilustradas antes o durante los retos.

## 8. Módulos / pantallas

Ver README.md §Módulos. Diez módulos principales + onboarding (4 páginas) + perfil, sin contar diálogos o ajustes triviales.

## 9. Flujo de navegación

`Splash → (Onboarding si es primera vez) → Home` y desde Home hacia: Conceptos, Materiales, Vigas/Columnas/Torres/Cargas/Retos (listas por capítulo) → Constructor(retoId) → resultado inline → Planos y Logros, Perfil.

## 10. Arquitectura

MVVM + Repository sobre tres capas (`data/`, `domain/`, `ui/`):

- **domain/model + domain/logic:** modelos y motor de reglas puros en Kotlin, sin ninguna dependencia de Android. `StructureEngine` calcula conectividad (BFS multi-fuente), reparto de cargas (propagación por capas de distancia, sin ciclos posibles porque la distancia decrece estrictamente), capacidad por material/rol/esbeltez, triangulación, estabilidad y evaluación de objetivos. `BadgeEngine`/`ProgressEngine` son igualmente puros.
- **data/local:** 13 entidades Room normalizadas con claves foráneas y `@Transaction` para relaciones (ver `docs/BASE_DE_DATOS.md`).
- **data/repository:** traduce entidades Room ↔ modelos de dominio; nunca se filtra un tipo de Room a la UI ni un modelo de dominio a Room directamente.
- **viewmodel/:** exponen `StateFlow` de UI state; delegan toda regla de negocio a domain/data.
- **ui/:** Jetpack Compose + Material 3, con ilustraciones vectoriales propias y Canvas para el Constructor y componentes decorativos (nunca solo Material Icons).

## 11. Datos y reglas de negocio

Ver `docs/BASE_DE_DATOS.md`. Reglas clave: una insignia solo se otorga si la estadística agregada real (consultas SQL sobre intentos guardados) cruza el umbral definido en `BadgeEngine`; un plano se marca desbloqueado solo la primera vez que se aprueba su reto (`UPDATE ... WHERE unlockedAt IS NULL`); el progreso de un reto nunca retrocede (`bestStars = max(anterior, nuevo)`).

## 12. UX e infancia

Sesiones de 5-20 min, sin vidas de espera, sin rankings en línea, sin presión social ni compras. Feedback siempre con explicación educativa breve (nunca solo "Correcto/Incorrecto"). Menos del 50% de la experiencia principal es opción múltiple: la mecánica central es construir, conectar, simular y comparar.

## 13. Privacidad

Sin `INTERNET`, sin cámara/micrófono/ubicación, sin nombre real, sin analítica ni anuncios. Persistencia 100% local vía Room.

## 14. Pruebas

75 pruebas de dominio puro (`StructureEngine`, `BadgeEngine`, `ProgressEngine`) verificadas realmente en JVM (ver `docs/BUILD_REPORT.md`), más 14 pruebas de persistencia Room/Robolectric escritas y listas para ejecutarse con el Android Gradle Plugin. Total: 89 pruebas.

## 15. Limitaciones conocidas

- El motor de simulación es conceptual/didáctico, no un solver de ingeniería estructural real (ver disclaimer en `StructureEngine.kt`).
- Las 14 pruebas de Room/Robolectric no se ejecutaron en este entorno de desarrollo por falta de Android SDK; sí se verificó su sintaxis y se diseñaron sobre el esquema real.
- `assembleDebug` no se verificó localmente (ver `docs/BUILD_REPORT.md`); se apoya en Android Studio o en el CI de GitHub Actions incluido.
- Las ilustraciones son vectoriales (generadas programáticamente) y con Canvas de Compose, no arte raster ilustrado a mano.

## 16. Mejoras futuras

- Estructuras en 3D o con cargas dinámicas (simulación física real con Box2D u otro motor).
- Modo "repaso" dirigido a los objetivos concretamente fallados en el último intento.
- Más capítulos temáticos (puentes colgantes, arcos, cerchas complejas).
- Exportar/objetos imprimibles reales de los planos desbloqueados.

## 17. Conclusiones

Constructópolis cumple los tres pilares exigidos: contenido educativo real (motor de reglas verificado con pruebas reales), identidad visual propia (paleta industrial, ilustraciones vectoriales/Canvas, Constructor interactivo) y arquitectura de producto (Room real, MVVM, 40 retos, gamificación ligada a acciones). Queda pendiente la verificación de compilación completa de Android, documentada honestamente en `docs/BUILD_REPORT.md`.
