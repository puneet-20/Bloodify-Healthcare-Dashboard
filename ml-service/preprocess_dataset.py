import os
import numpy as np
from PIL import Image

DATASET_ROOT = "dataset"
IMG_SIZE = 96  # width and height in pixels

LABELS = ["A+", "A-", "AB+", "AB-", "B+", "B-", "O+", "O-"]
LABEL_TO_IDX = {label: idx for idx, label in enumerate(LABELS)}

def normalize_label(folder_name):
    return folder_name.strip().upper()

def load_and_process_image(path):
    """Load an image, convert to grayscale, resize, normalize to 0-1 range."""
    img = Image.open(path).convert("L")  # grayscale
    img = img.resize((IMG_SIZE, IMG_SIZE))
    arr = np.array(img, dtype=np.float32) / 255.0
    return arr

def process_subfolder(subfolder):
    path = os.path.join(DATASET_ROOT, subfolder)
    images = []
    labels = []
    skipped = 0

    for folder_name in sorted(os.listdir(path)):
        folder_path = os.path.join(path, folder_name)
        if not os.path.isdir(folder_path):
            continue
        label = normalize_label(folder_name)
        if label not in LABEL_TO_IDX:
            print(f"  Skipping unknown label folder: {folder_name}")
            continue

        for fname in sorted(os.listdir(folder_path)):
            if not fname.lower().endswith(('.bmp', '.png', '.jpg', '.jpeg', '.tif', '.tiff')):
                continue
            fpath = os.path.join(folder_path, fname)
            try:
                arr = load_and_process_image(fpath)
                images.append(arr)
                labels.append(LABEL_TO_IDX[label])
            except Exception as e:
                print(f"  Skipped {fpath}: {e}")
                skipped += 1

    images = np.array(images, dtype=np.float32)
    labels = np.array(labels, dtype=np.int64)
    print(f"  {subfolder}: {images.shape[0]} images loaded, {skipped} skipped")
    return images, labels

def main():
    print("Processing ONLINE dataset...")
    online_images, online_labels = process_subfolder("online")
    np.savez_compressed("online_data.npz", images=online_images, labels=online_labels)
    print(f"  Saved online_data.npz -> images shape {online_images.shape}, labels shape {online_labels.shape}")

    print()
    print("Processing MANUAL dataset...")
    manual_images, manual_labels = process_subfolder("manual")
    np.savez_compressed("manual_data.npz", images=manual_images, labels=manual_labels)
    print(f"  Saved manual_data.npz -> images shape {manual_images.shape}, labels shape {manual_labels.shape}")

    print()
    print("Label mapping used:")
    for label, idx in LABEL_TO_IDX.items():
        print(f"  {idx}: {label}")

if __name__ == "__main__":
    main()