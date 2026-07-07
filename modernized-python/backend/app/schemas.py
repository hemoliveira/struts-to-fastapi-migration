import datetime
from decimal import Decimal

from pydantic import BaseModel, ConfigDict


class CustomerOut(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: int
    name: str
    email: str


class ProductOut(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: int
    sku: str
    name: str
    unit_price: Decimal


class OrderLineItemOut(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: int
    product_id: int
    product_name: str
    quantity: int
    unit_price: Decimal
    line_subtotal: Decimal
    line_discount: Decimal
    line_total: Decimal


class PurchaseOrderOut(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: int
    customer_id: int
    customer_name: str
    status: str
    created_date: datetime.datetime
    subtotal: Decimal
    discount_amount: Decimal
    tax_amount: Decimal
    total: Decimal
    line_items: list[OrderLineItemOut] = []


class CreateOrderRequest(BaseModel):
    customer_id: int


class AddLineItemRequest(BaseModel):
    product_id: int
    quantity: int
