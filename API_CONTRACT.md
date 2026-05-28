# API Contract

Base URL for local development:

```text
http://localhost:8080
```

Android emulator base URL:

```text
http://10.0.2.2:8080
```

## Authorization

Protected endpoints require JWT Bearer auth:

```http
Authorization: Bearer <token>
```

The token is returned by `/auth/register` and `/auth/login`. JWT contains `userId`.

## Error Format

All errors use the same JSON format:

```json
{
  "code": "VALIDATION_ERROR",
  "message": "Invalid email"
}
```

Common codes:

- `VALIDATION_ERROR`
- `UNAUTHORIZED`
- `NOT_FOUND`
- `CONFLICT`
- `INVALID_REQUEST_BODY`
- `BAD_REQUEST`
- `INTERNAL_ERROR`

## Health

### GET /health

Response:

```json
{
  "status": "ok"
}
```

## Auth

### POST /auth/register

Request:

```json
{
  "email": "hunter@example.com",
  "password": "secret-password"
}
```

Response:

```json
{
  "token": "jwt-token",
  "user": {
    "id": "user-id",
    "email": "hunter@example.com"
  }
}
```

Duplicate email returns `409 CONFLICT`.

### POST /auth/login

Request:

```json
{
  "email": "hunter@example.com",
  "password": "secret-password"
}
```

Response:

```json
{
  "token": "jwt-token",
  "user": {
    "id": "user-id",
    "email": "hunter@example.com"
  }
}
```

Wrong email or password returns `401 UNAUTHORIZED`.

## Notes

All `/notes` endpoints require `Authorization: Bearer <token>`.

Date-time fields use ISO-8601 strings.

### GET /notes?query=

Returns only notes owned by the authorized user. Optional `query` searches by `location`, `target`, `text`, and ISO string representation of `dateTime`.

Response:

```json
[
  {
    "id": "note-id",
    "dateTime": "2026-05-28T12:00:00Z",
    "location": "Northern forest",
    "target": "Boar",
    "text": "Saw fresh tracks near the river.",
    "createdAt": "2026-05-28T13:00:00Z",
    "updatedAt": "2026-05-28T13:00:00Z"
  }
]
```

### POST /notes

Request:

```json
{
  "dateTime": "2026-05-28T12:00:00Z",
  "location": "Northern forest",
  "target": "Boar",
  "text": "Saw fresh tracks near the river."
}
```

Response `201 CREATED`:

```json
{
  "id": "note-id",
  "dateTime": "2026-05-28T12:00:00Z",
  "location": "Northern forest",
  "target": "Boar",
  "text": "Saw fresh tracks near the river.",
  "createdAt": "2026-05-28T13:00:00Z",
  "updatedAt": "2026-05-28T13:00:00Z"
}
```

### GET /notes/{id}

Returns the note only if it belongs to the authorized user.

Response:

```json
{
  "id": "note-id",
  "dateTime": "2026-05-28T12:00:00Z",
  "location": "Northern forest",
  "target": "Boar",
  "text": "Saw fresh tracks near the river.",
  "createdAt": "2026-05-28T13:00:00Z",
  "updatedAt": "2026-05-28T13:00:00Z"
}
```

If the note does not exist or belongs to another user, response is `404 NOT_FOUND`.

### PUT /notes/{id}

Request:

```json
{
  "dateTime": "2026-05-29T09:30:00Z",
  "location": "Eastern field",
  "target": "Duck",
  "text": "Updated observation."
}
```

Response:

```json
{
  "id": "note-id",
  "dateTime": "2026-05-29T09:30:00Z",
  "location": "Eastern field",
  "target": "Duck",
  "text": "Updated observation.",
  "createdAt": "2026-05-28T13:00:00Z",
  "updatedAt": "2026-05-29T10:00:00Z"
}
```

If the note does not exist or belongs to another user, response is `404 NOT_FOUND`.

### DELETE /notes/{id}

Deletes the note only if it belongs to the authorized user.

Response:

```http
204 No Content
```

If the note does not exist or belongs to another user, response is `404 NOT_FOUND`.

## Rules

Rules are read-only. No auth header is required for current rules endpoints.

### GET /rules?query=

Optional `query` searches by `title`, `target`, `season`, `region`, and `text`.

Response:

```json
[
  {
    "id": "autumn-duck",
    "title": "Осенняя охота на утку",
    "target": "Утка",
    "season": "Лето-осень",
    "region": "Московская область",
    "text": "Сроки и нормы добычи определяются региональными правилами охоты на текущий сезон."
  }
]
```

### GET /rules/{id}

Response:

```json
{
  "id": "autumn-duck",
  "title": "Осенняя охота на утку",
  "target": "Утка",
  "season": "Лето-осень",
  "region": "Московская область",
  "text": "Сроки и нормы добычи определяются региональными правилами охоты на текущий сезон."
}
```

Missing rule returns `404 NOT_FOUND`.
