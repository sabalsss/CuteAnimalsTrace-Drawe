# Graph Report - CuteAnimalsTraceDraw  (2026-09-17)

## Corpus Check
- 42 files · ~264,391 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 281 nodes · 434 edges · 36 communities (17 shown, 19 thin omitted)
- Extraction: 90% EXTRACTED · 10% INFERRED · 0% AMBIGUOUS · INFERRED: 44 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `26a94a5d`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- Cute Animals: Trace & Draw
- UserPreferencesViewModel
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
- CuteAnimalsApp.kt
- UserPreferencesRepository
- TemplateCategory
- NavigationShellTest
- CameraPermissionState
- ExploreFiltersTest
- TracingOverlayState
- CameraPermissionStateTest
- CameraPreviewTest
- AssetTemplateImage
- SavedPreferencesTest
- Cute Animals: Trace & Draw — Production Image Generation Prompt
- generate_trace_assets.py
- GeneratorTests
- README-imagegen.md
- TraceScreen

## God Nodes (most connected - your core abstractions)
1. `TracingOverlayState` - 20 edges
2. `DrawingTemplate` - 18 edges
3. `UserPreferencesRepository` - 16 edges
4. `Cute Animals: Trace & Draw — Production Image Generation Prompt` - 16 edges
5. `CuteAnimalsApp()` - 14 edges
6. `TemplateCategory` - 12 edges
7. `filterDrawings()` - 12 edges
8. `SettingsScreen()` - 12 edges
9. `GeneratorTests` - 12 edges
10. `run()` - 11 edges

## Surprising Connections (you probably didn't know these)
- `TracingOverlayTest` --calls--> `TracingOverlayState`  [INFERRED]
  app/src/androidTest/java/com/sabalapps/cuteanimalstrace/TracingOverlayTest.kt → app/src/main/java/com/sabalapps/cuteanimalstrace/ui/TracingOverlayState.kt
- `FilterDrawingsTest` --calls--> `DrawingTemplate`  [INFERRED]
  app/src/test/java/com/sabalapps/cuteanimalstrace/ui/FilterDrawingsTest.kt → app/src/main/java/com/sabalapps/cuteanimalstrace/data/DrawingTemplate.kt
- `UserPreferencesViewModel` --calls--> `UserPreferencesRepository`  [INFERRED]
  app/src/main/java/com/sabalapps/cuteanimalstrace/ui/UserPreferencesViewModel.kt → app/src/main/java/com/sabalapps/cuteanimalstrace/data/UserPreferences.kt
- `HomeScreen()` --calls--> `AssetTemplateImage()`  [INFERRED]
  app/src/main/java/com/sabalapps/cuteanimalstrace/ui/Screens.kt → app/src/main/java/com/sabalapps/cuteanimalstrace/ui/AssetTemplateImage.kt
- `TemplateImage()` --calls--> `AssetTemplateImage()`  [INFERRED]
  app/src/main/java/com/sabalapps/cuteanimalstrace/ui/Screens.kt → app/src/main/java/com/sabalapps/cuteanimalstrace/ui/AssetTemplateImage.kt

## Import Cycles
- None detected.

## Communities (36 total, 19 thin omitted)

### Community 0 - "Cute Animals: Trace & Draw"
Cohesion: 0.29
Nodes (7): Android SDK requirements, CameraX overlay requirement, Cute Animals: Trace & Draw, Kotlin and Jetpack Compose, Material 3 themes, Offline operation, Simple architecture with stable AndroidX

### Community 1 - "UserPreferencesViewModel"
Cohesion: 0.14
Nodes (8): AndroidViewModel, TracingOverlayTest, MainActivity, CuteAnimalsTheme(), UserPreferencesViewModel, Bundle, ComponentActivity, Job

### Community 2 - "gradlew"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

### Community 19 - "DrawingTemplate"
Cohesion: 0.24
Nodes (16): DrawingTemplate, AssetManager, LocalTemplateCatalog, CuteAnimalsApp(), DrawingCard(), DrawingDetailScreen(), DrawingList(), EmptyCollection() (+8 more)

