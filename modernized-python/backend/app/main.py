import os
from decimal import Decimal

from fastapi import FastAPI, Request
from fastapi.responses import JSONResponse
from fastapi.middleware.cors import CORSMiddleware

from app.database import Base, SessionLocal, engine
from app.models import Customer, Product
from app.routers import customers, orders, products
from app.services.order_service import BusinessRuleError

# Note: Base.metadata.create_all is used here for zero-setup local convenience.
# In a production environment, database migrations should be run via Alembic (see alembic/ directory).
Base.metadata.create_all(bind=engine)


def seed_if_empty() -> None:
    db = SessionLocal()
    try:
        if db.query(Customer).count() > 0:
            return

        db.add_all(
            [
                Customer(name="Acme Retail Corp", email="purchasing@acmeretail.example"),
                Customer(name="Blue Ridge Distributors", email="orders@blueridge.example"),
                Customer(name="Coastal Supply Co", email="ap@coastalsupply.example"),
            ]
        )
        db.add_all(
            [
                Product(sku="WID-100", name="Standard Widget", unit_price=Decimal("12.50")),
                Product(sku="WID-200", name="Heavy-Duty Widget", unit_price=Decimal("24.00")),
                Product(sku="GAD-300", name="Premium Gadget", unit_price=Decimal("89.99")),
                Product(sku="GAD-400", name="Economy Gadget", unit_price=Decimal("15.75")),
            ]
        )
        db.commit()
    finally:
        db.close()


seed_if_empty()

app = FastAPI(
    title="OrderCo (modernized)",
    description="FastAPI/SQLAlchemy rebuild of the Struts/JSP OrderCo legacy app. See MIGRATION.md.",
    version="1.0.0",
)

@app.exception_handler(BusinessRuleError)
def business_rule_exception_handler(request: Request, exc: BusinessRuleError):
    return JSONResponse(
        status_code=400,
        content={"detail": str(exc), "error_code": "BUSINESS_RULE_VIOLATION"}
    )

cors_origins = os.getenv("CORS_ORIGINS", "http://localhost:5173").split(",")
app.add_middleware(
    CORSMiddleware,
    allow_origins=cors_origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(customers.router)
app.include_router(products.router)
app.include_router(orders.router)


@app.get("/health")
def health():
    return {"status": "ok"}
