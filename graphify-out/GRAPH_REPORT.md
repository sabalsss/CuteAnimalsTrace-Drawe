# Graph Report - CuteAnimalsTraceDraw  (2026-09-22)

## Corpus Check
- 56 files · ~1,629,511 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 419 nodes · 779 edges · 39 communities (19 shown, 20 thin omitted)
- Extraction: 90% EXTRACTED · 10% INFERRED · 0% AMBIGUOUS · INFERRED: 78 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `8e28c52c`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- Cute Animals: Trace & Draw
- CuteAnimalsTheme
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
- SadCat.kt
- TraceScreen
- UserPreferencesRepository
- CuteUi.kt
- CuteAnimalsApp.kt
- CameraPermissionState
- ExploreFiltersTest
- TracingOverlayState
- CameraPermissionStateTest
- CameraPreviewTest
- filterDrawings
- SavedPreferencesTest
- Cute Animals: Trace & Draw — Production Image Generation Prompt
- generate_preview_assets.py
- GeneratorTests
- README-imagegen.md
- PreviewAssetTests
- Color preview assets
- DailyReminder
- RateShare.kt

## God Nodes (most connected - your core abstractions)
1. `UserPreferencesRepository` - 24 edges
2. `CuteAnimalsApp()` - 23 edges
3. `TracingOverlayState` - 21 edges
4. `DrawingTemplate` - 20 edges
5. `ValidationError` - 20 edges
6. `TemplateCategory` - 18 edges
7. `Cute Animals: Trace & Draw — Production Image Generation Prompt` - 16 edges
8. `SettingsScreen()` - 15 edges
9. `HomeScreen()` - 14 edges
10. `expand_library()` - 14 edges

## Surprising Connections (you probably didn't know these)
- `TracingOverlayTest` --calls--> `TracingOverlayState`  [INFERRED]
  app/src/androidTest/java/com/sabalapps/cuteanimalstrace/TracingOverlayTest.kt → app/src/main/java/com/sabalapps/cuteanimalstrace/ui/TracingOverlayState.kt
- `FilterDrawingsTest` --calls--> `DrawingTemplate`  [INFERRED]
  app/src/test/java/com/sabalapps/cuteanimalstrace/ui/FilterDrawingsTest.kt → app/src/main/java/com/sabalapps/cuteanimalstrace/data/DrawingTemplate.kt
- `ArtworkPlate()` --calls--> `AssetTemplateImage()`  [INFERRED]
  app/src/main/java/com/sabalapps/cuteanimalstrace/ui/CuteUi.kt → app/src/main/java/com/sabalapps/cuteanimalstrace/ui/AssetTemplateImage.kt
- `TracingOverlay()` --calls--> `AssetTemplateImage()`  [INFERRED]
  app/src/main/java/com/sabalapps/cuteanimalstrace/ui/TracingOverlay.kt → app/src/main/java/com/sabalapps/cuteanimalstrace/ui/AssetTemplateImage.kt
- `CuteAnimalsApp()` --calls--> `rememberFavoriteSound()`  [INFERRED]
  app/src/main/java/com/sabalapps/cuteanimalstrace/ui/CuteAnimalsApp.kt → app/src/main/java/com/sabalapps/cuteanimalstrace/ui/FavoriteSound.kt

## Import Cycles
- None detected.

## Communities (39 total, 20 thin omitted)

### Community 0 - "Cute Animals: Trace & Draw"
Cohesion: 0.29
Nodes (7): Android SDK requirements, CameraX overlay requirement, Cute Animals: Trace & Draw, Kotlin and Jetpack Compose, Material 3 themes, Offline operation, Simple architecture with stable AndroidX

### Community 1 - "CuteAnimalsTheme"
Cohesion: 0.24
Nodes (5): PreviewRoutingTest, MainActivity, CuteAnimalsTheme(), Bundle, ComponentActivity

### Community 2 - "gradlew"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

### Community 19 - "SadCat.kt"
Cohesion: 0.25
Nodes (12): TracingOverlayTest, blinkOpenness(), drawCat(), drawEar(), drawEye(), drawTear(), FavoritesEmptyState(), Color (+4 more)

### Community 20 - "TraceScreen"
Cohesion: 0.23
Nodes (14): CameraPreview(), Modifier, activity(), FloatingBack(), Modifier, TipStep(), TraceChrome(), TraceScreen() (+6 more)

