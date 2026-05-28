# Client Handoff

Android emulator base URL:

```text
http://10.0.2.2:8080
```

Physical device base URL should use the backend machine LAN address, for example:

```text
http://192.168.1.10:8080
```

## Auth Flow

1. Register with `POST /auth/register` or login with `POST /auth/login`.
2. Save `AuthResponse.token` on the client.
3. Send the token to protected endpoints:

```http
Authorization: Bearer <token>
```

Only `/notes` endpoints are protected right now. `/rules` endpoints are public read-only.

## DTOs For Android

Use ISO-8601 strings for date-time values on the client.

```kotlin
data class RegisterRequest(
    val email: String,
    val password: String,
)

data class LoginRequest(
    val email: String,
    val password: String,
)

data class AuthResponse(
    val token: String,
    val user: UserResponse,
)

data class UserResponse(
    val id: String,
    val email: String,
)

data class CreateNoteRequest(
    val dateTime: String,
    val location: String,
    val target: String,
    val text: String,
)

data class UpdateNoteRequest(
    val dateTime: String,
    val location: String,
    val target: String,
    val text: String,
)

data class NoteResponse(
    val id: String,
    val dateTime: String,
    val location: String,
    val target: String,
    val text: String,
    val createdAt: String,
    val updatedAt: String,
)

data class RuleResponse(
    val id: String,
    val title: String,
    val target: String,
    val season: String,
    val region: String,
    val text: String,
)

data class ApiError(
    val code: String,
    val message: String,
)
```

## Notes CRUD

All notes belong to the authorized user from JWT. The client must not send `userId`; the backend takes it from the token.

- `GET /notes?query=` returns only current user's notes.
- `POST /notes` creates a note for current user.
- `GET /notes/{id}` returns `404` if the note is absent or belongs to another user.
- `PUT /notes/{id}` updates only current user's note.
- `DELETE /notes/{id}` deletes only current user's note.

Search is simple text matching by `location`, `target`, `text`, and `dateTime` ISO string.

## Rules

Rules are a read-only справочник.

- `GET /rules?query=` lists and searches rules.
- `GET /rules/{id}` opens a rule.
- There is no admin panel and no client-side write API for rules.

The backend seeds 7 demo rules when the Firestore `rules` collection is empty.

## Current Limits

- No refresh token.
- No email confirmation.
- No password recovery.
- No roles or admin endpoints.
- No image upload.
- No maps.
- No complex date filtering.
- Firestore credentials stay on the backend only.
