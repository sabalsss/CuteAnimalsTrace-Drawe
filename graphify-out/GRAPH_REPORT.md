# Graph Report - CuteAnimalsTraceDraw  (2026-09-20)

## Corpus Check
- 47 files · ~1,114,301 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 323 nodes · 533 edges · 37 communities (17 shown, 20 thin omitted)
- Extraction: 90% EXTRACTED · 10% INFERRED · 0% AMBIGUOUS · INFERRED: 51 edges (avg confidence: 0.8)
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
- CuteAnimalsTheme
- CameraPermissionState
- ExploreFiltersTest
- TracingOverlayState
- CameraPermissionStateTest
- CameraPreviewTest
- ProductionTemplateCatalogTest
- SavedPreferencesTest
- Cute Animals: Trace & Draw — Production Image Generation Prompt
- generate_preview_assets.py
- GeneratorTests
- README-imagegen.md
- PreviewAssetTests
- README-previews.md

## God Nodes (most connected - your core abstractions)
1. `TracingOverlayState` - 21 edges
2. `ValidationError` - 19 edges
3. `DrawingTemplate` - 18 edges
4. `UserPreferencesRepository` - 16 edges
5. `Cute Animals: Trace & Draw — Production Image Generation Prompt` - 16 edges
6. `CuteAnimalsApp()` - 14 edges
7. `TemplateCategory` - 12 edges
8. `filterDrawings()` - 12 edges
9. `SettingsScreen()` - 12 edges
10. `CuteAnimalsTheme()` - 12 edges

## Surprising Connections (you probably didn't know these)
- `FilterDrawingsTest` --calls--> `DrawingTemplate`  [INFERRED]
  app/src/test/java/com/sabalapps/cuteanimalstrace/ui/FilterDrawingsTest.kt → app/src/main/java/com/sabalapps/cuteanimalstrace/data/DrawingTemplate.kt
- `UserPreferencesViewModel` --calls--> `UserPreferencesRepository`  [INFERRED]
  app/src/main/java/com/sabalapps/cuteanimalstrace/ui/UserPreferencesViewModel.kt → app/src/main/java/com/sabalapps/cuteanimalstrace/data/UserPreferences.kt
- `HomeScreen()` --calls--> `AssetTemplateImage()`  [INFERRED]
  app/src/main/java/com/sabalapps/cuteanimalstrace/ui/Screens.kt → app/src/main/java/com/sabalapps/cuteanimalstrace/ui/AssetTemplateImage.kt
- `TemplateImage()` --calls--> `AssetTemplateImage()`  [INFERRED]
  app/src/main/java/com/sabalapps/cuteanimalstrace/ui/Screens.kt → app/src/main/java/com/sabalapps/cuteanimalstrace/ui/AssetTemplateImage.kt
- `TracingOverlay()` --calls--> `AssetTemplateImage()`  [INFERRED]
  app/src/main/java/com/sabalapps/cuteanimalstrace/ui/TracingOverlay.kt → app/src/main/java/com/sabalapps/cuteanimalstrace/ui/AssetTemplateImage.kt

## Import Cycles
- None detected.

## Communities (37 total, 20 thin omitted)

### Community 0 - "Cute Animals: Trace & Draw"
Cohesion: 0.29
Nodes (7): Android SDK requirements, CameraX overlay requirement, Cute Animals: Trace & Draw, Kotlin and Jetpack Compose, Material 3 themes, Offline operation, Simple architecture with stable AndroidX

### Community 1 - "UserPreferencesViewModel"
Cohesion: 0.23
Nodes (6): AndroidViewModel, MainActivity, UserPreferencesViewModel, Bundle, ComponentActivity, Job

### Community 2 - "gradlew"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

### Community 19 - "DrawingTemplate"
Cohesion: 0.39
Nodes (14): DrawingTemplate, CuteAnimalsApp(), DrawingCard(), DrawingDetailScreen(), DrawingList(), EmptyCollection(), ExploreScreen(), FavoriteButton() (+6 more)

