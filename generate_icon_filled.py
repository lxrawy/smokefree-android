#!/usr/bin/env python3
"""Generate app icons with content filling the canvas (100% fill)."""
from PIL import Image
import os

SRC = "/Users/cole/.workbuddy/clipboard-images/clipboard-2026-07-26T01-25-48-226Z-8a2b0a05.jpg"
OUT_DIR = "/Users/cole/WorkBuddy/Claw/android-smokefree/app/src/main/res"
PLAY_STORE = "/Users/cole/WorkBuddy/Claw/android-smokefree/app/play_store_icon.png"

# Android mipmap sizes
SIZES = {
    "mipmap-mdpi": 48,
    "mipmap-hdpi": 72,
    "mipmap-xhdpi": 96,
    "mipmap-xxhdpi": 144,
    "mipmap-xxxhdpi": 192,
}

# Fill ratio: 1.0 = content touches canvas edges
FILL_RATIO = 1.0


def process_icon(src_path, size, fill_ratio):
    """Process source image into a filled icon of given size."""
    img = Image.open(src_path).convert("RGBA")

    # Make white-ish background transparent
    data = list(img.getdata())
    new_data = []
    for r, g, b, a in data:
        if r >= 245 and g >= 245 and b >= 245:
            new_data.append((255, 255, 255, 0))
        else:
            new_data.append((r, g, b, a))
    img.putdata(new_data)

    # Find bounding box of non-transparent content
    bbox = img.getbbox()
    if bbox is None:
        raise ValueError("Image has no visible content after transparency!")

    # Crop to content
    img_cropped = img.crop(bbox)
    cw, ch = img_cropped.size

    # Target content size on canvas
    target_content_size = int(size * fill_ratio)

    # Compute scale to fit content within target size while preserving aspect ratio
    scale = target_content_size / max(cw, ch)
    new_w = int(cw * scale)
    new_h = int(ch * scale)

    # Resize content
    img_resized = img_cropped.resize((new_w, new_h), Image.LANCZOS)

    # Create canvas and paste centered
    canvas = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    paste_x = (size - new_w) // 2
    paste_y = (size - new_h) // 2
    canvas.paste(img_resized, (paste_x, paste_y), img_resized)

    return canvas


def main():
    # Generate both ic_launcher and ic_launcher_round for all sizes
    for folder, size in SIZES.items():
        icon = process_icon(SRC, size, FILL_RATIO)
        
        # Square icon
        out_path = os.path.join(OUT_DIR, folder, "ic_launcher.png")
        icon.save(out_path, "PNG")
        print(f"Generated: {out_path} ({size}x{size})")
        
        # Round icon (same content, canvas may be cropped to circle by system)
        round_path = os.path.join(OUT_DIR, folder, "ic_launcher_round.png")
        icon.save(round_path, "PNG")
        print(f"Generated: {round_path} ({size}x{size})")

    # Generate Play Store icon (512x512)
    play_store = process_icon(SRC, 512, FILL_RATIO)
    play_store.save(PLAY_STORE, "PNG")
    print(f"Generated: {PLAY_STORE} (512x512)")

    print("\nDone! Icons regenerated with 100% canvas fill.")


if __name__ == "__main__":
    main()
