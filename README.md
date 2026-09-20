# OrbitGuard

OrbitGuard is an interactive university demonstration of a satellite and space-debris analysis workflow. It combines an image-analysis surface, confidence filtering, explainable demo detections, optional custom YOLO inference, and a real A* path-planning visualizer.

> The bundled detections are a clearly labelled classroom simulation. Place your own trained weights at `models/best.pt` to enable live YOLO inference. This project is not an operational spacecraft-control system.

![OrbitGuard interface](docs/orbitguard-ui.png)

## Bundled sample images

| LEO scene | Station vicinity | Debris field |
|---|---|---|
| ![LEO scene](public/samples/leo-scene.png) | ![Station vicinity](public/samples/station-vicinity.png) | ![Debris field](public/samples/debris-field.png) |

These generated images are included for demonstration and interface testing. They are not labelled scientific observations and should not be used to measure model accuracy.

## What the application demonstrates

- Select one of three bundled satellite/debris scenes or upload a JPG/PNG.
- Run an analysis and inspect classes, confidence values, and bounding boxes.
- Change the confidence threshold and see low-confidence detections disappear.
- Click a detection in the image or inspector to highlight the same object.
- Add or remove debris obstacles on the planning grid.
- Watch the A* algorithm recalculate the shortest collision-free route.
- Open the in-app **Guide** for a concise assessment demonstration sequence.

## Technology

- **Frontend:** React, Vite, CSS, Lucide icons
- **API:** FastAPI and Uvicorn
- **Optional live inference:** Ultralytics YOLO with `models/best.pt`
- **Planning:** A* implemented in `src/lib/astar.js`

## Project structure

```text
YoLo/
├── backend/
│   └── main.py              # FastAPI and optional YOLO inference
├── models/
│   └── README.md            # Where to place best.pt
├── public/
│   └── samples/             # Bundled demonstration images
├── src/
│   ├── components/          # UI and planning components
│   ├── data/samples.js      # Sample metadata and demo detections
│   ├── lib/astar.js         # A* path-finding implementation
│   ├── App.jsx
│   └── styles.css
├── index.html
├── package.json
└── requirements.txt
```

## Prerequisites

- [Node.js 20 or newer](https://nodejs.org/)
- Python 3.10 to 3.12
- Git

## Start the whole program

### 1. Clone the repository

```bash
git clone https://github.com/Shushant-Kharate/YoLo.git
cd YoLo
```

### 2. Create a Python virtual environment

Windows PowerShell:

```powershell
python -m venv .venv
.\.venv\Scripts\Activate.ps1
```

macOS or Linux:

```bash
python3 -m venv .venv
source .venv/bin/activate
```

### 3. Install dependencies

```bash
pip install -r requirements.txt
npm install
```

### 4. Start the frontend and API together

```bash
npm start
```

Open [http://localhost:5173](http://localhost:5173).

The API runs at [http://localhost:8000](http://localhost:8000), and its interactive documentation is available at [http://localhost:8000/docs](http://localhost:8000/docs).

## Quick demo without a trained model

The project works immediately after installation:

1. Run `npm start`.
2. Select **LEO scene**, **Station vicinity**, or **Debris field**.
3. Click **Run analysis**.
4. Move the confidence slider.
5. Select a bounding box or an object in the inspector.
6. Scroll to **Orbital collision avoidance planning**.
7. Click cells to add or remove debris and observe A* recalculate the route.

In this mode, the interface displays **Demonstration model**. The sample bounding boxes are stored in `src/data/samples.js` and mirrored by the API. They are intentionally not presented as live model predictions.

## Enable live YOLO inference

1. Train or obtain a YOLO model with the satellite/debris classes required by your dataset.
2. Copy the final weight file to:

```text
models/best.pt
```

3. Restart `npm start`.
4. Upload a JPG or PNG and click **Run analysis**.

The model indicator changes to **Custom YOLO model**. The API reads the uploaded image in memory, runs YOLO, and returns normalized bounding boxes to the React interface.

### Important model note

Generic COCO weights do not contain a dedicated space-debris class. For meaningful results, train the model on an appropriately labelled spacecraft/debris dataset such as the dataset used for your coursework. The UI never claims that bundled sample detections are live predictions.

## Example YOLOv5 training command

The referenced paper trained YOLOv5 for 20 epochs with a batch size of 16. After preparing the dataset in YOLO format, an equivalent training command is:

```bash
python train.py \
  --img 640 \
  --batch 16 \
  --epochs 20 \
  --data /path/to/data.yaml \
  --weights yolov5s.pt \
  --name spark_yolov5
```

Copy `runs/train/spark_yolov5/weights/best.pt` into this repository's `models` directory.

## Run services separately

Frontend:

```bash
npm run dev
```

API:

```bash
python -m uvicorn backend.main:app --reload --port 8000
```

To use an API at another address, create a `.env` file before building the frontend:

```env
VITE_API_URL=http://localhost:8000
```

## Build for production

```bash
npm run build
npm run preview
```

The optimized frontend is generated in `dist/`. Host the frontend and API separately, then set `VITE_API_URL` to the deployed API address during the build.

## Demonstration script for LO 6.1 and LO 6.2

1. **Identify the input:** explain that the selected image represents optical surveillance data.
2. **Run analysis:** explain that YOLO predicts object class, confidence, and bounding-box coordinates.
3. **Adjust uncertainty:** move the threshold and explain why lower-confidence objects need verification.
4. **Connect the modules:** state that detections become potential obstacles after orbital tracking confirms their positions.
5. **Demonstrate planning:** click the grid to add debris and show how A* searches for a new shortest safe route.
6. **State the limitation:** image detection alone cannot determine a safe orbital manoeuvre; real deployment also requires multi-sensor tracking, orbital dynamics, validation, and authorized human control.

## Troubleshooting

### PowerShell blocks virtual-environment activation

Run the interpreter directly:

```powershell
.\.venv\Scripts\python.exe -m pip install -r requirements.txt
.\.venv\Scripts\python.exe -m uvicorn backend.main:app --reload --port 8000
```

### The interface stays in demonstration mode

- Confirm the file is named exactly `models/best.pt`.
- Restart the API after copying the model.
- Confirm `ultralytics` installed successfully.
- Open `http://localhost:8000/api/health` and check that `mode` is `live`.

### The API is unavailable

The frontend falls back to the bundled demonstration results. Check the terminal running the API, then open `http://localhost:8000/docs` to verify it started.

## Responsible-use statement

OrbitGuard is an educational prototype. Do not use its output for operational satellite manoeuvres. A real system must combine calibrated sensors, orbit determination, uncertainty modelling, conjunction assessment, human authorization, and independent safety checks.
