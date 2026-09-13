# Graph Report - CuteAnimalsTraceDraw  (2026-09-13)

## Corpus Check
- 16 files · ~4,943 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 93 nodes · 111 edges · 24 communities (9 shown, 15 thin omitted)
- Extraction: 95% EXTRACTED · 5% INFERRED · 0% AMBIGUOUS · INFERRED: 6 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `f665f82f`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- Cute Animals: Trace & Draw
- MainActivity.kt
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

## God Nodes (most connected - your core abstractions)
1. `DrawingTemplate` - 10 edges
2. `TemplateCategory` - 9 edges
3. `CuteAnimalsApp()` - 9 edges
4. `ScreenColumn()` - 6 edges
5. `Intro()` - 6 edges
6. `HomeScreen()` - 6 edges
7. `TraceScreen()` - 6 edges
8. `TopLevelDestination` - 6 edges
9. `Cute Animals: Trace & Draw` - 6 edges
10. `NavigationShellTest` - 5 edges

## Surprising Connections (you probably didn't know these)
- `LocalTemplateCatalog` --references--> `DrawingTemplate`  [EXTRACTED]
  app/src/main/java/com/sabalapps/cuteanimalstrace/data/LocalTemplateCatalog.kt → app/src/main/java/com/sabalapps/cuteanimalstrace/data/DrawingTemplate.kt
- `CuteAnimalsApp()` --calls--> `DrawingDetailScreen()`  [INFERRED]
  app/src/main/java/com/sabalapps/cuteanimalstrace/ui/CuteAnimalsApp.kt → app/src/main/java/com/sabalapps/cuteanimalstrace/ui/Screens.kt
- `CuteAnimalsApp()` --calls--> `ExploreScreen()`  [INFERRED]
  app/src/main/java/com/sabalapps/cuteanimalstrace/ui/CuteAnimalsApp.kt → app/src/main/java/com/sabalapps/cuteanimalstrace/ui/Screens.kt
- `CuteAnimalsApp()` --calls--> `FavoritesScreen()`  [INFERRED]
  app/src/main/java/com/sabalapps/cuteanimalstrace/ui/CuteAnimalsApp.kt → app/src/main/java/com/sabalapps/cuteanimalstrace/ui/Screens.kt
- `CuteAnimalsApp()` --calls--> `HomeScreen()`  [INFERRED]
  app/src/main/java/com/sabalapps/cuteanimalstrace/ui/CuteAnimalsApp.kt → app/src/main/java/com/sabalapps/cuteanimalstrace/ui/Screens.kt

## Import Cycles
- None detected.

## Communities (24 total, 15 thin omitted)

### Community 0 - "Cute Animals: Trace & Draw"
Cohesion: 0.29
Nodes (7): Android SDK requirements, CameraX overlay requirement, Cute Animals: Trace & Draw, Kotlin and Jetpack Compose, Material 3 themes, Offline operation, Simple architecture with stable AndroidX

### Community 1 - "MainActivity.kt"
Cohesion: 0.43
Nodes (4): MainActivity, CuteAnimalsTheme(), Bundle, ComponentActivity

### Community 2 - "gradlew"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

### Community 19 - "Screens.kt"
Cohesion: 0.44
Nodes (13): DrawingTemplate, CuteAnimalsApp(), DrawingCard(), DrawingDetailScreen(), ExploreScreen(), FavoritesScreen(), HomeScreen(), Illustration() (+5 more)

### Community 20 - "TopLevelDestination"
Cohesion: 0.15
Nodes (8): LocalTemplateCatalog, DrawingDestination, TopLevelDestination, Explore, Favorites, Home, Settings, ImageVector

### Community 22 - "TemplateCategory"
Cohesion: 0.14
Nodes (13): Difficulty, Detailed, Easy, Medium, TemplateCategory, BabyAnimals, Bears, Bunnies (+5 more)

## Knowledge Gaps
- **30 isolated node(s):** `Easy`, `Medium`, `Detailed`, `Cats`, `Dogs` (+25 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **15 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `DrawingTemplate` connect `Screens.kt` to `TopLevelDestination`, `TemplateCategory`?**
  _High betweenness centrality (0.164) - this node is a cross-community bridge._
- **Why does `LocalTemplateCatalog` connect `TopLevelDestination` to `Screens.kt`, `NavigationShellTest`?**
  _High betweenness centrality (0.116) - this node is a cross-community bridge._
- **Why does `CuteAnimalsApp()` connect `Screens.kt` to `MainActivity.kt`, `TopLevelDestination`?**
  _High betweenness centrality (0.100) - this node is a cross-community bridge._
- **Are the 6 inferred relationships involving `CuteAnimalsApp()` (e.g. with `DrawingDetailScreen()` and `ExploreScreen()`) actually correct?**
  _`CuteAnimalsApp()` has 6 INFERRED edges - model-reasoned connections that need verification._
- **What connects `Easy`, `Medium`, `Detailed` to the rest of the system?**
  _30 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `TemplateCategory` be split into smaller, more focused modules?**
  _Cohesion score 0.14285714285714285 - nodes in this community are weakly interconnected._