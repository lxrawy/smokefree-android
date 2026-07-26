#!/usr/bin/env python3
"""
处理 SmokeFree App 图标：等比例放大后中心裁剪为正方形，保持原始背景
"""
from PIL import Image
import os

SRC_ICON = "/Users/cole/.workbuddy/clipboard-images/clipboard-2026-07-26T02-13-56-119Z-16041d8d.jpg"
RES_DIR = "/Users/cole/WorkBuddy/Claw/android-smokefree/app/src/main/res"

DENSITIES = {
    "mipmap-ldpi": 36,
    "mipmap-mdpi": 48,
    "mipmap-hdpi": 72,
    "mipmap-xhdpi": 96,
    "mipmap-xxhdpi": 144,
    "mipmap-xxxhdpi": 192,
}


def create_icon(src_path, output_dir, size, zoom=1.12):
    img = Image.open(src_path).convert("RGBA")
    orig_w, orig_h = img.size

    # 等比例缩放
    scaled_w = int(orig_w * zoom)
    scaled_h = int(orig_h * zoom)
    scaled = img.resize((scaled_w, scaled_h), Image.LANCZOS)

    # 中心裁剪出正方形
    min_side = min(scaled_w, scaled_h)
    left = (scaled_w - min_side) // 2
    top = (scaled_h - min_side) // 2
    cropped = scaled.crop((left, top, left + min_side, top + min_side))

    # 缩放到目标尺寸
    final = cropped.resize((size, size), Image.LANCZOS)

    output_path = os.path.join(output_dir, "ic_launcher.png")
    final.save(output_path, "PNG")
    round_path = os.path.join(output_dir, "ic_launcher_round.png")
    final.save(round_path, "PNG")
    print(f"  Generated {size}x{size}")


def main():
    print("Processing SmokeFree app icon (zoom=1.12)...")
    for density, size in DENSITIES.items():
        output_dir = os.path.join(RES_DIR, density)
        os.makedirs(output_dir, exist_ok=True)
        create_icon(SRC_ICON, output_dir, size, zoom=1.12)
    print("\nDone!")


if __name__ == "__main__":
    main()
