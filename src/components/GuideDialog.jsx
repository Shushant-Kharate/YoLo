import { BookOpen, Check, X } from 'lucide-react'

export function GuideDialog({ open, onClose }) {
  if (!open) return null
  return (
    <div className="dialog-backdrop" role="presentation" onMouseDown={onClose}>
      <section className="guide-dialog" role="dialog" aria-modal="true" aria-labelledby="guide-title" onMouseDown={(event) => event.stopPropagation()}>
        <button className="dialog-close" type="button" onClick={onClose} aria-label="Close guide"><X /></button>
        <BookOpen size={30} className="cyan-text" />
        <h2 id="guide-title">How to demonstrate OrbitGuard</h2>
        <ol>
          <li><span>1</span><div><strong>Choose an image</strong><p>Select one of the bundled scenes or upload a JPG or PNG.</p></div></li>
          <li><span>2</span><div><strong>Run the analysis</strong><p>The Java API uses a custom YOLO ONNX model when <code>models/best.onnx</code> exists. Otherwise it returns clearly labelled sample detections.</p></div></li>
          <li><span>3</span><div><strong>Inspect confidence</strong><p>Move the threshold slider and select a detection to connect the image evidence with the inspector.</p></div></li>
          <li><span>4</span><div><strong>Plan around debris</strong><p>Click the A* grid to add or remove obstacles and watch the shortest route recalculate.</p></div></li>
        </ol>
        <div className="guide-note"><Check size={18} /><p>For assessment, explain that a single image provides detection evidence, while real collision avoidance also needs calibrated orbital tracking data.</p></div>
      </section>
    </div>
  )
}