### Community 20 - "CuteAnimalsApp.kt"
Cohesion: 0.18
Nodes (8): DrawingDestination, TopLevelDestination, Explore, Favorites, Home, Settings, Illustration(), ImageVector

### Community 21 - "UserPreferencesRepository"
Cohesion: 0.12
Nodes (12): Appearance, Dark, Light, System, Keys, UserPreferences, UserPreferencesRepository, PreferenceSwitch() (+4 more)

### Community 22 - "TemplateCategory"
Cohesion: 0.11
Nodes (15): Difficulty, Detailed, Easy, Medium, TemplateCategory, BabyAnimals, Bears, Bunnies (+7 more)

### Community 24 - "CameraPermissionState"
Cohesion: 0.33
Nodes (5): CameraPermissionState, Blocked, Denied, Granted, NotRequested

### Community 26 - "TracingOverlayState"
Cohesion: 0.13
Nodes (8): Bitmap, Modifier, OpacityControl(), OverlayActions(), OverlayControls(), TracingOverlay(), TracingOverlayState, TracingOverlayStateTest

### Community 29 - "AssetTemplateImage"
Cohesion: 0.19
Nodes (8): ProductionTemplateCatalogTest, AssetImageState, AssetTemplateImage(), AssetManager, Bitmap, Modifier, TemplateBitmapLoader, ColorFilter

### Community 31 - "Cute Animals: Trace & Draw — Production Image Generation Prompt"
Cohesion: 0.12
Nodes (16): Baby Animals — 10, Bears — 8, Bunnies — 10, Cats — 15, Cute Animals: Trace & Draw — Production Image Generation Prompt, Difficulty, Dogs — 15, Featured images (+8 more)

### Community 32 - "generate_trace_assets.py"
Cohesion: 0.27
Nodes (17): Exception, android_record(), atomic_json(), check_pixels(), convert_image(), destination(), final_audit(), is_lossless_webp() (+9 more)

### Community 33 - "GeneratorTests"
Cohesion: 0.18
Nodes (3): drawing_bytes(), GeneratorTests, Offline tests: no API calls and no production asset writes.

### Community 36 - "TraceScreen"
Cohesion: 0.29
Nodes (8): CameraPreview(), Modifier, activity(), TraceScreen(), Bitmap, loadUserImage(), ContentResolver, Uri

## Knowledge Gaps
- **54 isolated node(s):** `Easy`, `Medium`, `Detailed`, `Cats`, `Dogs` (+49 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **19 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `CuteAnimalsApp()` connect `DrawingTemplate` to `UserPreferencesViewModel`, `CuteAnimalsApp.kt`, `UserPreferencesRepository`, `TraceScreen`?**
  _High betweenness centrality (0.117) - this node is a cross-community bridge._
- **Why does `TraceScreen()` connect `TraceScreen` to `TracingOverlayState`, `DrawingTemplate`, `UserPreferencesRepository`?**
  _High betweenness centrality (0.080) - this node is a cross-community bridge._
- **Why does `DrawingTemplate` connect `DrawingTemplate` to `TracingOverlayState`, `TraceScreen`, `TemplateCategory`?**
  _High betweenness centrality (0.080) - this node is a cross-community bridge._
- **Are the 9 inferred relationships involving `TracingOverlayState` (e.g. with `TracingOverlayTest` and `TraceScreen()`) actually correct?**
  _`TracingOverlayState` has 9 INFERRED edges - model-reasoned connections that need verification._
- **Are the 4 inferred relationships involving `UserPreferencesRepository` (e.g. with `UserPreferencesViewModel` and `.defaultOpacityIsBoundedAndRejectsInvalidValues()`) actually correct?**
  _`UserPreferencesRepository` has 4 INFERRED edges - model-reasoned connections that need verification._
- **Are the 8 inferred relationships involving `CuteAnimalsApp()` (e.g. with `.recordViewed()` and `.toggleFavorite()`) actually correct?**
  _`CuteAnimalsApp()` has 8 INFERRED edges - model-reasoned connections that need verification._
- **What connects `Easy`, `Medium`, `Detailed` to the rest of the system?**
  _54 weakly-connected nodes found - possible documentation gaps or missing edges._