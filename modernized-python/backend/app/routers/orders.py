from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session

from app import schemas
from app.database import get_db
from app.services import order_service
from app.services.order_service import BusinessRuleError

router = APIRouter(prefix="/orders", tags=["orders"])


@router.get("", response_model=list[schemas.PurchaseOrderOut])
def list_orders(db: Session = Depends(get_db)):
    return order_service.list_orders(db)


@router.post("", response_model=schemas.PurchaseOrderOut, status_code=201)
def create_order(request: schemas.CreateOrderRequest, db: Session = Depends(get_db)):
    order = order_service.create_order(db, request.customer_id)
    return order


@router.get("/{order_id}", response_model=schemas.PurchaseOrderOut)
def get_order(order_id: int, db: Session = Depends(get_db)):
    order = order_service.get_order(db, order_id)
    if order is None:
        raise HTTPException(status_code=404, detail=f"Order {order_id} was not found.")
    return order


@router.post("/{order_id}/line-items", response_model=schemas.PurchaseOrderOut, status_code=201)
def add_line_item(order_id: int, request: schemas.AddLineItemRequest, db: Session = Depends(get_db)):
    try:
        return order_service.add_line_item(db, order_id, request.product_id, request.quantity)
    except BusinessRuleError as e:
        raise HTTPException(status_code=400, detail=str(e))


@router.post("/{order_id}/submit", response_model=schemas.PurchaseOrderOut)
def submit_order(order_id: int, db: Session = Depends(get_db)):
    try:
        return order_service.submit_order(db, order_id)
    except BusinessRuleError as e:
        raise HTTPException(status_code=400, detail=str(e))


@router.post("/{order_id}/approve", response_model=schemas.PurchaseOrderOut)
def approve_order(order_id: int, db: Session = Depends(get_db)):
    try:
        return order_service.approve_order(db, order_id)
    except BusinessRuleError as e:
        raise HTTPException(status_code=400, detail=str(e))


@router.post("/{order_id}/reject", response_model=schemas.PurchaseOrderOut)
def reject_order(order_id: int, db: Session = Depends(get_db)):
    try:
        return order_service.reject_order(db, order_id)
    except BusinessRuleError as e:
        raise HTTPException(status_code=400, detail=str(e))