### Community 21 - "UserPreferencesRepository"
Cohesion: 0.07
Nodes (19): AndroidViewModel, Appearance, Dark, Light, System, Keys, UserPreferences, UserPreferencesRepository (+11 more)

### Community 22 - "CuteUi.kt"
Cohesion: 0.09
Nodes (50): androidx, Difficulty, Detailed, Easy, Medium, DrawingTemplate, TemplateCategory, BabyAnimals (+42 more)

### Community 23 - "CuteAnimalsApp.kt"
Cohesion: 0.08
Nodes (10): NavigationShellTest, AssetManager, LocalTemplateCatalog, RateSharePrompt, DrawingDestination, TopLevelDestination, Explore, Favorites (+2 more)

### Community 24 - "CameraPermissionState"
Cohesion: 0.33
Nodes (5): CameraPermissionState, Blocked, Denied, Granted, NotRequested

### Community 26 - "TracingOverlayState"
Cohesion: 0.10
Nodes (18): Animatable, cuteSliderColors(), PromptIllustration(), RateShareDialog(), StarRating(), DrawingPen(), Bitmap, Dp (+10 more)

### Community 29 - "filterDrawings"
Cohesion: 0.12
Nodes (10): ProductionTemplateCatalogTest, AssetImageState, AssetTemplateImage(), AssetManager, Bitmap, Modifier, TemplateBitmapLoader, filterDrawings() (+2 more)

### Community 31 - "Cute Animals: Trace & Draw — Production Image Generation Prompt"
Cohesion: 0.12
Nodes (16): Baby Animals — 10, Bears — 8, Bunnies — 10, Cats — 15, Cute Animals: Trace & Draw — Production Image Generation Prompt, Difficulty, Dogs — 15, Featured images (+8 more)

### Community 32 - "generate_preview_assets.py"
Cohesion: 0.17
Nodes (34): Exception, align_preview(), audit(), digest(), expand_library(), image_stats(), ink_mask(), load_library() (+26 more)

### Community 33 - "GeneratorTests"
Cohesion: 0.18
Nodes (3): drawing_bytes(), GeneratorTests, Offline tests: no API calls and no production asset writes.

### Community 37 - "DailyReminder"
Cohesion: 0.28
Nodes (5): DailyReminder, Context, ReminderWorker, CoroutineWorker, Result

### Community 38 - "RateShare.kt"
Cohesion: 0.33
Nodes (7): FavoriteSound, rememberFavoriteSound(), Context, launchInAppReview(), openStoreListing(), shareApp(), storeUrl()

## Knowledge Gaps
- **55 isolated node(s):** `Easy`, `Medium`, `Detailed`, `Cats`, `Dogs` (+50 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **20 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `CuteAnimalsApp()` connect `UserPreferencesRepository` to `CuteAnimalsTheme`, `RateShare.kt`, `TraceScreen`, `CuteUi.kt`, `CuteAnimalsApp.kt`, `TracingOverlayState`?**
  _High betweenness centrality (0.119) - this node is a cross-community bridge._
- **Why does `TraceScreen()` connect `TraceScreen` to `TracingOverlayState`, `UserPreferencesRepository`, `CuteUi.kt`?**
  _High betweenness centrality (0.065) - this node is a cross-community bridge._
- **Why does `DrawingTemplate` connect `CuteUi.kt` to `TracingOverlayState`, `TraceScreen`, `filterDrawings`, `CuteAnimalsApp.kt`?**
  _High betweenness centrality (0.062) - this node is a cross-community bridge._
- **Are the 4 inferred relationships involving `UserPreferencesRepository` (e.g. with `UserPreferencesViewModel` and `.defaultOpacityIsBoundedAndRejectsInvalidValues()`) actually correct?**
  _`UserPreferencesRepository` has 4 INFERRED edges - model-reasoned connections that need verification._
- **Are the 17 inferred relationships involving `CuteAnimalsApp()` (e.g. with `.recordPromptDismissed()` and `.recordPromptShown()`) actually correct?**
  _`CuteAnimalsApp()` has 17 INFERRED edges - model-reasoned connections that need verification._
- **Are the 10 inferred relationships involving `TracingOverlayState` (e.g. with `.tracingLoadsOriginalWithoutDependingOnPreview()` and `TracingOverlayTest`) actually correct?**
  _`TracingOverlayState` has 10 INFERRED edges - model-reasoned connections that need verification._
- **What connects `Easy`, `Medium`, `Detailed` to the rest of the system?**
  _55 weakly-connected nodes found - possible documentation gaps or missing edges._