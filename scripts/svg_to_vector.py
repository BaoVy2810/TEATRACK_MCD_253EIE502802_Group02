#!/usr/bin/env python3
"""Convert SVG files in res/drawable to Android Vector Drawable XML."""

import os
import re
import xml.etree.ElementTree as ET
from pathlib import Path

DRAWABLE_DIR = Path(__file__).resolve().parents[1] / "app/src/main/res/drawable"

# Old PNG/resource name -> new SVG source file (without extension)
ALIASES = {
    "bag1": "bag_1",
    "coupon1": "coupon_1",
    "coupon2": "coupon_2",
    "coupon3": "coupon_3",
    "documentadd": "document_add",
    "edit2": "edit_2",
    "cloudupload": "cloud_upload",
    "clouddownload": "cloud_download",
    "document2": "document_2",
    "documentdelite": "document_delete",
    "ic_arrow_back": "left",
    "ic_arrow_forward": "right",
    "ic_store": "box_1",
    "ic_location_on": "location",
    "ic_info_outline": "info_circle",
    "ic_local_offer": "coupon_1",
    "ic_cash": "coins",
    "ic_delete_outline": "trash_can",
    "ic_username": "user",
    "ic_email": "mail",
    "ic_lock": "lock",
    "ic_phonenumber": "call",
    "ic_sucessfully": "check",
}


def parse_color(value: str) -> str:
    if not value or value.lower() == "none":
        return "@android:color/transparent"
    return value


def circle_to_path(cx: float, cy: float, r: float) -> str:
    return (
        f"M{cx - r},{cy} "
        f"a{r},{r} 0 1,0 {2 * r},0 "
        f"a{r},{r} 0 1,0 {-2 * r},0"
    )


def rect_to_path(x: float, y: float, w: float, h: float, rx: float = 0) -> str:
    if rx > 0:
        return (
            f"M{x + rx},{y} h{w - 2 * rx} a{rx},{rx} 0 0,1 {rx},{rx} "
            f"v{h - 2 * rx} a{rx},{rx} 0 0,1 {-rx},{rx} "
            f"h{-w + 2 * rx} a{rx},{rx} 0 0,1 {-rx},{-rx} "
            f"v{-h + 2 * rx} a{rx},{rx} 0 0,1 {rx},{-rx} z"
        )
    return f"M{x},{y} h{w} v{h} h{-w} z"


def parse_transform(transform: str, x: float, y: float) -> tuple[float, float]:
    if not transform:
        return x, y
    matrix = re.search(
        r"matrix\(\s*(-?\d+(?:\.\d+)?)\s+(-?\d+(?:\.\d+)?)\s+"
        r"(-?\d+(?:\.\d+)?)\s+(-?\d+(?:\.\d+)?)\s+(-?\d+(?:\.\d+)?)\s+(-?\d+(?:\.\d+)?)\s*\)",
        transform,
    )
    if matrix:
        a, b, c, d, e, f = map(float, matrix.groups())
        return a * x + c * y + e, b * x + d * y + f
    return x, y


def element_to_path(elem: ET.Element) -> list[dict]:
    tag = elem.tag.split("}")[-1]
    attrs = elem.attrib
    paths: list[dict] = []

    stroke = attrs.get("stroke")
    fill = attrs.get("fill", "none")
    stroke_width = attrs.get("stroke-width", "0")
    stroke_linecap = attrs.get("stroke-linecap", "butt")
    stroke_linejoin = attrs.get("stroke-linejoin", "miter")

    base = {
        "strokeColor": parse_color(stroke) if stroke else None,
        "strokeWidth": stroke_width if stroke else None,
        "strokeLineCap": stroke_linecap if stroke_linecap != "butt" else None,
        "strokeLineJoin": stroke_linejoin if stroke_linejoin != "miter" else None,
        "fillColor": parse_color(fill),
    }

    if tag == "path" and attrs.get("d"):
        entry = dict(base)
        entry["pathData"] = attrs["d"]
        paths.append(entry)
    elif tag == "circle":
        cx = float(attrs.get("cx", 0))
        cy = float(attrs.get("cy", 0))
        r = float(attrs.get("r", 0))
        cx, cy = parse_transform(attrs.get("transform", ""), cx, cy)
        entry = dict(base)
        entry["pathData"] = circle_to_path(cx, cy, r)
        paths.append(entry)
    elif tag == "rect":
        x = float(attrs.get("x", 0))
        y = float(attrs.get("y", 0))
        w = float(attrs.get("width", 0))
        h = float(attrs.get("height", 0))
        rx = float(attrs.get("rx", 0))
        entry = dict(base)
        entry["pathData"] = rect_to_path(x, y, w, h, rx)
        paths.append(entry)

    return paths


