# Graph Report - CuteAnimalsTraceDraw  (2026-09-15)

## Corpus Check
- 40 files · ~262,749 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 258 nodes · 382 edges · 35 communities (16 shown, 19 thin omitted)
- Extraction: 90% EXTRACTED · 10% INFERRED · 0% AMBIGUOUS · INFERRED: 39 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `26a94a5d`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- Cute Animals: Trace & Draw
- TracingOverlayTest
- gradlew
- ExampleInstrumentedTest
- ExampleUnitTest
- Gradle build and test validation
- HDPI square Android launcher icon: white robot head on green grid
- HDPI round Android launcher icon: white robot head on green grid
- MDPI square Android launcher icon: white robot head on green grid
- MDPI round Android launcher icon: white robot head on green grid
- XHDPI square Android launcher icon: white robot head on green grid
- Circular Android launcher icon: white robot head on green grid
- Rounded-square Android launcher icon: white robot head on green grid
- Circular Android launcher icon: white robot head on green grid
- Rounded-square Android launcher icon: white robot head on green grid
- Circular Android launcher icon: white robot head on green grid
- DrawingTemplate
- LocalTemplateCatalog
- UserPreferencesRepository
- TemplateCategory
- LocalTemplateCatalogTest
- CameraPermissionState
- ExploreFiltersTest
- TracingOverlayState
- CameraPermissionStateTest
- CameraPreviewTest
- UserPreferencesViewModel
- SavedPreferencesTest
- Cute Animals: Trace & Draw — Production Image Generation Prompt
- generate_trace_assets.py
- GeneratorTests
- README-imagegen.md

## God Nodes (most connected - your core abstractions)
1. `TracingOverlayState` - 20 edges
2. `DrawingTemplate` - 17 edges
3. `UserPreferencesRepository` - 16 edges
4. `Cute Animals: Trace & Draw — Production Image Generation Prompt` - 16 edges
5. `CuteAnimalsApp()` - 13 edges
6. `TemplateCategory` - 12 edges
7. `SettingsScreen()` - 12 edges
8. `GeneratorTests` - 12 edges
9. `run()` - 11 edges
10. `filterDrawings()` - 10 edges

## Surprising Connections (you probably didn't know these)
- `TracingOverlayTest` --calls--> `TracingOverlayState`  [INFERRED]
  app/src/androidTest/java/com/sabalapps/cuteanimalstrace/TracingOverlayTest.kt → app/src/main/java/com/sabalapps/cuteanimalstrace/ui/TracingOverlayState.kt
- `UserPreferencesViewModel` --calls--> `UserPreferencesRepository`  [INFERRED]
  app/src/main/java/com/sabalapps/cuteanimalstrace/ui/UserPreferencesViewModel.kt → app/src/main/java/com/sabalapps/cuteanimalstrace/data/UserPreferences.kt
- `CuteAnimalsApp()` --calls--> `SettingsScreen()`  [INFERRED]
  app/src/main/java/com/sabalapps/cuteanimalstrace/ui/CuteAnimalsApp.kt → app/src/main/java/com/sabalapps/cuteanimalstrace/ui/SettingsScreen.kt
- `ExploreScreen()` --calls--> `filterDrawings()`  [INFERRED]
  app/src/main/java/com/sabalapps/cuteanimalstrace/ui/Screens.kt → app/src/main/java/com/sabalapps/cuteanimalstrace/ui/FilterDrawings.kt
- `TraceScreen()` --calls--> `OverlayControls()`  [INFERRED]
  app/src/main/java/com/sabalapps/cuteanimalstrace/ui/TraceScreen.kt → app/src/main/java/com/sabalapps/cuteanimalstrace/ui/TracingOverlay.kt

## Import Cycles
- None detected.

## Communities (35 total, 19 thin omitted)

### Community 0 - "Cute Animals: Trace & Draw"
Cohesion: 0.29
Nodes (7): Android SDK requirements, CameraX overlay requirement, Cute Animals: Trace & Draw, Kotlin and Jetpack Compose, Material 3 themes, Offline operation, Simple architecture with stable AndroidX

### Community 1 - "TracingOverlayTest"
Cohesion: 0.23
Nodes (5): TracingOverlayTest, MainActivity, CuteAnimalsTheme(), Bundle, ComponentActivity

