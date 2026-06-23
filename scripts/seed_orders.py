#!/usr/bin/env python3
"""
Seed sample orders into Firebase Realtime Database (node: orders).

ID format: HTNGTD + 8 random digits (e.g. HTNGTD07820426).

Usage:
  # Import bundled sample data (merge into existing orders)
  python3 scripts/seed_orders.py --import firebase/orders_seed.json

  # Generate and push N new random orders
  python3 scripts/seed_orders.py --generate 5

Requires one of:
  - FIREBASE_DATABASE_URL (default from google-services.json)
  - FIREBASE_ID_TOKEN for authenticated REST writes, OR
  - database rules that allow write during development

Example:
  export FIREBASE_ID_TOKEN="your-id-token"
  python3 scripts/seed_orders.py --import firebase/orders_seed.json
"""

from __future__ import annotations

import argparse
import json
import os
import random
import sys
import urllib.error
import urllib.request
from datetime import datetime, timezone
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
DEFAULT_DB_URL = "https://teatrack-htng-default-rtdb.asia-southeast1.firebasedatabase.app"

PRODUCTS = [
    ("prod_hongtra_ngogia", "Hồng Trà Ngô Gia", 16000),
    ("prod_olong_mochuong", "Trà Ô Long Mộc Hương", 19000),
    ("prod_hongtra_bidao", "Hồng Trà Bí Đao", 16000),
    ("prod_traxanh_bidao", "Trà Xanh Bí Đao", 19000),
    ("prod_suatuoi_khoaimon", "Sữa Tươi Khoai Môn Nghiền", 26000),
    ("prod_olong_latte", "Ô Long Latte", 25000),
    ("prod_trasuatranchau", "Trà Sữa Trân Châu Đường Đen", 27000),
]

CUSTOMERS = [
    ("Trần Ngọc Bảo Vy", "0917384492", "Thị xã Dĩ An, Bình Dương", "CS01"),
    ("Nguyễn Minh Khang", "0903123456", "H071 - 244 Duong So 8 Street, Linh Xuan Ward, Ho Chi Minh City", "CS02"),
    ("Lê Thị Hương", "0938765432", "H116 - 181 Nguyễn Gia Trí, Phường 25, Thạnh Mỹ Tây, Hồ Chí Minh", "CS03"),
    ("Phạm Quốc Anh", "0977123987", "H054 - 29/1 Nguyễn Gia Trí, Phường 25, Thạnh Mỹ Tây, Hồ Chí Minh", "CS04"),
]

PAYMENTS = ["Tiền mặt", "MoMo", "ZaloPay", "Ví điện tử"]
STATUSES = ["pending", "processing", "completed", "completed", "completed"]


def generate_order_id(existing: set[str]) -> str:
    while True:
        order_id = f"HTNGTD{random.randint(10_000_000, 99_999_999)}"
        if order_id not in existing:
            existing.add(order_id)
            return order_id


def iso_now() -> str:
    return datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%S.%f")[:-3] + "Z"


def format_meta(minutes_ago: int, total: int) -> str:
    if minutes_ago < 60:
        return f"{minutes_ago} mins ago • {total:,}".replace(",", ".") + "đ"
    hours = minutes_ago // 60
    return f"{hours} hour ago • {total:,}".replace(",", ".") + "đ"


def build_random_order(existing_ids: set[str]) -> dict:
    order_id = generate_order_id(existing_ids)
    name, phone, address, user_id = random.choice(CUSTOMERS)
    payment = random.choice(PAYMENTS)
    status = random.choice(STATUSES)
    shipping = 10000
    discount = 0

    item_count = random.randint(1, 3)
    items = {}
    subtotal = 0
    for index in range(item_count):
        product_id, product_name, unit_price = random.choice(PRODUCTS)
        qty = random.randint(1, 2)
        line_total = unit_price * qty
        subtotal += line_total
        items[str(index)] = {
            "productId": product_id,
            "productName": product_name,
            "quantity": qty,
            "unitPrice": unit_price,
            "lineTotal": line_total,
            "size": random.choice(["M", "L"]),
            "sugar": random.choice(["Ít", "Vừa", "Nhiều"]),
            "ice": random.choice(["Ít", "Vừa", "Nhiều"]),
            "toppings": random.choice(["", "Trân châu đen x1", "Kem cheese x1"]),
        }

    total = subtotal + shipping - discount
    now = iso_now()
    minutes_ago = random.choice([3, 8, 15, 45, 120, 180])

    return {
        "id": order_id,
        "orderId": order_id,
        "title": f"New Order #{order_id}",
        "meta": format_meta(minutes_ago, total),
        "date": now,
        "customerName": name,
        "customerPhone": phone,
        "customerAddress": address,
        "paymentMethod": payment,
        "status": status,
        "subtotal": subtotal,
        "shipping": shipping,
        "discount": discount,
        "total": total,
        "items": items,
        "deliveryDate": datetime.now(timezone.utc).strftime("%Y-%m-%d"),
        "deliveryTime": f"{random.randint(10, 21):02d}:{random.choice(['00', '15', '30', '45'])}",
        "note": random.choice(["", "Không có ghi chú", "Ít đường, nhiều đá"]),
        "couponCode": "",
        "userId": user_id,
        "createdAt": now.replace("Z", "+00:00"),
        "updatedAt": now.replace("Z", "+00:00"),
    }


def patch_orders(db_url: str, orders: dict, id_token: str | None) -> None:
    url = f"{db_url.rstrip('/')}/orders.json"
    if id_token:
        url += f"?auth={id_token}"

    payload = json.dumps(orders).encode("utf-8")
    request = urllib.request.Request(
        url,
        data=payload,
        method="PATCH",
        headers={"Content-Type": "application/json"},
    )
    try:
        with urllib.request.urlopen(request, timeout=30) as response:
            print(f"Pushed {len(orders)} order(s). HTTP {response.status}")
    except urllib.error.HTTPError as error:
        body = error.read().decode("utf-8", errors="replace")
        print(f"Firebase write failed: HTTP {error.code}\n{body}", file=sys.stderr)
        sys.exit(1)


def load_seed_file(path: Path) -> dict:
    data = json.loads(path.read_text(encoding="utf-8"))
    if "orders" in data:
        return data["orders"]
    return data


def main() -> None:
    parser = argparse.ArgumentParser(description="Seed Firebase Realtime Database orders")
    parser.add_argument("--import", dest="import_file", help="JSON file with orders data")
    parser.add_argument("--generate", type=int, default=0, help="Generate N random orders")
    parser.add_argument("--db-url", default=os.getenv("FIREBASE_DATABASE_URL", DEFAULT_DB_URL))
    parser.add_argument("--dry-run", action="store_true", help="Print JSON only, do not push")
    args = parser.parse_args()

    orders: dict = {}

    if args.import_file:
        orders.update(load_seed_file(Path(args.import_file)))

    if args.generate > 0:
        existing = set(orders.keys())
        for _ in range(args.generate):
            order = build_random_order(existing)
            orders[order["id"]] = order

    if not orders:
        parser.error("Nothing to push. Use --import or --generate.")

    if args.dry_run:
        print(json.dumps({"orders": orders}, ensure_ascii=False, indent=2))
        return

    id_token = os.getenv("FIREBASE_ID_TOKEN")
    if not id_token:
        print("Warning: FIREBASE_ID_TOKEN not set. Push only works if DB rules allow public write.", file=sys.stderr)

    patch_orders(args.db_url, orders, id_token)
    print("Done. Order IDs:")
    for order_id in sorted(orders.keys()):
        print(f"  - {order_id}")


if __name__ == "__main__":
    main()
