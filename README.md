# struts-to-fastapi-migration

> A legacy Java (Struts 1.x / JSP / JDBC) enterprise app and its modernized rebuild (FastAPI / SQLAlchemy / React), implementing the identical purchase-order domain — built so the migration itself, not just the finished product, can be inspected and verified.

![Java](https://img.shields.io/badge/Java-8-ED8B00?style=flat-square&logo=openjdk&logoColor=white)
![Struts](https://img.shields.io/badge/Struts-1.3.10-black?style=flat-square)
![Python](https://img.shields.io/badge/Python-3.12+-3776AB?style=flat-square&logo=python&logoColor=white)
![FastAPI](https://img.shields.io/badge/FastAPI-0.139-009688?style=flat-square&logo=fastapi&logoColor=white)
![React](https://img.shields.io/badge/React-18-61DAFB?style=flat-square&logo=react&logoColor=black)
![License](https://img.shields.io/badge/License-MIT-green?style=flat-square)

---

## Why this exists

Every "legacy modernization" pitch says the same thing: 20 years of
enterprise Java experience, ready to migrate your monolith to a modern
stack. Almost none of them show it. This repo is the demonstration: a real,
running Struts/JSP/JDBC application (the kind of internal enterprise
system this architecture was common for between roughly 2003 and 2010) and
a from-scratch FastAPI/React rebuild of the *exact same business logic*,
with a test that proves the two produce identical output for identical
input.

**Start with [`MIGRATION.md`](MIGRATION.md)** — that's the actual
deliverable here: a layer-by-layer map of what changed, what didn't, why,
and how this would be sequenced as a real client engagement rather than a
big-bang rewrite.

## What's in each half

### `legacy-java/` — the "before"

Purchase-order management in the architectural style of a mid-2000s J2EE
app: Struts 1.3.10 front controller, JSP views, a service layer modeled on
EJB session-bean separation of concerns, DAOs over raw JDBC, against an
embedded H2 database (standing in for the Oracle this pattern was built
against in production — see `legacy-java/pom.xml`). Runs on Java 8 under an
embedded Tomcat 7 via Maven.

```bash
cd legacy-java
mvn clean package
mvn tomcat7:run
# -> http://localhost:8081/orderco
```

Verified end-to-end by hand: order creation, the bulk-discount rule,
tax calculation, and every status-transition guard rail (can't submit an
empty order, can't approve a non-submitted order, etc.) all behave
correctly against a live, running instance — not just compiled.

### `modernized-python/` — the "after"

The same domain rebuilt as a FastAPI backend (SQLAlchemy models, Pydantic
schemas, the identical business rules re-implemented with matching
rounding behavior) and a React/Vite frontend.

```bash
# Backend
cd modernized-python/backend
python -m venv venv && ./venv/Scripts/activate   # or source venv/bin/activate on macOS/Linux
pip install -r requirements.txt
uvicorn app.main:app --reload
# -> http://localhost:8000 (interactive docs at /docs)

# Frontend (separate terminal)
cd modernized-python/frontend
npm install
npm run dev
# -> http://localhost:5173
```

Backed by 11 passing `pytest` tests (no Docker/Postgres required — they run
against a throwaway **in-memory** SQLite database, recreated fresh per
test) covering the bulk discount, tax calculation, every status-transition
guard rail, and — the one that matters most — numeric parity with the
legacy app for the same order.

Running `uvicorn` directly (as above) starts only the backend + frontend
against a local SQLite file, without the legacy Java app. To run all three
services (legacy Java included) concurrently, use one of the two options
below instead.

### Option A: Local Run (Without Docker - Windows)
Requires JDK 8 and Maven on your `PATH` for the legacy app (see
`legacy-java/` above) in addition to Python and Node. If you don't have
Docker installed, use the provided PowerShell helper script at the root
directory — it launches the Java Tomcat app, Python FastAPI app, and React
Vite app in three separate terminal windows:

```powershell
./start-local.ps1
# Backend: http://localhost:8000 (docs at /docs)
# Frontend: http://localhost:5173
# Legacy Java: http://localhost:8081/orderco
```

### Option B: Containerized Run (With Docker)
For a fully containerized deployment using PostgreSQL:

```bash
docker compose up --build
# Backend: http://localhost:8000 (docs at /docs)
# Frontend: http://localhost:3000
# Legacy Java: http://localhost:8081/orderco
```

## Proof of parity, at a glance

Same order (12 units of a $12.50 product + 2 units of an $89.99 product),
run independently against both systems:

| | Legacy | Modernized |
|---|---|---|
| Subtotal | $329.98 | $329.98 |
| Discount | $15.00 | $15.00 |
| Tax | $25.20 | $25.20 |
| **Total** | **$340.18** | **$340.18** |

Full details, including *why* each layer was mapped the way it was, in
[`MIGRATION.md`](MIGRATION.md).

> **Note:** the two halves use separate, unconnected databases -- H2 for
> the legacy app, SQLite/Postgres for the modernized one. An order created
> in one will **not** appear in the other. That's intentional: this repo
> proves the two systems produce identical results for identical input
> when run independently, not that they share a live data store (a real
> migration wouldn't point old and new code at the same tables mid-cutover
> either -- see the phased approach in [`MIGRATION.md`](MIGRATION.md#how-this-would-actually-run-as-a-client-engagement)).

## What else is in the modernized stack

Beyond reproducing the legacy behavior rule-for-rule, the modernized side
picks up a few things a real migration typically bundles in once the core
logic is ported and verified:

- **Containerized deployment.** `docker-compose.yml` brings up Postgres,
  the FastAPI backend, the React frontend (built and served via nginx),
  and the legacy app side by side, each from its own Dockerfile.
- **Schema migrations.** `alembic/` versions the database schema instead
  of relying on manual DDL, which is how the legacy H2 schema is managed
  today.
- **A small operations view.** The order list page computes total
  revenue, average order value, approval rate, and pending-order count
  directly from the order records and surfaces them as a dashboard. It's
  the same underlying data the legacy app's list page has always had --
  this just adds a second, aggregated way to look at it.

## License

MIT.
