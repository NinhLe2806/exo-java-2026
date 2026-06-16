# Folder Consistency Checker

This repository contains:

- a mock REST API served by NGINX in Docker
- a Java reactive service exposing `GET /inconsistencies`

The service fetches data from the mock API, compares per-user folders with the global folder list, and reports every discrepancy it finds.

## Running the mock API

```bash
docker compose up -d
```

The service is available at `http://localhost:8080`.

To stop it:

```bash
docker compose down
```

## Mock API endpoints

### GET /users

Returns the list of user email addresses.

```bash
curl http://localhost:8080/users
```

```json
["john@linagora.com", "alice@linagora.com", "..."]
```

### GET /users/{email}/folders

Returns the folders for a given user.

```bash
curl http://localhost:8080/users/john@linagora.com/folders
```

```json
[
  {"id": "9d68e13e-fa7e-476e-b4d0-a80aec399be2", "name": "Trash"},
  {"id": "ef17f006-a454-46ee-8ab8-8fa13629797c", "name": "Inbox"}
]
```

## Running the consistency checker

The checker runs on port `8081` by default and calls the mock API at `http://localhost:8080`.

On Windows:

```bash
.\mvnw.cmd spring-boot:run
```

On Linux/macOS:

```bash
sh ./mvnw spring-boot:run
```

To override the mock API URL:

```bash
.\mvnw.cmd spring-boot:run -Dspring-boot.run.arguments="--mock-api.base-url=http://localhost:8080"
```

Build and test:

```bash
.\mvnw.cmd test
```

## Consistency checker endpoint

### GET /inconsistencies

```bash
curl http://localhost:8081/inconsistencies
```

Example response:

```json
{
  "summary": {
    "total": 5,
    "missingFromGlobal": 2,
    "missingFromUserFolders": 2,
    "nameMismatches": 1,
    "duplicateEntries": 0,
    "globalFoldersForUnknownUsers": 0
  },
  "missingFromGlobal": [
    {
      "user": "john@linagora.com",
      "id": "01797ed7-56b8-4681-add1-fdac1244963a",
      "name": "Social"
    }
  ],
  "missingFromUserFolders": [
    {
      "user": "john@linagora.com",
      "id": "01797ed7-56b8-4681-add1-aaaaaaaaaaaa",
      "name": "Social"
    }
  ],
  "nameMismatches": [
    {
      "user": "john@linagora.com",
      "id": "55cc5502-7237-4e5c-b4da-4d4aebca58e0",
      "userFolderName": "Receipts",
      "globalFolderName": "Wrong name"
    }
  ],
  "duplicateEntries": [],
  "globalFoldersForUnknownUsers": []
}
```

Response fields:

- `summary`: counts for each inconsistency category plus the total number of reported items.
- `missingFromGlobal`: folders returned by `/users/{email}/folders` but not by `/folders` for the same `user + id`.
- `missingFromUserFolders`: folders returned by `/folders` for a known user but not by `/users/{email}/folders` for the same `user + id`.
- `nameMismatches`: folders present in both sources for the same `user + id`, but with different names.
- `duplicateEntries`: duplicate `user + id` records in either source.
- `globalFoldersForUnknownUsers`: folders returned by `/folders` for users not listed by `/users`.

## Implementation notes

- The checker uses Spring WebFlux and `WebClient`.
- Calls to the mock API are non-blocking.
- User folder calls are executed concurrently with a bounded concurrency of 8.
- No dataset values are hardcoded in the service.

### GET /folders

Returns all folders across all users.

```bash
curl http://localhost:8080/folders
```

```json
[
  {"id": "9d68e13e-fa7e-476e-b4d0-a80aec399be2", "user": "john@linagora.com", "name": "Trash"},
  {"id": "ef17f006-a454-46ee-8ab8-8fa13629797c", "user": "john@linagora.com", "name": "Inbox"}
]
```
