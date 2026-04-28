#!/usr/bin/env python3

import os
import re
import shutil
from pathlib import Path

# === KONFIGURACJA ===

INPUT_DIR = Path("Free8DirRobot")
OUTPUT_DIR = Path("processed/robot")

# mapowanie kierunków (opcjonalne)
DIR_MAP = {
    "1": "n",
    "2": "ne",
    "3": "e",
    "4": "se",
    "5": "s",
    "6": "sw",
    "7": "w",
    "8": "nw",
}

USE_CARDINAL = True  # True = n/ne/e..., False = dir1..8

# regex do parsowania nazw
PATTERN = re.compile(
    r"LowPolyManny_(?P<variant>\w+)_rig_(?P<action>\w+)_dir(?P<dir>\d+)\.png"
)


# === FUNKCJE ===

def normalize_action(action: str) -> str:
    # CamelCase → snake_case
    s = re.sub(r'(?<!^)(?=[A-Z])', '_', action).lower()
    return s


def normalize_variant(variant: str) -> str:
    return variant.lower()


def map_direction(d: str) -> str:
    if USE_CARDINAL:
        return DIR_MAP.get(d, d)
    return f"dir{d}"


def process_file(src_path: Path):
    match = PATTERN.match(src_path.name)
    if not match:
        print(f"[SKIP] {src_path.name}")
        return

    variant = match.group("variant")
    action = match.group("action")
    direction = match.group("dir")

    # ignoruj export
    if variant.lower() == "export":
        print(f"[SKIP export] {src_path.name}")
        return

    variant = normalize_variant(variant)
    action = normalize_action(action)
    direction = map_direction(direction)

    # struktura katalogów
    dst_dir = OUTPUT_DIR / action / variant
    dst_dir.mkdir(parents=True, exist_ok=True)

    # nazwa pliku
    new_name = f"{direction}.png"
    dst_path = dst_dir / new_name

    print(f"[COPY] {src_path} → {dst_path}")
    shutil.copy2(src_path, dst_path)


# === MAIN ===

def main():
    if not INPUT_DIR.exists():
        print(f"Input dir not found: {INPUT_DIR}")
        return

    for root, _, files in os.walk(INPUT_DIR):
        for file in files:
            if file.endswith(".png"):
                process_file(Path(root) / file)


if __name__ == "__main__":
    main()
