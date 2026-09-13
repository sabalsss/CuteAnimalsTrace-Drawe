# Graph Report - CuteAnimalsTraceDraw  (2026-09-13)

## Corpus Check
- Corpus is ~2,981 words - fits in a single context window. You may not need a graph.

## Summary
- 37 nodes · 22 edges · 19 communities (6 shown, 13 thin omitted)
- Extraction: 100% EXTRACTED · 0% INFERRED · 0% AMBIGUOUS
- Token cost: unavailable (subagent token usage was not exposed).

## Community Hubs (Navigation)
- Documented App Requirements
- Activity Startup
- Gradle Wrapper
- Android Instrumentation Tests
- Local Unit Tests
- Development Workflow
- HDPI Square Icon
- HDPI Round Icon
- MDPI Square Icon
- MDPI Round Icon
- XHDPI Square Icon
- XHDPI Round Icon
- XXHDPI Square Icon
- XXHDPI Round Icon
- XXXHDPI Square Icon
- XXXHDPI Round Icon

## God Nodes (most connected - your core abstractions)
1. `Cute Animals: Trace & Draw` - 6 edges
2. `MainActivity` - 3 edges
3. `ExampleInstrumentedTest` - 2 edges
4. `ExampleUnitTest` - 2 edges
5. `Kotlin and Jetpack Compose` - 1 edges
6. `Material 3 themes` - 1 edges
7. `CameraX overlay requirement` - 1 edges
8. `Offline operation` - 1 edges
9. `Android SDK requirements` - 1 edges
10. `Simple architecture with stable AndroidX` - 1 edges

## Surprising Connections (you probably didn't know these)
- None detected - all connections are within the same source files.

## Import Cycles
- None detected.

## Communities (19 total, 13 thin omitted)

### Community 0 - "Documented App Requirements"
Cohesion: 0.29
Nodes (7): Android SDK requirements, CameraX overlay requirement, Cute Animals: Trace & Draw, Kotlin and Jetpack Compose, Material 3 themes, Offline operation, Simple architecture with stable AndroidX

### Community 1 - "Activity Startup"
Cohesion: 0.60
Nodes (3): MainActivity, AppCompatActivity, Bundle

### Community 2 - "Gradle Wrapper"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

## Knowledge Gaps
- **15 isolated node(s):** `Kotlin and Jetpack Compose`, `Material 3 themes`, `CameraX overlay requirement`, `Android SDK requirements`, `Gradle build and test validation` (+10 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **13 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **What connects `Kotlin and Jetpack Compose`, `Material 3 themes`, `CameraX overlay requirement` to the rest of the system?**
  _15 weakly-connected nodes found - possible documentation gaps or missing edges._
## Extraction Limitations

- Health warning: eight dangling import edges reference external AndroidX/JUnit symbols without corresponding nodes. They are omitted from the exported graph; dependency coverage is incomplete. No missing endpoints, self-loops, or collapsed edges were reported.
- Token usage: subagent usage counts were unavailable. The generated numeric zeros are placeholders, not measured zero usage; monetary cost is unknown.
- AGENTS.md describes intended Compose/CameraX requirements. Existing code remains an AppCompat starter; documentation nodes do not establish implemented features.
- Detection covered 18 supported files; XML resources and version-catalog configuration are not fully represented.
