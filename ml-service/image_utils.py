import cv2
import numpy as np

TARGET_SIZE = (96, 96)  # (width, height)

def preprocess_image(image_input, target_size=TARGET_SIZE):
    """
    Accepts either a file path (str) or a raw image array (numpy, from OpenCV/SecuGen capture).
    Handles color, grayscale, or even RGBA input safely.
    Returns a normalized (0-1 float32) grayscale image of shape (H, W, 1).
    """
    if isinstance(image_input, str):
        img = cv2.imread(image_input, cv2.IMREAD_UNCHANGED)
        if img is None:
            raise ValueError(f"Could not read image: {image_input}")
    else:
        img = image_input

    # Handle different channel counts robustly
    if len(img.shape) == 2:
        gray = img  # already grayscale
    elif img.shape[2] == 4:
        gray = cv2.cvtColor(img, cv2.COLOR_BGRA2GRAY)
    elif img.shape[2] == 3:
        gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
    else:
        raise ValueError(f"Unexpected image shape: {img.shape}")

    # Resize to consistent target size
    resized = cv2.resize(gray, target_size, interpolation=cv2.INTER_AREA)

    # Contrast normalization (helps with the lighter/washed-out manual images)
    normalized = cv2.normalize(resized, None, 0, 255, cv2.NORM_MINMAX)

    # Scale to 0-1 float32, add channel dimension for the model
    final = normalized.astype(np.float32) / 255.0
    final = np.expand_dims(final, axis=-1)  # shape: (96, 96, 1)

    return final
