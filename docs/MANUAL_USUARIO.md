# Manual de Usuario — Constructópolis

## 1. Instalación

1. Copia el archivo `Constructopolis-v1.0.0.apk` (carpeta `deliverables/`) al dispositivo Android.
2. Toca el archivo APK y permite la instalación desde "orígenes desconocidos" si el sistema lo pide (solo la primera vez).
3. Abre "Constructópolis" desde el cajón de aplicaciones.

Requisitos: Android 7.0 (API 24) o superior. No requiere conexión a Internet ni cuenta de ningún tipo.

## 2. Primer inicio

Al abrir la app por primera vez verás 4 pantallas breves:

1. **Bienvenida al Taller** — presentación de la Ingeniera Nova.
2. **Elige tus materiales** — madera, acero y concreto.
3. **Construye y pon a prueba** — cómo funciona el Constructor.
4. **Tu taller es privado** — qué datos se guardan (solo alias y avatar, nunca información personal).

Puedes tocar "Saltar" en cualquier momento. Este onboarding no se repite en aperturas posteriores.

## 3. Navegación general

La pantalla **Home** (el Taller) es el centro de todo: muestra tu progreso general y ocho módulos:

| Módulo | Qué hace |
|---|---|
| Conceptos | Explicaciones ilustradas de ideas clave (carga, apoyo, viga, columna, triangulación, esbeltez, estabilidad, presupuesto) |
| Materiales | Ficha de cada material con resistencia, peso y costo |
| Vigas | Retos del capítulo 1 |
| Columnas | Retos del capítulo 2 |
| Torres | Retos del capítulo 3 |
| Cargas y Viento | Retos del capítulo 4 (incluyen viento lateral) |
| Gran Taller de Retos | Retos combinados del capítulo 5 |
| Planos y Logros | Tu colección de insignias y planos desbloqueados |

Toca tu avatar (arriba a la derecha) para entrar a **Mi perfil**.

## 4. Cómo resolver un reto (el Constructor)

1. Entra a cualquier módulo de capítulo (por ejemplo, Vigas) y toca un reto disponible.
2. Verás una cuadrícula con uno o más apoyos ya colocados (triángulos grises).
3. Usa la barra inferior para elegir herramienta:
   - **Nodo:** toca una celda vacía para colocar un punto de unión.
   - **Pieza:** toca un nodo de inicio y luego otro nodo para conectar una viga/columna/diagonal, con el material y el rol que elijas en los chips.
   - **Carga:** toca un nodo para colocarle un peso.
   - **Borrar:** toca un nodo (que no sea apoyo) para quitarlo junto con sus piezas y cargas.
4. Toca **Guardar** en cualquier momento para no perder tu avance.
5. Toca **Probar** para simular. Verás:
   - Si el reto se superó o no, con una explicación (nunca solo "correcto/incorrecto").
   - Estrellas ganadas (0 a 3).
   - Altura, costo frente al presupuesto y porcentaje de estabilidad.
   - Las piezas que están al límite o que fallaron, resaltadas con color **e** ícono (no dependas solo del color).

## 5. Permisos

Constructópolis no solicita ningún permiso especial (sin cámara, micrófono, ubicación ni contactos). Toda la información se guarda solo en el dispositivo.

## 6. Ejemplo guiado

**Reto "Primer Puente":** hay dos apoyos fijos en las esquinas inferiores. Coloca un nodo arriba, en el centro, y conéctalo a cada apoyo con una viga de madera. Guarda y prueba: si el presupuesto lo permite y ninguna viga se rompe, ¡reto superado!

## 7. Errores frecuentes

- **"Tu estructura no está conectada al suelo":** algún nodo con piezas no tiene un camino de piezas hasta un apoyo. Revisa que todas tus piezas formen una sola red conectada a los triángulos grises.
- **"Alguna pieza se rompió":** superaste la capacidad del material en esa pieza. Prueba con un material más resistente (acero) o reparte la carga con más piezas.
- **"Te pasaste del presupuesto":** usa materiales más económicos (madera) o menos piezas.
- La app no avanza de pantalla: verifica que tocaste "Guardar" antes de salir del Constructor si querías conservar cambios.

## 8. Reinicio y desinstalación

- **Reiniciar el progreso:** Android → Ajustes → Aplicaciones → Constructópolis → Almacenamiento → "Borrar datos". Esto reinicia perfil, retos y colección (no se puede deshacer).
- **Desinstalar:** mantén presionado el ícono de la app y selecciona "Desinstalar", o desde Ajustes → Aplicaciones → Constructópolis → Desinstalar.
