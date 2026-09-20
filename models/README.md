# Model directory

Place your trained YOLO weights here using this exact name:

```text
models/best.pt
```

OrbitGuard automatically changes from **Demonstration model** to **Custom YOLO model** after the backend restarts and detects this file.

The weights should come from a model trained on the same class labels used by your SPARK/space-object dataset. Do not use the interface output for real spacecraft operations without calibrated orbital tracking and independent validation.
