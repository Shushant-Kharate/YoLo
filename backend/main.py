from __future__ import annotations

from functools import lru_cache
from pathlib import Path
from typing import Annotated

from fastapi import FastAPI, File, Form, HTTPException, UploadFile
from fastapi.middleware.cors import CORSMiddleware

ROOT = Path(__file__).resolve().parents[1]
MODEL_PATH = ROOT / "models" / "best.pt"
MAX_UPLOAD = 10 * 1024 * 1024

app = FastAPI(title="OrbitGuard API", version="1.0.0")
app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:5173", "http://127.0.0.1:5173"],
    allow_credentials=True,
    allow_methods=["*"] ,
    allow_headers=["*"],
)

DEMO_DETECTIONS = {
    "leo": [
        {"id": "leo-sat", "label": "Satellite", "confidence": 0.93, "color": "cyan", "box": [69, 26, 25, 43]},
        {"id": "leo-debris", "label": "Debris", "confidence": 0.87, "color": "amber", "box": [45, 14, 8, 10]},
    ],
    "station": [
        {"id": "station-main", "label": "Satellite", "confidence": 0.96, "color": "cyan", "box": [4, 5, 54, 66]},
        {"id": "station-debris-a", "label": "Debris", "confidence": 0.89, "color": "amber", "box": [75, 24, 9, 17]},
        {"id": "station-debris-b", "label": "Debris", "confidence": 0.81, "color": "amber", "box": [85, 51, 6, 12]},
    ],
    "field": [
        {"id": "field-sat", "label": "Satellite", "confidence": 0.91, "color": "cyan", "box": [78, 4, 14, 16]},
        {"id": "field-a", "label": "Debris", "confidence": 0.88, "color": "amber", "box": [6, 28, 10, 23]},
        {"id": "field-b", "label": "Debris", "confidence": 0.84, "color": "amber", "box": [55, 45, 6, 11]},
        {"id": "field-c", "label": "Debris", "confidence": 0.78, "color": "amber", "box": [87, 56, 8, 20]},
        {"id": "field-d", "label": "Debris", "confidence": 0.72, "color": "amber", "box": [31, 16, 5, 8]},
    ],
    "upload": [
        {"id": "upload-object", "label": "Unverified object", "confidence": 0.68, "color": "amber", "box": [38, 28, 22, 28]},
    ],
}


def model_available() -> bool:
    return MODEL_PATH.is_file()


@lru_cache(maxsize=1)
def get_model():
    if not model_available():
        return None
    try:
        from ultralytics import YOLO
    except ImportError as exc:
        raise RuntimeError("Install ultralytics to use models/best.pt") from exc
    return YOLO(str(MODEL_PATH))


@app.get("/api/health")
def health():
    return {
        "status": "ok",
        "mode": "live" if model_available() else "demo",
        "model": str(MODEL_PATH.name) if model_available() else None,
    }


@app.post("/api/analyze")
async def analyze(
    confidence: Annotated[float, Form()] = 0.5,
    scene: Annotated[str, Form()] = "upload",
    file: Annotated[UploadFile | None, File()] = None,
):
    if confidence < 0 or confidence > 1:
        raise HTTPException(status_code=400, detail="Confidence must be between 0 and 1")

    if not model_available():
        detections = [item for item in DEMO_DETECTIONS.get(scene, DEMO_DETECTIONS["upload"]) if item["confidence"] >= confidence]
        return {"mode": "demo", "detections": detections, "message": "Bundled demonstration detections. Add models/best.pt for live inference."}

    if file is None:
        raise HTTPException(status_code=400, detail="Upload an image for live inference")
    payload = await file.read(MAX_UPLOAD + 1)
    if len(payload) > MAX_UPLOAD:
        raise HTTPException(status_code=413, detail="Image exceeds 10 MB")
    if file.content_type not in {"image/jpeg", "image/png"}:
        raise HTTPException(status_code=415, detail="Only JPG and PNG images are supported")

    import io
    from PIL import Image, UnidentifiedImageError

    try:
        image = Image.open(io.BytesIO(payload)).convert("RGB")
    except UnidentifiedImageError as exc:
        raise HTTPException(status_code=400, detail="The uploaded file is not a valid image") from exc
    model = get_model()
    result = model.predict(image, conf=confidence, verbose=False)[0]
    width, height = image.size
    names = result.names
    detections = []
    for index, box in enumerate(result.boxes):
        x1, y1, x2, y2 = box.xyxy[0].tolist()
        label = str(names[int(box.cls[0])])
        detections.append({
            "id": f"live-{index}",
            "label": label.replace("_", " ").title(),
            "confidence": round(float(box.conf[0]), 4),
            "color": "amber" if "debris" in label.lower() else "cyan",
            "box": [
                round((x1 / width) * 100, 2),
                round((y1 / height) * 100, 2),
                round(((x2 - x1) / width) * 100, 2),
                round(((y2 - y1) / height) * 100, 2),
            ],
        })
    return {"mode": "live", "detections": detections, "message": "Live YOLO inference completed."}
