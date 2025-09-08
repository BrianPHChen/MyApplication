# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

- **Build project**: `./gradlew build`
- **Run tests**: `./gradlew test`
- **Run instrumented tests**: `./gradlew connectedAndroidTest`
- **Clean build**: `./gradlew clean`
- **Assemble debug APK**: `./gradlew assembleDebug`
- **Install debug APK**: `./gradlew installDebug`

## Project Architecture

This is a modern Android application built with Clean Architecture and current best practices:

### Technology Stack
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (fully Compose-based, no Fragments)
- **Dependency Injection**: Hilt/Dagger
- **Architecture**: Clean Architecture with MVVM presentation pattern
- **State Management**: StateFlow with Compose state management
- **Build System**: Gradle with Version Catalogs (libs.versions.toml)

### Clean Architecture Layers

#### Domain Layer (`domain/`)
- **Models**: `domain/model/Task.kt` - Domain entities
- **Repositories**: `domain/repository/TaskRepository.kt` - Repository contracts
- **Pure Kotlin**: No Android dependencies

#### Data Layer (`data/`)
- **Repository Implementations**: `data/repository/TaskRepositoryImpl.kt`
- **Data Sources**: Local data management
- **Dependency**: Implements domain contracts

#### Presentation Layer (`presentation/`)
- **Screens**: `presentation/tasks/list/TaskListScreen.kt` - Composable screens
- **ViewModels**: `presentation/tasks/list/TaskListViewModel.kt` - @HiltViewModel
- **Components**: `presentation/tasks/list/components/` - Reusable UI components

#### Dependency Injection (`di/`)
- **Modules**: `di/RepositoryModule.kt` - Hilt dependency binding

#### UI Theme (`ui/`)
- **Theming**: Compose Material Design 3 theming

### Package Structure
```
com.example.myapplication/
├── TaskManagerApplication.kt (@HiltAndroidApp)
├── MainActivity.kt (@AndroidEntryPoint)
├── di/
│   └── RepositoryModule.kt
├── domain/
│   ├── model/
│   │   └── Task.kt
│   └── repository/
│       └── TaskRepository.kt
├── data/
│   └── repository/
│       └── TaskRepositoryImpl.kt
├── presentation/
│   └── tasks/list/
│       ├── TaskListScreen.kt
│       ├── TaskListViewModel.kt
│       └── components/
│           └── TaskItem.kt
└── ui/theme/
    ├── Color.kt
    ├── Theme.kt
    └── Type.kt
```

### Key Configuration
- **Min SDK**: 24
- **Target SDK**: 36
- **Compile SDK**: 36
- **Java Version**: 11
- **Application Class**: `TaskManagerApplication` (Hilt-annotated with `android:name` in manifest)

### Architecture Principles
- **Single Activity Architecture**: MainActivity with Compose navigation
- **Unidirectional Data Flow**: State flows down, events flow up
- **Repository Pattern**: Domain layer defines contracts, data layer implements
- **Dependency Inversion**: High-level modules don't depend on low-level modules
- **Hilt Dependency Injection**: `@HiltAndroidApp` → `@AndroidEntryPoint` → `@HiltViewModel` → `@Inject`

### Dependency Injection Flow
```
TaskManagerApplication (@HiltAndroidApp)
    ↓
MainActivity (@AndroidEntryPoint)
    ↓
TaskListScreen (hiltViewModel())
    ↓
TaskListViewModel (@HiltViewModel + @Inject constructor)
    ↓
TaskRepository (interface → implementation via @Binds)
```

### Development Setup
- **Gradle Version Catalogs**: Centralized dependency management
- **KAPT**: Enabled for Hilt annotation processing
- **Compose Compiler**: Integrated via Kotlin plugin
- **No Fragments**: Pure Compose UI architecture
- **StateFlow**: Reactive state management with Compose integration