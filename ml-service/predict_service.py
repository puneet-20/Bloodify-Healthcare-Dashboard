from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
import base64
import io
import numpy as np
from PIL import Image
import tensorflow as tf

app = FastAPI()

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

MODEL = tf.keras.models.load_model("fingerprint_model.keras")

CLASS_NAMES = ["A+", "A-", "AB+", "AB-", "B+", "B-", "O+", "O-"]

IMG_SIZE = (96, 96)

class PredictRequest(BaseModel):
    image_base64: str

def preprocess_image(b64_string: str) -> np.ndarray:
    if "," in b64_string:
        b64_string = b64_string.split(",")[1]
    img_bytes = base64.b64decode(b64_string)
    img = Image.open(io.BytesIO(img_bytes)).convert("L")
    img = img.resize(IMG_SIZE)
    arr = np.array(img, dtype=np.float32) / 255.0
    arr = arr.reshape(1, IMG_SIZE[0], IMG_SIZE[1], 1)
    return arr

@app.post("/predict")
def predict(req: PredictRequest):
    try:
        arr = preprocess_image(req.image_base64)
        preds = MODEL.predict(arr)[0]
        top_idx = int(np.argmax(preds))
        return {
            "blood_group": CLASS_NAMES[top_idx],
            "confidence": float(preds[top_idx]),
            "all_probabilities": {CLASS_NAMES[i]: float(p) for i, p in enumerate(preds)}
        }
    except Exception as e:
        return {"error": str(e)}