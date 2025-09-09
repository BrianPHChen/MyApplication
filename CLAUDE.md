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

This project follows a Clean Architecture layered testing approach with Room database integration:

### Testing Architecture Overview
```
┌─────────────────────────────────────┐
│ Presentation Layer (UI Logic)       │
│ - TaskListViewModel                 │
│ └─ Mock Repository 驗證方法調用      │
├─────────────────────────────────────┤
│ Domain Layer (Business Rules)       │
│ - Task Model                        │
│ └─ 簡單 Data Class，通常不需測試     │
├─────────────────────────────────────┤
│ Data Layer (Data Access)            │
│ - TaskRepositoryImpl                │
│ └─ Mock DAO 驗證資料轉換和方法調用    │
├─────────────────────────────────────┤
│ Local Data (Database)               │
│ - TaskDao                           │
│ └─ Room In-Memory DB 測試真實查詢    │
└─────────────────────────────────────┘
```

### Testing Strategy by Layer

### 1. DAO Layer Testing (`TaskDaoTest`)
**Purpose**: Test real database operations and SQL queries

**Testing Approach**: Integration Testing with Room In-Memory Database

**What to Test**:
- Real CRUD operations against SQLite database
- SQL query correctness and performance
- Flow emission behavior from database changes
- Data integrity and constraints
- Complex query scenarios

**Testing Tools**:
- **Room In-Memory Database**: Real database instance for testing
- **Turbine**: `.test { awaitItem() }` - for Flow testing
- **AndroidJUnit4**: Android test runner
- **runTest**: Coroutine testing environment

**Example Pattern**:
```kotlin
@RunWith(AndroidJUnit4::class)
class TaskDaoTest {
    
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()
    
    private lateinit var database: TaskDatabase
    private lateinit var taskDao: TaskDao
    
    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, TaskDatabase::class.java).build()
        taskDao = database.taskDao()
    }
    
    @Test
    fun insertTask_and_getAllTasks() = runTest {
        // Test real database operations
    }
}
```

### 2. Repository Layer Testing (`TaskRepositoryImplTest`)
**Purpose**: Test data layer integration and Entity ↔ Domain transformations

**Testing Approach**: Unit Testing with Mocked DAO

**What to Test**:
- Entity to Domain Model mapping (`toDomain()`)
- Domain to Entity Model mapping (`toEntity()`) 
- DAO method delegation and parameter passing
- Flow transformation from Entity to Domain
- Error handling and edge cases

**Testing Tools**:
- **MockK**: `mockk(relaxed = true)` - for DAO mocking
- **coEvery/coVerify**: Mock suspend function behavior
- **Turbine**: Flow testing with transformations
- **runTest**: Coroutine testing environment

**Example Pattern**:
```kotlin
class TaskRepositoryImplTest {
    
    private val mockTaskDao = mockk<TaskDao>(relaxed = true)
    private lateinit var repository: TaskRepositoryImpl
    
    @Before
    fun setUp() {
        repository = TaskRepositoryImpl(mockTaskDao)
    }
    
    @Test
    fun `addTask should call DAO insertTask with correct entity`() = runTest {
        // Test method delegation and data transformation
        val domainTask = TestData.createTask()
        
        repository.addTask(domainTask)
        
        coVerify(exactly = 1) { 
            mockTaskDao.insertTask(domainTask.toEntity()) 
        }
    }
}
```

### 3. ViewModel Layer Testing (`TaskListViewModelTest`)
**Purpose**: Test presentation layer integration and method delegation

**What to Test**:
- **Method Delegation**: Verify ViewModel correctly calls Repository methods
- **Parameter Passing**: Ensure correct parameters are passed to Repository
- **StateFlow Setup**: Confirm StateFlow initialization from Repository
- **Coroutine Integration**: Verify suspend function calls work correctly

**What NOT to Test**:
- Business logic implementation (that's Repository's responsibility)
- Data transformations (Repository handles this)
- Database operations (DAO tests cover this)

**Testing Tools**:
- **MockK**: `mockk(relaxed = true)` - for Repository mocking
- **coEvery/coVerify**: Verify suspend function calls
- **InstantTaskExecutorRule**: Handle Android Architecture Components
- **StandardTestDispatcher**: Manage coroutine testing

**Example Pattern**:
```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class TaskListViewModelTest {
    
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()
    
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mockRepository: TaskRepository
    private lateinit var viewModel: TaskListViewModel
    
    @Test
    fun `addTask應該調用Repository的addTask方法`() = runTest {
        // Test UI logic and method delegation only
    }
}
```

### Testing Dependencies
```kotlin
// Unit Testing
testImplementation(libs.junit)
testImplementation(libs.kotlin.test)
testImplementation(libs.kotlinx.coroutines.test)
testImplementation(libs.androidx.core.testing)
testImplementation(libs.mockk)
testImplementation(libs.mockk.android)
testImplementation(libs.turbine)
testImplementation(libs.androidx.room.testing)

// Android Instrumentation Testing (for DAO tests)
androidTestImplementation(libs.androidx.junit)
androidTestImplementation(libs.androidx.espresso.core)
```

### Testing File Structure
```
src/
├── test/                           # Unit Tests
│   ├── java/com/example/myapplication/
│   │   ├── data/repository/
│   │   │   └── TaskRepositoryImplTest.kt    # Mock DAO tests
│   │   ├── presentation/tasks/list/
│   │   │   └── TaskListViewModelTest.kt     # Mock Repository tests
│   │   └── testutil/
│   │       ├── TestData.kt                  # Test data factory
│   │       └── TestDatabase.kt              # Test database utilities
│   └── 
├── androidTest/                    # Integration Tests  
│   └── java/com/example/myapplication/
│       └── data/local/dao/
│           └── TaskDaoTest.kt               # Room In-Memory DB tests
└── 
```

### Key Testing Utilities

#### Test Data Factory (`testutil/TestData.kt`)
- Centralized test data creation for both Entity and Domain models
- Consistent test scenarios across all test layers
- Factory methods for creating test data

#### Test Database Factory (`testutil/TestDatabase.kt`)
- Reusable In-Memory database setup for DAO tests
- Consistent database configuration across tests

#### Fake Repository (`testutil/FakeTaskRepository.kt`)
- **Deprecated**: No longer recommended with Room integration
- Use Mock DAO in Repository tests instead

### Testing Best Practices

1. **Layer Separation**: Each layer tests only its own responsibilities
2. **Mock Strategy**: 
   - DAO tests: Real Room In-Memory Database
   - Repository tests: Mock DAO
   - ViewModel tests: Mock Repository
3. **Flow Testing**: Always use `cancelAndIgnoreRemainingEvents()` after `awaitItem()`
4. **Coroutine Testing**: Use `advanceUntilIdle()` for suspend function completion
5. **Test Naming**: Use descriptive names that explain expected behavior
6. **Data Transformation Testing**: Verify Entity ↔ Domain mapping correctness