def convert_svg(svg_path: Path) -> str:
    tree = ET.parse(svg_path)
    root = tree.getroot()

    view_box = root.attrib.get("viewBox", "0 0 24 24").split()
    if len(view_box) == 4:
        vw, vh = view_box[2], view_box[3]
    else:
        vw = root.attrib.get("width", "24").replace("px", "")
        vh = root.attrib.get("height", "24").replace("px", "")

    all_paths: list[dict] = []
    for elem in root.iter():
        if elem is root:
            continue
        all_paths.extend(element_to_path(elem))

    if not all_paths:
        raise ValueError(f"No paths found in {svg_path}")

    lines = [
        '<?xml version="1.0" encoding="utf-8"?>',
        '<vector xmlns:android="http://schemas.android.com/apk/res/android"',
        '    android:width="24dp"',
        '    android:height="24dp"',
        f'    android:viewportWidth="{vw}"',
        f'    android:viewportHeight="{vh}">',
    ]

    for path in all_paths:
        lines.append("    <path")
        lines.append(f'        android:pathData="{path["pathData"]}"')
        if path.get("fillColor"):
            lines.append(f'        android:fillColor="{path["fillColor"]}"')
        if path.get("strokeColor"):
            lines.append(f'        android:strokeColor="{path["strokeColor"]}"')
        if path.get("strokeWidth"):
            lines.append(f'        android:strokeWidth="{path["strokeWidth"]}"')
        if path.get("strokeLineCap"):
            lines.append(f'        android:strokeLineCap="{path["strokeLineCap"]}"')
        if path.get("strokeLineJoin"):
            lines.append(f'        android:strokeLineJoin="{path["strokeLineJoin"]}"')
        lines.append("        />")

    lines.append("</vector>")
    return "\n".join(lines) + "\n"


def write_vector(name: str, content: str) -> None:
    out = DRAWABLE_DIR / f"{name}.xml"
    out.write_text(content, encoding="utf-8")


def main() -> None:
    converted: dict[str, str] = {}

    for svg_file in sorted(DRAWABLE_DIR.glob("*.svg")):
        name = svg_file.stem
        try:
            content = convert_svg(svg_file)
            write_vector(name, content)
            converted[name] = content
            print(f"Converted {svg_file.name} -> {name}.xml")
        except Exception as exc:
            print(f"FAILED {svg_file.name}: {exc}")

    for alias, source in ALIASES.items():
        if source in converted:
            write_vector(alias, converted[source])
            print(f"Alias {alias}.xml <- {source}.xml")
        elif (DRAWABLE_DIR / f"{source}.xml").exists():
            content = (DRAWABLE_DIR / f"{source}.xml").read_text(encoding="utf-8")
            write_vector(alias, content)
            print(f"Alias {alias}.xml <- existing {source}.xml")
        else:
            print(f"MISSING alias source for {alias}: {source}")

    removed = 0
    for svg_file in DRAWABLE_DIR.glob("*.svg"):
        svg_file.unlink()
        removed += 1
    print(f"Removed {removed} .svg files")


if __name__ == "__main__":
    main()
