# Coding Exercise: Folder Consistency Checker

## Context

A mock REST API serves user and folder data through an NGINX container. The API exposes three endpoints:

- `GET /users` — returns a JSON array of user email addresses
- `GET /users/{email}/folders` — returns the folders for a given user
- `GET /folders` — returns all folders across all users

The data served by this API **contains deliberate inconsistencies** between `/folders` and the per-user `/users/{email}/folders` responses.

Your job is to build a service that detects and reports these inconsistencies.

## Getting started

Start the mock API:

```bash
docker compose up -d
```

The API will be available at `http://localhost:8080`. See `README.md` for endpoint details.

## Your task

Build a **Java reactive service** that:

1. Fetches data from the mock API (`/users`, `/users/{email}/folders`, `/folders`)
2. Compares the per-user folder data against the global folder list
3. Exposes a single endpoint:

```
GET /inconsistencies
```

This endpoint must return a JSON response describing **all inconsistencies** found between the two data sources. 

### What is an inconsistency?

Any discrepancy between what `/folders` says and what `/users/{email}/folders` says. Think about the different types of inconsistencies that can arise — this is part of the exercise.

### Response format

You must **design your own response format**. It should be clear, structured, and cover all types of inconsistencies you identify. Be prepared to justify your choices.

## Requirements

- **Language:** Java
- **Reactive & non-blocking:** use a reactive stack (e.g. Spring WebFlux, Mutiny, or similar). The service must not use blocking I/O to call the mock API.
- **Clean code:** readable, well-structured, properly organized.
- **Documentation:** provide a short README with:
  - How to build and run your service
  - A description of your `/inconsistencies` API (response format, meaning of fields)
- **Test suite**: while it do not need to be a complete test suite we would like to be able to evaluate the testing strategy you would follow.
- **No hardcoded data:** your service must dynamically fetch from the mock API, not embed the dataset.

## Evaluation

Your solution will be evaluated on a **different dataset** than the one provided. The mock API data will be swapped — your service must handle any data, not just the sample.

We will look at:

- Correctness: does it find all inconsistencies?
- Response format: is it clear and complete?
- Code quality: is it clean, idiomatic, and well-organized?
- Reactivity: is the implementation truly non-blocking?
- Documentation: can we build, run, and understand the API without reading the source code?