### Community 2 - "gradlew"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

### Community 19 - "DrawingTemplate"
Cohesion: 0.23
Nodes (19): DrawingTemplate, UserPreferences, CameraPreview(), Modifier, CuteAnimalsApp(), DrawingCard(), DrawingDetailScreen(), ExploreScreen() (+11 more)

### Community 20 - "LocalTemplateCatalog"
Cohesion: 0.11
Nodes (8): NavigationShellTest, LocalTemplateCatalog, DrawingDestination, TopLevelDestination, Explore, Favorites, Home, Settings

### Community 21 - "UserPreferencesRepository"
Cohesion: 0.11
Nodes (11): Appearance, Dark, Light, System, Keys, UserPreferencesRepository, PreferenceSwitch(), SettingsScreen() (+3 more)

### Community 22 - "TemplateCategory"
Cohesion: 0.11
Nodes (15): Difficulty, Detailed, Easy, Medium, TemplateCategory, BabyAnimals, Bears, Bunnies (+7 more)

### Community 24 - "CameraPermissionState"
Cohesion: 0.33
Nodes (5): CameraPermissionState, Blocked, Denied, Granted, NotRequested

### Community 26 - "TracingOverlayState"
Cohesion: 0.14
Nodes (6): Modifier, OpacityControl(), OverlayActions(), OverlayControls(), TracingOverlayState, TracingOverlayStateTest

### Community 29 - "UserPreferencesViewModel"
Cohesion: 0.38
Nodes (3): AndroidViewModel, UserPreferencesViewModel, Job

### Community 31 - "Cute Animals: Trace & Draw — Production Image Generation Prompt"
Cohesion: 0.12
Nodes (16): Baby Animals — 10, Bears — 8, Bunnies — 10, Cats — 15, Cute Animals: Trace & Draw — Production Image Generation Prompt, Difficulty, Dogs — 15, Featured images (+8 more)

### Community 32 - "generate_trace_assets.py"
Cohesion: 0.27
Nodes (17): Exception, android_record(), atomic_json(), check_pixels(), convert_image(), destination(), final_audit(), is_lossless_webp() (+9 more)

### Community 33 - "GeneratorTests"
Cohesion: 0.18
Nodes (3): drawing_bytes(), GeneratorTests, Offline tests: no API calls and no production asset writes.

## Knowledge Gaps
- **54 isolated node(s):** `Easy`, `Medium`, `Detailed`, `Cats`, `Dogs` (+49 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **19 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `CuteAnimalsApp()` connect `DrawingTemplate` to `TracingOverlayTest`, `UserPreferencesViewModel`, `LocalTemplateCatalog`, `UserPreferencesRepository`?**
  _High betweenness centrality (0.085) - this node is a cross-community bridge._
- **Why does `DrawingTemplate` connect `DrawingTemplate` to `TracingOverlayState`, `LocalTemplateCatalog`, `TemplateCategory`?**
  _High betweenness centrality (0.069) - this node is a cross-community bridge._
- **Why does `TraceScreen()` connect `DrawingTemplate` to `TracingOverlayState`?**
  _High betweenness centrality (0.068) - this node is a cross-community bridge._
- **Are the 9 inferred relationships involving `TracingOverlayState` (e.g. with `TracingOverlayTest` and `TraceScreen()`) actually correct?**
  _`TracingOverlayState` has 9 INFERRED edges - model-reasoned connections that need verification._
- **Are the 4 inferred relationships involving `UserPreferencesRepository` (e.g. with `UserPreferencesViewModel` and `.defaultOpacityIsBoundedAndRejectsInvalidValues()`) actually correct?**
  _`UserPreferencesRepository` has 4 INFERRED edges - model-reasoned connections that need verification._
- **Are the 8 inferred relationships involving `CuteAnimalsApp()` (e.g. with `.recordViewed()` and `.toggleFavorite()`) actually correct?**
  _`CuteAnimalsApp()` has 8 INFERRED edges - model-reasoned connections that need verification._
- **What connects `Easy`, `Medium`, `Detailed` to the rest of the system?**
  _54 weakly-connected nodes found - possible documentation gaps or missing edges._