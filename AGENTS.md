# Cute Animals: Trace & Draw

Native Android application for tracing cute animal drawings on real paper
using the phone camera.

## Technology

- Kotlin
- Jetpack Compose
- Material 3
- CameraX
- Navigation Compose
- DataStore
- Kotlin Serialization/local JSON if required
- Android Photo Picker
- Gradle Kotlin DSL

## Platform

- minSdk 26
- targetSdk 36 or newer stable requirement
- compileSdk 36 or newer stable SDK

## Product rules

The app must work offline.

Do NOT add:

- Firebase
- Supabase
- backend services
- user accounts
- network APIs
- AI APIs
- analytics
- advertisements
- subscriptions
- in-app purchases
- ARCore unless specifically requested

The core tracing system is a normal CameraX preview with a transparent,
movable image overlay. It is not 3D augmented reality.

## Engineering rules

- Prefer stable AndroidX dependencies.
- Avoid alpha/beta dependencies unless absolutely necessary.
- Keep architecture simple.
- Do not overengineer.
- Use Material 3.
- Support light and dark themes.
- Use edge-to-edge Android layouts.
- Preserve state across reasonable configuration changes.
- Keep the application responsive on different Android screen sizes.
- Do not rewrite working systems unnecessarily.
- Inspect existing code before modifying it.
- Build after meaningful changes.
- Fix compilation errors before declaring a task complete.
- Add tests where they provide useful protection.

## Workflow

For each task:

1. Inspect the relevant existing files.
2. Explain the intended implementation briefly.
3. Implement only the requested milestone.
4. Do not implement future milestones unless required.
5. Run the appropriate Gradle build/tests.
6. Report files changed, tests/build result, and anything I should manually verify.