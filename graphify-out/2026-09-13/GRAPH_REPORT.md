# Graph Report - CuteAnimalsTraceDraw  (2026-09-13)

## Corpus Check
- 28 files · ~7,474 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 148 nodes · 203 edges · 29 communities (11 shown, 18 thin omitted)
- Extraction: 88% EXTRACTED · 12% INFERRED · 0% AMBIGUOUS · INFERRED: 24 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `cfab3177`
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
- Screens.kt
- TopLevelDestination
- NavigationShellTest
- TemplateCategory
- LocalTemplateCatalogTest
- CameraPermissionState
- ExploreFiltersTest
- TracingOverlayState
- CameraPermissionStateTest
- CameraPreviewTest

## God Nodes (most connected - your core abstractions)
1. `DrawingTemplate` - 15 edges
2. `TemplateCategory` - 12 edges
3. `filterDrawings()` - 10 edges
4. `TracingOverlayState` - 10 edges
5. `CuteAnimalsApp()` - 9 edges
6. `TraceScreen()` - 8 edges
7. `Difficulty` - 7 edges
8. `ExploreFiltersTest` - 6 edges
9. `TracingOverlayTest` - 6 edges
10. `LocalTemplateCatalog` - 6 edges

## Surprising Connections (you probably didn't know these)
- `TracingOverlayTest` --calls--> `TracingOverlayState`  [INFERRED]
  app/src/androidTest/java/com/sabalapps/cuteanimalstrace/TracingOverlayTest.kt → app/src/main/java/com/sabalapps/cuteanimalstrace/ui/TracingOverlayState.kt
- `CuteAnimalsApp()` --calls--> `TraceScreen()`  [INFERRED]
  app/src/main/java/com/sabalapps/cuteanimalstrace/ui/CuteAnimalsApp.kt → app/src/main/java/com/sabalapps/cuteanimalstrace/ui/TraceScreen.kt
- `ExploreScreen()` --calls--> `filterDrawings()`  [INFERRED]
  app/src/main/java/com/sabalapps/cuteanimalstrace/ui/Screens.kt → app/src/main/java/com/sabalapps/cuteanimalstrace/ui/FilterDrawings.kt
- `LocalTemplateCatalog` --references--> `DrawingTemplate`  [EXTRACTED]
  app/src/main/java/com/sabalapps/cuteanimalstrace/data/LocalTemplateCatalog.kt → app/src/main/java/com/sabalapps/cuteanimalstrace/data/DrawingTemplate.kt
- `filterDrawings()` --references--> `DrawingTemplate`  [EXTRACTED]
  app/src/main/java/com/sabalapps/cuteanimalstrace/ui/FilterDrawings.kt → app/src/main/java/com/sabalapps/cuteanimalstrace/data/DrawingTemplate.kt

## Import Cycles
- None detected.

## Communities (29 total, 18 thin omitted)

### Community 0 - "Cute Animals: Trace & Draw"
Cohesion: 0.29
Nodes (7): Android SDK requirements, CameraX overlay requirement, Cute Animals: Trace & Draw, Kotlin and Jetpack Compose, Material 3 themes, Offline operation, Simple architecture with stable AndroidX

### Community 1 - "TracingOverlayTest"
Cohesion: 0.26
Nodes (5): TracingOverlayTest, MainActivity, CuteAnimalsTheme(), Bundle, ComponentActivity

### Community 2 - "gradlew"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

### Community 19 - "Screens.kt"
Cohesion: 0.40
Nodes (13): DrawingTemplate, CuteAnimalsApp(), DrawingCard(), DrawingDetailScreen(), ExploreScreen(), FavoritesScreen(), HomeScreen(), Illustration() (+5 more)

### Community 20 - "TopLevelDestination"
Cohesion: 0.22
Nodes (6): DrawingDestination, TopLevelDestination, Explore, Favorites, Home, Settings

### Community 22 - "TemplateCategory"
Cohesion: 0.11
Nodes (15): Difficulty, Detailed, Easy, Medium, TemplateCategory, BabyAnimals, Bears, Bunnies (+7 more)

### Community 24 - "CameraPermissionState"
Cohesion: 0.33
Nodes (5): CameraPermissionState, Blocked, Denied, Granted, NotRequested

### Community 26 - "TracingOverlayState"
Cohesion: 0.18
Nodes (8): CameraPreview(), activity(), TraceScreen(), OverlayControls(), TracingOverlay(), TracingOverlayState, TracingOverlayStateTest, Modifier

## Knowledge Gaps
- **34 isolated node(s):** `Easy`, `Medium`, `Detailed`, `Cats`, `Dogs` (+29 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **18 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `DrawingTemplate` connect `Screens.kt` to `TracingOverlayState`, `NavigationShellTest`, `TemplateCategory`?**
  _High betweenness centrality (0.112) - this node is a cross-community bridge._
- **Why does `LocalTemplateCatalog` connect `NavigationShellTest` to `TracingOverlayTest`, `Screens.kt`, `TopLevelDestination`?**
  _High betweenness centrality (0.066) - this node is a cross-community bridge._
- **Why does `filterDrawings()` connect `TemplateCategory` to `Screens.kt`?**
  _High betweenness centrality (0.063) - this node is a cross-community bridge._
- **Are the 6 inferred relationships involving `filterDrawings()` (e.g. with `ExploreScreen()` and `.blankQuery_returnsWholeCatalogInOrder()`) actually correct?**
  _`filterDrawings()` has 6 INFERRED edges - model-reasoned connections that need verification._
- **Are the 7 inferred relationships involving `TracingOverlayState` (e.g. with `TracingOverlayTest` and `TraceScreen()`) actually correct?**
  _`TracingOverlayState` has 7 INFERRED edges - model-reasoned connections that need verification._
- **Are the 6 inferred relationships involving `CuteAnimalsApp()` (e.g. with `DrawingDetailScreen()` and `ExploreScreen()`) actually correct?**
  _`CuteAnimalsApp()` has 6 INFERRED edges - model-reasoned connections that need verification._
- **What connects `Easy`, `Medium`, `Detailed` to the rest of the system?**
  _34 weakly-connected nodes found - possible documentation gaps or missing edges._