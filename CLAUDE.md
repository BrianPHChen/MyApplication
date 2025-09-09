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

## Unit Testing Strategy

This project follows a layered testing approach that aligns with Clean Architecture principles:

### Testing Philosophy
- **Separation of Concerns**: Each layer tests different responsibilities
- **Repository Layer**: Tests business logic implementation and data operations
- **ViewModel Layer**: Tests presentation logic and integration with domain layer

### Repository Testing (`TaskRepositoryImplTest`)
**Purpose**: Test business logic and data operations using real implementations

**What to Test**:
- Business logic implementation (add, update, delete, toggle operations)
- Data transformations and state management
- StateFlow behavior and emissions
- Edge cases and error scenarios
- Data integrity across multiple operations

**Testing Tools**:
- **Real Implementation**: `TaskRepositoryImpl()` - test actual business logic
- **Turbine**: `.test { awaitItem() }` - for Flow testing
- **runTest**: Coroutine testing environment
- **Assertions**: Verify state changes and data correctness

**Example Pattern**:
```kotlin
@Test
fun `addTask should add new task to list`() = runTest {
    // Given
    val newTask = TestData.createTask()
    
    // When
    repository.addTask(newTask)
    
    // Then
    repository.getAllTasks().test {
        val tasks = awaitItem()
        assertEquals(expectedSize, tasks.size)
        assertEquals(newTask, tasks.find { it.id == newTask.id })
        cancelAndIgnoreRemainingEvents()
    }
}
```

### ViewModel Testing (`TaskListViewModelTest`)
**Purpose**: Test presentation layer integration and method delegation

**What to Test**:
- **Method Delegation**: Verify ViewModel correctly calls Repository methods
- **Parameter Passing**: Ensure correct parameters are passed to Repository
- **Coroutine Integration**: Verify suspend function calls work correctly
- **State Initialization**: Confirm StateFlow setup from Repository

**What NOT to Test**:
- Business logic implementation (that's Repository's responsibility)
- State transformations (Repository handles this)
- Data correctness (Repository tests cover this)

**Testing Tools**:
- **MockK**: `mockk(relaxed = true)` - for Repository mocking
- **coEvery/coVerify**: Verify suspend function calls
- **InstantTaskExecutorRule**: Handle Android Architecture Components
- **StandardTestDispatcher**: Manage coroutine testing

**Example Pattern**:
```kotlin
@Test
fun `addTask應該調用Repository的addTask方法`() = runTest {
    // Given
    val testTask = TestData.createTask()
    
    // When
    viewModel.addTask(testTask)
    advanceUntilIdle()
    
    // Then - 驗證Repository方法被調用
    coVerify(exactly = 1) { mockRepository.addTask(testTask) }
}
```

### Testing Dependencies
```kotlin
// Unit Testing
testImplementation("junit:junit:4.13.2")
testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
testImplementation("androidx.arch.core:core-testing:2.2.0")
testImplementation("io.mockk:mockk:1.13.8")
testImplementation("io.mockk:mockk-android:1.13.8")
testImplementation("app.cash.turbine:turbine:1.0.0")
testImplementation("org.jetbrains.kotlin:kotlin-test:1.9.10")
```

### Key Testing Utilities

#### Test Data (`testutil/TestData.kt`)
- Centralized test data creation
- Consistent test scenarios across test classes
- Factory methods for creating test entities

#### Fake Repository (`testutil/FakeTaskRepository.kt`)
- **Use Case**: Integration tests or complex business logic scenarios
- **Not Recommended For**: ViewModel testing (use Mocks instead)

### Testing Best Practices
1. **Layer Separation**: Don't test business logic in ViewModel tests
2. **Mock Strategy**: Use Mocks for ViewModel, Real implementations for Repository
3. **Flow Testing**: Always use `cancelAndIgnoreRemainingEvents()` after `awaitItem()`
4. **Coroutine Testing**: Use `advanceUntilIdle()` for suspend function completion
5. **Test Naming**: Use descriptive names in Chinese/English that explain expected behavior