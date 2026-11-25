# AeroDex — Prueba Técnica KMP (Maestro-Detalle de Aeronaves)

Aplicación móvil **Kotlin Multiplatform (Android e iOS)** con **Compose Multiplatform**, basada en la plantilla [`afalabarce/kmp-template`](https://github.com/afalabarce/kmp-template) y siguiendo la guía de diseño **AeroDex Expressive** (Material 3 Expressive) y accesibilidad **WCAG 2.2 AA**.

> Ver documentación técnica completa (decisiones de diseño, arquitectura, stack tecnológico, WCAG 2.2, Detekt y estrategia de testing): [DOCUMENTACION_TECNICA.md](DOCUMENTACION_TECNICA.md).

---

## API Pública Utilizada

- **Flight API - Aircraft Database**: [`https://flightapi.afalabarce.dev/swagger/openapi.json`](https://flightapi.afalabarce.dev/swagger/openapi.json)
- Endpoints consumidos mediante **Ktorfit** sobre **Ktor**:
  - `GET /api/v1/models`: búsqueda libre (`q`), filtro por categoría (`category`) y paginación por cursor (`cursor`, `limit`).
  - `GET /api/v1/models/{icaoCode}`: ficha técnica completa del modelo (dimensiones, pesos, rendimiento, capacidad, planta motriz, variantes y fotografías).

---

## Cómo Ejecutar el Proyecto

### Requisitos
- JDK 17+ (configurado en `JAVA_HOME`)
- Android Studio / Gradle Wrapper (`./gradlew`)
- Para iOS: Xcode 15+ en macOS

### Android
```bash
./gradlew :androidApp:assembleDebug
```
O abrir el proyecto en Android Studio y ejecutar la configuración `androidApp`.

### iOS
Abrir `iosApp/iosApp.xcodeproj` en Xcode y ejecutar sobre un simulador o dispositivo iOS. El framework compartido (`PresentationUi`) se compila automáticamente desde Gradle.

### Tests Unitarios
```bash
./gradlew :shared:domain:usecases:testAndroidHostTest \
          :shared:data:repository:testAndroidHostTest \
          :shared:presentation:viewmodels:testAndroidHostTest
```
(En macOS también pueden ejecutarse los equivalentes `iosSimulatorArm64Test`).

---

## Decisiones Principales de Arquitectura

### Decisión de plataforma: Kotlin Multiplatform

Se eligió **Kotlin Multiplatform (KMP)** para compartir las capas de dominio, datos y presentación sin sacrificar el desarrollo nativo de Android. La arquitectura mantiene esas capas en módulos Kotlin con destino Android: el código compartido se compila y consume directamente desde `androidApp`, por lo que las funcionalidades implementadas en esas capas son **100 % compatibles con Android** y no requieren una reescritura, una capa de adaptación ni un cambio de lenguaje o herramientas.

Para el trabajo habitual de Android, la curva de aprendizaje es **nula**: se mantienen Kotlin, Gradle, Android Studio, las APIs de Android y el mismo flujo de compilación y depuración. La ampliación hacia iOS se incorpora progresivamente, aislando las particularidades de plataforma en los puntos de integración correspondientes (`androidMain`/`iosMain` y los puntos de entrada nativos). Así, el esfuerzo crece de forma lineal con las funcionalidades compartidas y las adaptaciones específicas que realmente se necesiten, en lugar de duplicar la lógica por plataforma. El aprendizaje adicional de APIs propias de iOS solo aplica al trabajo específico de esa plataforma; no altera el desarrollo Android.

### 1. Arquitectura Modular KMP (`Data`, `Domain`, `Presentation`)
- **`shared/domain` (`models`, `repository`, `usecases`)**: modelos puros de dominio (`AircraftModelSummary`, `AircraftModelDetail`, `AircraftCategory`, `DataSourceOrigin`), contrato `AircraftRepository` y casos de uso (`SearchAircraftModelsUseCase`, `GetAircraftModelDetailUseCase`). Sin dependencias de red ni UI.
- **`shared/data` (`models`, `datasources`, `datasources-core`, `repository`)**:
  - **Ktorfit sobre Ktor** (`ApiService`) con `ContentNegotiation` (`kotlinx.serialization.json`) y `Logging`.
  - **Room Multiplatform** (`AppDatabase`, `AircraftDao`) para persistencia local.
  - **Inyección de dependencias** con Koin 4.
- **`shared/presentation` (`viewModels`, `ui`)**: `AircraftViewModel` con `StateFlow<AircraftUiState>` y UI compartida en Compose Multiplatform (`AeroDexSearchPane`, `AircraftDetailPane`).

### 2. Concurrencia y Cancelación de Búsquedas
- En `AircraftViewModel`, cada nueva búsqueda (al escribir con debounce de 300 ms, pulsar buscar o cambiar de categoría) **cancela explícitamente el `Job` anterior** (`searchJob?.cancel()`) e incrementa un token de secuencia (`activeSearchToken`).
- Las `CancellationException` se propagan correctamente (nunca se capturan como errores de UI ni activan el fallback offline) y cualquier respuesta tardía cuyo token no coincida con el activo se descarta, garantizando que nunca se muestren resultados de búsquedas obsoletas.

### 3. Caché en Memoria (TTL) y Modo Offline
- **Caché en memoria con TTL de 3 minutos (`AircraftMemoryCache`)**: protegida con `Mutex` y clave normalizada `(query, category, cursor, limit)` para búsquedas y `icaoTypeCode` para detalles. Si existe una entrada vigente dentro del TTL, se devuelve inmediatamente (`DataSourceOrigin.MEMORY_CACHE`) sin realizar llamada de red. Al expirar, se consulta la API y se actualiza la caché.
- **Modo Offline (Room)**: cada respuesta exitosa de red se persiste de forma asíncrona en Room (`cached_aircraft_summary` y `cached_aircraft_detail`). Si falla la conectividad y no hay caché en memoria vigente, el repositorio recurre automáticamente a Room (`DataSourceOrigin.OFFLINE_CACHE`) y la UI muestra un indicador visual de modo sin conexión.

### 4. Diseño Adaptativo (Móviles, Tablets y Plegables en Android e iOS)
- La raíz de `App()` utiliza `BoxWithConstraints` para adaptarse dinámicamente en ambas plataformas:
  - **Ancho compacto (`< 600.dp`)**: navegación de panel único (Listado ↔ Detalle con botón de retorno).
  - **Ancho medio/expandido (`>= 600.dp`, tablets, iPads y plegables desplegados)**: diseño **Master-Detail de doble panel** simultáneo (listado con selección resaltada a la izquierda y ficha técnica adaptativa de 1 a 3 columnas a la derecha).

---

## Tests Incluidos
- **Domain (`AircraftUseCasesTest`)**: normalización de parámetros, validación de códigos ICAO y delegación al repositorio mockeado.
- **Data (`AircraftRepositoryCacheTest`)**: acierto de caché dentro del TTL sin llamada de red, expiración tras TTL con refresco de red, fallback offline ante error de red y propagación de `CancellationException`.
- **Presentation (`AircraftViewModelTest`)**: cancelación de búsqueda lenta en curso al lanzar una nueva búsqueda rápida, selección/limpieza de detalle y estado offline.
