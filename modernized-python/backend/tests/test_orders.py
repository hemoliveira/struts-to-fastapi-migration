"""
Business-rule tests for the modernized order service.

The `test_order_totals_match_legacy_reference` test below reproduces the
exact scenario manually verified end-to-end against the running legacy
Struts/JSP/H2 app (see MIGRATION.md): customer "Acme Retail Corp", 12 units
of a $12.50 product (triggers the bulk discount) plus 2 units of an $89.99
product (does not). The legacy app computed subtotal $329.98, discount
$15.00, tax $25.20, total $340.18 -- this test asserts the Python
reimplementation produces the identical numbers.
"""


def test_create_order_starts_in_draft(client):
    res = client.post("/orders", json={"customer_id": 1})
    assert res.status_code == 201
    assert res.json()["status"] == "DRAFT"
    assert res.json()["customer_name"] == "Acme Retail Corp"


def test_add_line_item_below_threshold_has_no_discount(client):
    order = client.post("/orders", json={"customer_id": 1}).json()
    res = client.post(f"/orders/{order['id']}/line-items", json={"product_id": 2, "quantity": 2})

    assert res.status_code == 201
    item = res.json()["line_items"][0]
    assert item["quantity"] == 2
    assert item["line_subtotal"] == "179.98"
    assert item["line_discount"] == "0.00"
    assert item["line_total"] == "179.98"


def test_add_line_item_at_threshold_applies_bulk_discount(client):
    order = client.post("/orders", json={"customer_id": 1}).json()
    res = client.post(f"/orders/{order['id']}/line-items", json={"product_id": 1, "quantity": 10})

    assert res.status_code == 201
    item = res.json()["line_items"][0]
    # 10 * 12.50 = 125.00, 10% discount = 12.50, total = 112.50
    assert item["line_subtotal"] == "125.00"
    assert item["line_discount"] == "12.50"
    assert item["line_total"] == "112.50"


def test_order_totals_match_legacy_reference(client):
    order = client.post("/orders", json={"customer_id": 1}).json()
    order_id = order["id"]

    client.post(f"/orders/{order_id}/line-items", json={"product_id": 1, "quantity": 12})
    client.post(f"/orders/{order_id}/line-items", json={"product_id": 2, "quantity": 2})

    res = client.get(f"/orders/{order_id}")
    body = res.json()

    assert body["subtotal"] == "329.98"
    assert body["discount_amount"] == "15.00"
    assert body["tax_amount"] == "25.20"
    assert body["total"] == "340.18"


def test_cannot_add_line_item_to_non_draft_order(client):
    order = client.post("/orders", json={"customer_id": 1}).json()
    order_id = order["id"]
    client.post(f"/orders/{order_id}/line-items", json={"product_id": 1, "quantity": 1})
    client.post(f"/orders/{order_id}/submit")

    res = client.post(f"/orders/{order_id}/line-items", json={"product_id": 1, "quantity": 1})
    assert res.status_code == 400
    assert "DRAFT" in res.json()["detail"]


def test_cannot_submit_an_order_with_no_line_items(client):
    order = client.post("/orders", json={"customer_id": 1}).json()
    res = client.post(f"/orders/{order['id']}/submit")
    assert res.status_code == 400
    assert "no line items" in res.json()["detail"]


def test_submit_then_approve_flow(client):
    order = client.post("/orders", json={"customer_id": 1}).json()
    order_id = order["id"]
    client.post(f"/orders/{order_id}/line-items", json={"product_id": 1, "quantity": 1})

    submit_res = client.post(f"/orders/{order_id}/submit")
    assert submit_res.json()["status"] == "SUBMITTED"

    approve_res = client.post(f"/orders/{order_id}/approve")
    assert approve_res.json()["status"] == "APPROVED"


def test_cannot_approve_an_already_approved_order(client):
    order = client.post("/orders", json={"customer_id": 1}).json()
    order_id = order["id"]
    client.post(f"/orders/{order_id}/line-items", json={"product_id": 1, "quantity": 1})
    client.post(f"/orders/{order_id}/submit")
    client.post(f"/orders/{order_id}/approve")

    res = client.post(f"/orders/{order_id}/approve")
    assert res.status_code == 400
    assert "SUBMITTED" in res.json()["detail"]


def test_reject_flow(client):
    order = client.post("/orders", json={"customer_id": 1}).json()
    order_id = order["id"]
    client.post(f"/orders/{order_id}/line-items", json={"product_id": 1, "quantity": 1})
    client.post(f"/orders/{order_id}/submit")

    res = client.post(f"/orders/{order_id}/reject")
    assert res.json()["status"] == "REJECTED"


def test_get_nonexistent_order_returns_404(client):
    res = client.get("/orders/999999")
    assert res.status_code == 404


def test_list_orders_returns_all_created_orders(client):
    client.post("/orders", json={"customer_id": 1})
    client.post("/orders", json={"customer_id": 2})

    res = client.get("/orders")
    assert res.status_code == 200
    assert len(res.json()) == 2
