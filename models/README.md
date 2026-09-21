# Model directory

Place an exported YOLOv5 ONNX model here using this exact name:

```text
models/best.onnx
```

Add one class name per line to:

```text
models/classes.txt
```

Example:

```text
satellite
debris
```

OrbitGuard automatically changes from **Demonstration model** to **Custom YOLO model** after the Java backend restarts and detects `best.onnx`. Java performs image preprocessing, ONNX Runtime inference, confidence filtering, non-maximum suppression, and response serialization.

The ONNX file should come from a YOLOv5 model trained on the same class labels used by the SPARK/space-object dataset. Do not use this educational interface for real spacecraft operations without calibrated orbital tracking and independent validation.
