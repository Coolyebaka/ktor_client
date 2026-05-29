# API Contract

Base URL for local development:

```text
http://localhost:8080
```

Keep this value as a client setting, for example `API_BASE_URL`. If backend `PORT` changes, update `API_BASE_URL`.

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

Invalid JSON body or invalid field type returns `400` with `INVALID_REQUEST_BODY` or `BAD_REQUEST`.

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
Invalid email returns `400 VALIDATION_ERROR`. Password must contain at least 6 characters.

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
Invalid email format returns `400 VALIDATION_ERROR`.

## Notes

All `/notes` endpoints require `Authorization: Bearer <token>`.

User-selected note date and time are separate fields:

- `date`: ISO date string, for example `"2026-05-28"`;
- `time`: ISO time string, for example `"12:00:00"`.

Both fields can be `null` when the client has no selected date or time. For create and update
requests, `date` and `time` may also be omitted; the backend treats omitted fields as `null`.
Do not send empty strings for date or time.

`location`, `target`, and `text` are nullable user fields. If they are present, the backend trims
them before saving. If they are omitted, the backend stores `null`.

`createdAt` and `updatedAt` are required single ISO-8601 timestamp fields. The client sends both
fields in create and update requests. If one of them is omitted or `null`, the backend returns
`400 VALIDATION_ERROR` and does not save the note.

### GET /notes?query=

Returns only notes owned by the authorized user. Optional `query` searches by `location`, `target`, `text`, `date`, and `time` when they are present.

Response:

```json
[
  {
    "id": "note-id",
    "date": "2026-05-28",
    "time": "12:00:00",
    "location": "Northern forest",
    "target": "Boar",
    "text": "Saw fresh tracks near the river.",
    "createdAt": "2026-05-28T13:00:00Z",
    "updatedAt": "2026-05-28T13:00:00Z"
  }
]
```

If the note has no selected date or time, `date` and `time` are `null`.

### POST /notes

Request:

```json
{
  "date": "2026-05-28",
  "time": "12:00:00",
  "location": "Northern forest",
  "target": "Boar",
  "text": "Saw fresh tracks near the river.",
  "createdAt": "2026-05-28T13:00:00Z",
  "updatedAt": "2026-05-28T13:00:00Z"
}
```

Empty user fields request:

```json
{
  "date": null,
  "time": null,
  "location": null,
  "target": null,
  "text": null,
  "createdAt": "2026-05-28T13:00:00Z",
  "updatedAt": "2026-05-28T13:00:00Z"
}
```

Response `201 CREATED`:

```json
{
  "id": "note-id",
  "date": "2026-05-28",
  "time": "12:00:00",
  "location": "Northern forest",
  "target": "Boar",
  "text": "Saw fresh tracks near the river.",
  "createdAt": "2026-05-28T13:00:00Z",
  "updatedAt": "2026-05-28T13:00:00Z"
}
```

If the note has no selected date or time, `date` and `time` are `null`.
If `location`, `target`, or `text` is empty, the corresponding field is `null`.

### GET /notes/{id}

Returns the note only if it belongs to the authorized user.

Response:

```json
{
  "id": "note-id",
  "date": "2026-05-28",
  "time": "12:00:00",
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
  "date": "2026-05-29",
  "time": "09:30:00",
  "location": "Eastern field",
  "target": "Duck",
  "text": "Updated observation.",
  "createdAt": "2026-05-28T13:00:00Z",
  "updatedAt": "2026-05-29T10:00:00Z"
}
```

Use `null` to clear nullable user fields. `PUT /notes/{id}` replaces all editable fields:
`date`, `time`, `location`, `target`, `text`, `createdAt`, and `updatedAt`.
If `date`, `time`, `location`, `target`, or `text` is omitted, that field is saved as `null`.
If `createdAt` or `updatedAt` is omitted or `null`, the backend returns `400 VALIDATION_ERROR` and
does not update the note.

Response:

```json
{
  "id": "note-id",
  "date": "2026-05-29",
  "time": "09:30:00",
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
