# Hunter Diary Android Client

Курсовой проект "Дневник охотника".

## Стек

- Kotlin
- Jetpack Compose
- Navigation Compose
- Koin
- Ktor Client
- kotlinx.serialization
- DataStore
- ViewModel + StateFlow
- Gradle Kotlin DSL

## Настройка

### Base URL backend-сервера

Адрес backend-сервера задается в файле `local.properties`:

```properties
API_BASE_URL=http://localhost:8080
```

Если значение не указано, по умолчанию используется:

```text
http://localhost:8080
```

Для Android Emulator можно указать:

```properties
API_BASE_URL=http://10.0.2.2:8080
```
