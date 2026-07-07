import os

from sqlalchemy import create_engine
from sqlalchemy.orm import declarative_base, sessionmaker

# Real deployments point this at Postgres (see docker-compose.yml at the
# repo root). Falls back to a local SQLite file so the app can be tried
# with zero setup; tests override this again with an in-memory SQLite
# database (see tests/conftest.py).
DATABASE_URL = os.getenv("DATABASE_URL", "sqlite:///./orderco.db")

connect_args = {"check_same_thread": False} if DATABASE_URL.startswith("sqlite") else {}
engine = create_engine(DATABASE_URL, connect_args=connect_args)

SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

Base = declarative_base()


def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()
