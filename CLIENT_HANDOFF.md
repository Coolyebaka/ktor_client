# Client Handoff

Keep backend URL as a client setting, for example:

```kotlin
const val API_BASE_URL = "http://localhost:8080"
```

Backend base URL on the server computer:

```text
http://localhost:8080
```

If backend `PORT` changes, update `API_BASE_URL`.

## Auth Flow

1. Register with `POST /auth/register` or login with `POST /auth/login`.
2. Save `AuthResponse.token` on the client.
3. Send the token to protected endpoints:

```http
Authorization: Bearer <token>
```

Only `/notes` endpoints are protected right now. `/rules` endpoints are public read-only.

Emails are normalized by the backend with trim/lowercase. Password must contain at least 6
characters. Wrong email or password returns `401` with `ApiError`.

## DTOs For Android

Use separate nullable fields for the user-selected note date and time:

- `date: LocalDate?` serializes as `"2026-05-28"`;
- `time: LocalTime?` serializes as `"12:00:00"`.

With `kotlinx.serialization`, user-entered empty values can be sent as `null` or omitted in
create/update requests. Do not send empty strings. `createdAt` and `updatedAt` are technical
timestamps and must always be sent in create/update requests.

```kotlin
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String,
)

@Serializable
data class AuthResponse(
    val token: String,
    val user: UserResponse,
)

@Serializable
data class UserResponse(
    val id: String,
    val email: String,
)

@Serializable
data class CreateNoteRequest(
    val date: LocalDate? = null,
    val time: LocalTime? = null,
    val location: String? = null,
    val target: String? = null,
    val text: String? = null,
    val createdAt: Instant,
    val updatedAt: Instant,
)

@Serializable
data class UpdateNoteRequest(
    val date: LocalDate? = null,
    val time: LocalTime? = null,
    val location: String? = null,
    val target: String? = null,
    val text: String? = null,
    val createdAt: Instant,
    val updatedAt: Instant,
)

@Serializable
data class NoteResponse(
    val id: String,
    val date: LocalDate?,
    val time: LocalTime?,
    val location: String?,
    val target: String?,
    val text: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
)

@Serializable
data class RuleResponse(
    val id: String,
    val title: String,
    val target: String,
    val season: String,
    val region: String,
    val text: String,
)

@Serializable
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
- `PUT /notes/{id}` replaces all editable fields of current user's note.
- `DELETE /notes/{id}` deletes only current user's note.

All user-entered note fields are nullable: `date`, `time`, `location`, `target`, and `text`.
The backend trims text fields and stores `null` if a text field is omitted or blank.
The client must send `createdAt` and `updatedAt` as single `Instant` fields in create/update
requests. If either one is omitted or `null`, the backend returns `400 VALIDATION_ERROR` and does
not save the note.

Search is simple text matching by `location`, `target`, `text`, `date`, and `time` when present.

`createdAt` and `updatedAt` stay single timestamp fields; they are not split into date/time.

## Rules

Rules are a read-only book.

- `GET /rules?query=` lists and searches rules.
- `GET /rules/{id}` opens a rule.

