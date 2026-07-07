import os

DATASET_ROOT = "dataset"

def normalize_label(folder_name):
    return folder_name.strip().upper()

def scan_dataset(subfolder):
    path = os.path.join(DATASET_ROOT, subfolder)
    if not os.path.exists(path):
        print(f"  WARNING: {path} does not exist!")
        return {}

    counts = {}
    for folder_name in sorted(os.listdir(path)):
        folder_path = os.path.join(path, folder_name)
        if not os.path.isdir(folder_path):
            continue
        label = normalize_label(folder_name)
        images = [f for f in os.listdir(folder_path)
                  if f.lower().endswith((".bmp", ".png", ".jpg", ".jpeg", ".tif", ".tiff"))]
        counts[label] = counts.get(label, 0) + len(images)
    return counts

def main():
    print("=" * 50)
    print("ONLINE DATASET")
    print("=" * 50)
    online_counts = scan_dataset("online")
    for label, count in sorted(online_counts.items()):
        print(f"  {label:6s}: {count} images")
    print(f"  TOTAL: {sum(online_counts.values())} images")

    print()
    print("=" * 50)
    print("MANUAL DATASET")
    print("=" * 50)
    manual_counts = scan_dataset("manual")
    for label, count in sorted(manual_counts.items()):
        print(f"  {label:6s}: {count} images")
    print(f"  TOTAL: {sum(manual_counts.values())} images")

    print()
    print("=" * 50)
    print("LABEL CONSISTENCY CHECK")
    print("=" * 50)
    online_labels = set(online_counts.keys())
    manual_labels = set(manual_counts.keys())
    print(f"  Labels only in online: {online_labels - manual_labels}")
    print(f"  Labels only in manual: {manual_labels - online_labels}")
    print(f"  Common labels: {sorted(online_labels & manual_labels)}")

if __name__ == "__main__":
    main()
