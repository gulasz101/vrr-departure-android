# AGENTS.md - VRR Departure Board Android

This file contains guidelines and commands for agentic coding agents working on the VRR Departure Board Android project.

## Build Commands

### Core Build Commands
```bash
./gradlew assembleDebug          # Build debug APK
./gradlew assembleRelease        # Build release APK (requires signing)
./gradlew installDebug           # Install debug APK on connected device
./gradlew clean                  # Clean build artifacts
./gradlew build                  # Build all variants
```

### Testing Commands
```bash
./gradlew test                   # Run all unit tests
./gradlew connectedAndroidTest   # Run instrumented tests on device
./gradlew testDebug              # Run tests for debug variant
./gradlew testRelease            # Run tests for release variant
```

### Code Quality Commands
```bash
./gradlew ktlintCheck            # Check Kotlin code style
./gradlew ktlintFormat           # Auto-format Kotlin code
./gradlew detekt                 # Run static analysis
./gradlew lintDebug              # Run Android lint for debug
./gradlew lintRelease            # Run Android lint for release
```

### Single Test Execution
```bash
./gradlew test --tests "com.vrr.departureboard.ExampleTest"  # Run specific test class
./gradlew test --tests "*specificTest"                       # Run specific test method
./gradlew connectedAndroidTest --tests "com.vrr.departureboard.ExampleTest"  # Run specific instrumented test
```

## Project Architecture

### Technology Stack
- **Language**: Kotlin 1.9.21
- **UI Framework**: Jetpack Compose with Material3
- **Architecture**: MVVM with Clean Architecture
- **Build System**: Gradle with Kotlin DSL
- **Networking**: Ktor Client 2.3.7
- **DI**: Hilt 2.50 with KSP
- **Async**: Kotlin Coroutines & Flow
- **Local Storage**: DataStore Preferences
- **JSON**: Kotlinx Serialization

### Package Structure
```
com.vrr.departureboard/
├── data/
│   ├── api/           # Ktor API client & DTOs
│   ├── local/         # DataStore for settings
│   └── repository/    # Repository implementations
├── di/                # Hilt dependency injection modules
├── domain/
│   └── model/         # Domain models
└── ui/
    ├── components/    # Reusable UI components
    ├── screens/       # App screens
    └── theme/         # Material3 theme
```

## Code Style Guidelines

### Kotlin Code Style
- Follow official Kotlin code style (configured in `gradle.properties`)
- Use 4 space indentation (no tabs)
- Maximum line length: 120 characters

### Naming Conventions
- **Classes**: PascalCase (e.g., `DepartureViewModel`, `StopSection`)
- **Functions**: camelCase (e.g., `refreshAllStops`, `selectStop`)
- **Variables**: camelCase
- **Constants**: UPPER_SNAKE_CASE (e.g., `PRIMARY_COLOR`)
- **Compose Functions**: PascalCase for composables
- **Package Names**: lowercase with dots (e.g., `data.api.dto`)

### Import Organization
```kotlin
// 1. Android/AndroidX imports
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*

// 2. Kotlin standard library
import kotlinx.coroutines.flow.*

// 3. Third-party libraries
import io.ktor.client.*
import dagger.hilt.android.lifecycle.HiltViewModel

// 4. Project imports
import com.vrr.departureboard.data.repository.DepartureRepository
import com.vrr.departureboard.domain.model.Departure
```

### Function and Variable Declarations
- Use explicit return types for public functions
- Prefer `val` over `var` for immutability
- Use type inference for local variables when clear
- Declare nullable types explicitly with `?`

```kotlin
// Good
fun getDepartures(): Flow<List<Departure>> = repository.getDepartures()
private val httpClient = HttpClient()

// Avoid
fun getDepartures() = repository.getDepartures()
var httpClient = HttpClient()
```

## Compose Guidelines

### Composable Functions
- Use PascalCase naming
- Add `@Composable` annotation
- Prefer stateless composables
- Pass state as parameters when possible

```kotlin
@Composable
fun DepartureList(
    departures: List<Departure>,
    onDepartureClick: (Departure) -> Unit,
    modifier: Modifier = Modifier
) {
    // Implementation
}
```

### State Management
- Use `remember` for local state
- Use `mutableStateOf` for reactive state
- Prefer `ViewModel` for screen-level state
- Use `collectAsState()` for Flow/StateFlow

```kotlin
@Composable
fun DepartureScreen(
    viewModel: DepartureViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // UI implementation
}
```

## Dependency Injection

### Hilt Usage
- Use `@HiltViewModel` for ViewModels
- Use `@Inject` for constructor injection
- Create modules in `di/` package
- Use `@Provides` for external dependencies

```kotlin
@HiltViewModel
class DepartureViewModel @Inject constructor(
    private val repository: DepartureRepository
) : ViewModel() {
    // Implementation
}
```

## Error Handling

### Network Errors
- Use `Result<T>` type for API calls
- Handle network errors gracefully
- Show user-friendly error messages
- Implement retry mechanisms

```kotlin
suspend fun fetchDepartures(): Result<List<Departure>> {
    return try {
        val departures = apiService.getDepartures()
        Result.success(departures)
    } catch (e: IOException) {
        Result.failure(NetworkError("Failed to connect", e))
    }
}
```

### UI Error States
- Show error messages in UI
- Provide retry actions
- Use loading states appropriately
- Handle empty states

## Testing Guidelines

### Unit Tests
- Test ViewModels and Repositories
- Use JUnit 4 and MockK
- Test coroutines with `runTest`
- Mock external dependencies

### Compose Tests
- Use `composeTestRule`
- Test user interactions
- Verify UI state changes
- Test navigation flows

```kotlin
@Test
fun departureList_displaysCorrectly() {
    composeTestRule.setContent {
        DepartureList(departures = sampleDepartures)
    }
    
    composeTestRule
        .onNodeWithText("Sample Departure")
        .assertIsDisplayed()
}
```

## API Integration

### VRR EFA API Specifics
- Use custom Content-Type for `text/html` JSON responses
- Handle flexible response structures
- Implement proper error handling
- Use Ktor client with logging

```kotlin
private val httpClient = HttpClient {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            isLenient = true
        })
    }
    install(Logging) {
        logger = Logger.DEFAULT
        level = LogLevel.INFO
    }
}
```

## Performance Guidelines

### Coroutines
- Use `viewModelScope` for ViewModels
- Cancel coroutines properly in `onCleared()`
- Use `flow` for data streams
- Implement proper exception handling

### Compose Performance
- Use `remember` for expensive calculations
- Prefer `LazyColumn` for long lists
- Avoid recomposition with stable keys
- Use `derivedStateOf` for computed state

## Security Considerations

- Never log sensitive data (API keys, tokens)
- Use secure storage for credentials
- Validate API responses
- Implement proper certificate pinning if needed

## Git Workflow

### Commit Message Style
- Use present tense ("Add feature" not "Added feature")
- Keep messages under 72 characters
- Include issue numbers when applicable
- Use conventional commit format when possible

### Branch Naming
- `feature/description` for new features
- `bugfix/description` for bug fixes
- `hotfix/description` for urgent fixes
- `refactor/description` for code improvements

## Release Process

1. Update version in `build.gradle.kts`
2. Update `CHANGELOG.md`
3. Create release tag
4. GitHub Actions will build and release automatically
5. Test release APK before publishing

## Environment Setup

### Required Tools
- Android Studio Hedgehog or later
- JDK 17
- Android SDK 34
- Git

### IDE Configuration
- Enable Kotlin code style
- Configure Ktor client imports
- Set up Hilt annotation processing
- Enable Compose compiler metrics if needed