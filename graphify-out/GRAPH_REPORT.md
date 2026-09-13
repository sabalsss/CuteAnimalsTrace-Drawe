# Graph Report - CuteAnimalsTraceDraw  (2026-09-13)

## Corpus Check
- 14 files · ~4,177 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 68 nodes · 81 edges · 22 communities (8 shown, 14 thin omitted)
- Extraction: 93% EXTRACTED · 7% INFERRED · 0% AMBIGUOUS · INFERRED: 6 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `09918ab5`
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

## God Nodes (most connected - your core abstractions)
1. `CuteAnimalsApp()` - 9 edges
2. `Intro()` - 7 edges
3. `ScreenColumn()` - 6 edges
4. `Illustration()` - 6 edges
5. `DrawingDetailScreen()` - 6 edges
6. `TraceScreen()` - 6 edges
7. `TopLevelDestination` - 6 edges
8. `Cute Animals: Trace & Draw` - 6 edges
9. `DrawingCard()` - 5 edges
10. `HomeScreen()` - 5 edges

## Surprising Connections (you probably didn't know these)
- `CuteAnimalsApp()` --calls--> `DrawingDetailScreen()`  [INFERRED]
  app/src/main/java/com/sabalapps/cuteanimalstrace/ui/CuteAnimalsApp.kt → app/src/main/java/com/sabalapps/cuteanimalstrace/ui/Screens.kt
- `CuteAnimalsApp()` --calls--> `ExploreScreen()`  [INFERRED]
  app/src/main/java/com/sabalapps/cuteanimalstrace/ui/CuteAnimalsApp.kt → app/src/main/java/com/sabalapps/cuteanimalstrace/ui/Screens.kt
- `CuteAnimalsApp()` --calls--> `FavoritesScreen()`  [INFERRED]
  app/src/main/java/com/sabalapps/cuteanimalstrace/ui/CuteAnimalsApp.kt → app/src/main/java/com/sabalapps/cuteanimalstrace/ui/Screens.kt
- `CuteAnimalsApp()` --calls--> `HomeScreen()`  [INFERRED]
  app/src/main/java/com/sabalapps/cuteanimalstrace/ui/CuteAnimalsApp.kt → app/src/main/java/com/sabalapps/cuteanimalstrace/ui/Screens.kt
- `CuteAnimalsApp()` --calls--> `SettingsScreen()`  [INFERRED]
  app/src/main/java/com/sabalapps/cuteanimalstrace/ui/CuteAnimalsApp.kt → app/src/main/java/com/sabalapps/cuteanimalstrace/ui/Screens.kt

## Import Cycles
- None detected.

## Communities (22 total, 14 thin omitted)

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
Cohesion: 0.41
Nodes (12): CuteAnimalsApp(), PlaceholderDrawing, DrawingCard(), DrawingDetailScreen(), ExploreScreen(), FavoritesScreen(), HomeScreen(), Illustration() (+4 more)

### Community 20 - "TopLevelDestination"
Cohesion: 0.20
Nodes (7): DrawingDestination, TopLevelDestination, Explore, Favorites, Home, Settings, ImageVector

## Knowledge Gaps
- **19 isolated node(s):** `Home`, `Explore`, `Favorites`, `Settings`, `Android SDK requirements` (+14 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **14 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `CuteAnimalsApp()` connect `Screens.kt` to `MainActivity.kt`, `TopLevelDestination`?**
  _High betweenness centrality (0.121) - this node is a cross-community bridge._
- **Are the 6 inferred relationships involving `CuteAnimalsApp()` (e.g. with `DrawingDetailScreen()` and `ExploreScreen()`) actually correct?**
  _`CuteAnimalsApp()` has 6 INFERRED edges - model-reasoned connections that need verification._
- **What connects `Home`, `Explore`, `Favorites` to the rest of the system?**
  _19 weakly-connected nodes found - possible documentation gaps or missing edges._