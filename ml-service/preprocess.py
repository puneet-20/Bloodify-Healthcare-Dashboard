import os
import shutil
import random
import cv2
from image_utils import preprocess_image

random.seed(42)

DATASET_ROOT = "dataset"
PROCESSED_ROOT = os.path.join(DATASET_ROOT, "processed")

SPLIT_RATIOS = {"train": 0.70, "val": 0.15, "test": 0.15}

def normalize_label(folder_name):
    return folder_name.strip().upper()

def get_image_files(folder_path):
    return [f for f in os.listdir(folder_path)
            if f.lower().endswith((".bmp", ".png", ".jpg", ".jpeg", ".tif", ".tiff"))]

def process_and_save(src_path, dst_path):
    processed = preprocess_image(src_path)  # (96,96,1) float32, 0-1
    img_to_save = (processed[:, :, 0] * 255).astype("uint8")
    cv2.imwrite(dst_path, img_to_save)

def process_online_with_splits():
    print("Processing ONLINE dataset (train/val/test split)...")
    online_path = os.path.join(DATASET_ROOT, "online")

    for folder_name in sorted(os.listdir(online_path)):
        folder_path = os.path.join(online_path, folder_name)
        if not os.path.isdir(folder_path):
            continue
        label = normalize_label(folder_name)
        images = get_image_files(folder_path)
        random.shuffle(images)

        n = len(images)
        n_train = int(n * SPLIT_RATIOS["train"])
        n_val = int(n * SPLIT_RATIOS["val"])

        splits = {
            "train": images[:n_train],
            "val": images[n_train:n_train + n_val],
            "test": images[n_train + n_val:]
        }

        for split_name, file_list in splits.items():
            out_dir = os.path.join(PROCESSED_ROOT, split_name, label)
            os.makedirs(out_dir, exist_ok=True)
            for fname in file_list:
                src = os.path.join(folder_path, fname)
                dst = os.path.join(out_dir, fname)
                process_and_save(src, dst)

        print(f"  {label}: {n} total -> train={len(splits['train'])}, val={len(splits['val'])}, test={len(splits['test'])}")

def process_manual_holdout():
    print()
    print("Processing MANUAL dataset (held-out real-world test set)...")
    manual_path = os.path.join(DATASET_ROOT, "manual")

    for folder_name in sorted(os.listdir(manual_path)):
        folder_path = os.path.join(manual_path, folder_name)
        if not os.path.isdir(folder_path):
            continue
        label = normalize_label(folder_name)
        images = get_image_files(folder_path)

        out_dir = os.path.join(PROCESSED_ROOT, "manual_holdout", label)
        os.makedirs(out_dir, exist_ok=True)
        for fname in images:
            src = os.path.join(folder_path, fname)
            dst = os.path.join(out_dir, fname)
            process_and_save(src, dst)

        print(f"  {label}: {len(images)} images processed")

def main():
    if os.path.exists(PROCESSED_ROOT):
        print(f"Removing old processed folder: {PROCESSED_ROOT}")
        shutil.rmtree(PROCESSED_ROOT)

    process_online_with_splits()
    process_manual_holdout()
    print()
    print("Done. Processed data saved under:", PROCESSED_ROOT)

if __name__ == "__main__":
    main()
