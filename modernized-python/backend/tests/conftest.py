from decimal import Decimal

import pytest
from fastapi.testclient import TestClient
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
from sqlalchemy.pool import StaticPool

from app.database import Base, get_db
from app.main import app
from app.models import Customer, Product


@pytest.fixture()
def client():
    """Fresh in-memory SQLite database per test -- no external services,
    same spirit as pg-mem in the auth-rbac-starter's test suite."""
    engine = create_engine(
        "sqlite:///:memory:",
        connect_args={"check_same_thread": False},
        poolclass=StaticPool,
    )
    TestingSessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)
    Base.metadata.create_all(bind=engine)

    session = TestingSessionLocal()
    session.add_all(
        [
            Customer(name="Acme Retail Corp", email="purchasing@acmeretail.example"),
            Customer(name="Blue Ridge Distributors", email="orders@blueridge.example"),
        ]
    )
    session.add_all(
        [
            Product(sku="WID-100", name="Standard Widget", unit_price=Decimal("12.50")),
            Product(sku="GAD-300", name="Premium Gadget", unit_price=Decimal("89.99")),
        ]
    )
    session.commit()
    session.close()

    def override_get_db():
        db = TestingSessionLocal()
        try:
            yield db
        finally:
            db.close()

    app.dependency_overrides[get_db] = override_get_db
    yield TestClient(app)
    app.dependency_overrides.clear()