### Community 20 - "CuteAnimalsApp.kt"
Cohesion: 0.18
Nodes (8): DrawingDestination, TopLevelDestination, Explore, Favorites, Home, Settings, Illustration(), ImageVector

### Community 21 - "UserPreferencesRepository"
Cohesion: 0.09
Nodes (20): Appearance, Dark, Light, System, Keys, UserPreferences, UserPreferencesRepository, CameraPreview() (+12 more)

### Community 22 - "TemplateCategory"
Cohesion: 0.11
Nodes (15): Difficulty, Detailed, Easy, Medium, TemplateCategory, BabyAnimals, Bears, Bunnies (+7 more)

### Community 23 - "CuteAnimalsTheme"
Cohesion: 0.13
Nodes (5): NavigationShellTest, PreviewRoutingTest, AssetManager, LocalTemplateCatalog, CuteAnimalsTheme()

### Community 24 - "CameraPermissionState"
Cohesion: 0.33
Nodes (5): CameraPermissionState, Blocked, Denied, Granted, NotRequested

### Community 26 - "TracingOverlayState"
Cohesion: 0.11
Nodes (9): TracingOverlayTest, Bitmap, Modifier, OpacityControl(), OverlayActions(), OverlayControls(), TracingOverlay(), TracingOverlayState (+1 more)

### Community 29 - "ProductionTemplateCatalogTest"
Cohesion: 0.17
Nodes (8): ProductionTemplateCatalogTest, AssetImageState, AssetTemplateImage(), AssetManager, Bitmap, Modifier, TemplateBitmapLoader, ColorFilter

### Community 31 - "Cute Animals: Trace & Draw — Production Image Generation Prompt"
Cohesion: 0.12
Nodes (16): Baby Animals — 10, Bears — 8, Bunnies — 10, Cats — 15, Cute Animals: Trace & Draw — Production Image Generation Prompt, Difficulty, Dogs — 15, Featured images (+8 more)

### Community 32 - "generate_preview_assets.py"
Cohesion: 0.16
Nodes (32): Exception, align_preview(), audit(), digest(), image_stats(), ink_mask(), load_library(), main() (+24 more)

### Community 33 - "GeneratorTests"
Cohesion: 0.18
Nodes (3): drawing_bytes(), GeneratorTests, Offline tests: no API calls and no production asset writes.

## Knowledge Gaps
- **55 isolated node(s):** `Easy`, `Medium`, `Detailed`, `Cats`, `Dogs` (+50 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **20 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `CuteAnimalsApp()` connect `DrawingTemplate` to `UserPreferencesViewModel`, `CuteAnimalsApp.kt`, `UserPreferencesRepository`, `CuteAnimalsTheme`?**
  _High betweenness centrality (0.094) - this node is a cross-community bridge._
- **Why does `DrawingTemplate` connect `DrawingTemplate` to `TracingOverlayState`, `UserPreferencesRepository`, `TemplateCategory`, `CuteAnimalsTheme`?**
  _High betweenness centrality (0.066) - this node is a cross-community bridge._
- **Why does `TraceScreen()` connect `UserPreferencesRepository` to `TracingOverlayState`, `DrawingTemplate`?**
  _High betweenness centrality (0.062) - this node is a cross-community bridge._
- **Are the 10 inferred relationships involving `TracingOverlayState` (e.g. with `.tracingLoadsOriginalWithoutDependingOnPreview()` and `TracingOverlayTest`) actually correct?**
  _`TracingOverlayState` has 10 INFERRED edges - model-reasoned connections that need verification._
- **Are the 4 inferred relationships involving `UserPreferencesRepository` (e.g. with `UserPreferencesViewModel` and `.defaultOpacityIsBoundedAndRejectsInvalidValues()`) actually correct?**
  _`UserPreferencesRepository` has 4 INFERRED edges - model-reasoned connections that need verification._
- **What connects `Easy`, `Medium`, `Detailed` to the rest of the system?**
  _55 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `UserPreferencesRepository` be split into smaller, more focused modules?**
  _Cohesion score 0.08534850640113797 - nodes in this community are weakly interconnected._