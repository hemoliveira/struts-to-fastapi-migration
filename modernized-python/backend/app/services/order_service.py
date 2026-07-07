"""
Order business logic. This is the direct Python counterpart of
OrderService.java on the legacy side -- same rules, same rounding, same
status workflow. See MIGRATION.md at the repo root for the side-by-side
mapping and why the numbers below must match exactly.
"""
from decimal import Decimal, ROUND_HALF_UP

from sqlalchemy.orm import Session

from app.models import OrderLineItem, Product, PurchaseOrder

BULK_DISCOUNT_THRESHOLD = 10
BULK_DISCOUNT_RATE = Decimal("0.10")
TAX_RATE = Decimal("0.08")

TWO_PLACES = Decimal("0.01")


class BusinessRuleError(Exception):
    """Mirrors BusinessRuleException on the legacy side."""


def _round(value: Decimal) -> Decimal:
    return value.quantize(TWO_PLACES, rounding=ROUND_HALF_UP)


def calculate_line_discount(quantity: int, line_subtotal: Decimal) -> Decimal:
    """Bulk-discount rule: quantity >= BULK_DISCOUNT_THRESHOLD earns
    BULK_DISCOUNT_RATE off that line's subtotal."""
    if quantity >= BULK_DISCOUNT_THRESHOLD:
        return _round(line_subtotal * BULK_DISCOUNT_RATE)
    return _round(Decimal("0"))


def create_order(db: Session, customer_id: int) -> PurchaseOrder:
    order = PurchaseOrder(customer_id=customer_id, status=PurchaseOrder.STATUS_DRAFT)
    db.add(order)
    db.commit()
    db.refresh(order)
    return order


def add_line_item(db: Session, order_id: int, product_id: int, quantity: int) -> PurchaseOrder:
    order = db.get(PurchaseOrder, order_id)
    if order is None:
        raise BusinessRuleError(f"Order {order_id} does not exist.")
    if order.status != PurchaseOrder.STATUS_DRAFT:
        raise BusinessRuleError("Line items can only be added while an order is in DRAFT status.")
    if quantity <= 0:
        raise BusinessRuleError("Quantity must be a positive number.")

    product = db.get(Product, product_id)
    if product is None:
        raise BusinessRuleError(f"Product {product_id} does not exist.")

    line_subtotal = _round(product.unit_price * quantity)
    line_discount = calculate_line_discount(quantity, line_subtotal)
    line_total = _round(line_subtotal - line_discount)

    line_item = OrderLineItem(
        order_id=order.id,
        product_id=product.id,
        quantity=quantity,
        unit_price=product.unit_price,
        line_subtotal=line_subtotal,
        line_discount=line_discount,
        line_total=line_total,
    )
    db.add(line_item)
    db.commit()

    _recalculate_order_totals(db, order_id)
    db.refresh(order)
    return order


def _recalculate_order_totals(db: Session, order_id: int) -> None:
    order = db.get(PurchaseOrder, order_id)

    subtotal = sum((item.line_subtotal for item in order.line_items), Decimal("0"))
    discount = sum((item.line_discount for item in order.line_items), Decimal("0"))

    taxable = subtotal - discount
    tax = _round(taxable * TAX_RATE)
    total = _round(taxable + tax)

    order.subtotal = _round(subtotal)
    order.discount_amount = _round(discount)
    order.tax_amount = tax
    order.total = total
    db.commit()


def submit_order(db: Session, order_id: int) -> PurchaseOrder:
    order = db.get(PurchaseOrder, order_id)
    if order is None:
        raise BusinessRuleError(f"Order {order_id} does not exist.")
    if order.status != PurchaseOrder.STATUS_DRAFT:
        raise BusinessRuleError("Only a DRAFT order can be submitted.")
    if not order.line_items:
        raise BusinessRuleError("Cannot submit an order with no line items.")

    order.status = PurchaseOrder.STATUS_SUBMITTED
    db.commit()
    db.refresh(order)
    return order


def _transition_from_submitted(db: Session, order_id: int, target_status: str) -> PurchaseOrder:
    order = db.get(PurchaseOrder, order_id)
    if order is None:
        raise BusinessRuleError(f"Order {order_id} does not exist.")
    if order.status != PurchaseOrder.STATUS_SUBMITTED:
        raise BusinessRuleError("Only a SUBMITTED order can be approved or rejected.")

    order.status = target_status
    db.commit()
    db.refresh(order)
    return order


def approve_order(db: Session, order_id: int) -> PurchaseOrder:
    return _transition_from_submitted(db, order_id, PurchaseOrder.STATUS_APPROVED)


def reject_order(db: Session, order_id: int) -> PurchaseOrder:
    return _transition_from_submitted(db, order_id, PurchaseOrder.STATUS_REJECTED)


def get_order(db: Session, order_id: int) -> PurchaseOrder | None:
    return db.get(PurchaseOrder, order_id)


def list_orders(db: Session) -> list[PurchaseOrder]:
    return db.query(PurchaseOrder).order_by(PurchaseOrder.id.desc()).all()
