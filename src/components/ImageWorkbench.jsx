import { Expand, ImagePlus, Upload } from 'lucide-react'

function DetectionBox({ detection, selected, onSelect }) {
  const [x, y, w, h] = detection.box
  return (
    <button
      className={`detection-box ${detection.color} ${selected ? 'selected' : ''}`}
      style={{ left: `${x}%`, top: `${y}%`, width: `${w}%`, height: `${h}%` }}
      onClick={() => onSelect(detection.id)}
      aria-label={`${detection.label}, confidence ${Math.round(detection.confidence * 100)} percent`}
      type="button"
    >
      <span>{detection.label.toLowerCase()} {detection.confidence.toFixed(2)}</span>
    </button>
  )
}

export function ImageWorkbench({ samples, selectedSample, imageUrl, detections, selectedDetection, onSelectSample, onSelectDetection, onUpload }) {
  const openFull = () => window.open(imageUrl, '_blank', 'noopener,noreferrer')

  return (
    <section className="analysis-layout" id="analyze">
      <aside className="source-panel panel">
        <div>
          <h1>Satellite &amp; debris analysis</h1>
          <p>Inspect detections, assess risk, and plan a safe route.</p>
        </div>
        <label className="drop-zone">
          <Upload size={30} strokeWidth={1.5} />
          <strong>Drop an image here</strong>
          <span>or choose a local file</span>
          <span className="button-like">Choose image</span>
          <small>JPG or PNG · maximum 10 MB</small>
          <input type="file" accept="image/png,image/jpeg" onChange={onUpload} />
        </label>
        <div className="sample-heading"><span>Sample images</span></div>
        <div className="samples">
          {samples.map((sample) => (
            <button
              type="button"
              className={selectedSample?.id === sample.id ? 'selected' : ''}
              key={sample.id}
              onClick={() => onSelectSample(sample)}
            >
              <img src={sample.file} alt="" />
              <span>{sample.name}</span>
            </button>
          ))}
        </div>
      </aside>

      <div className="viewer panel" aria-label="Detection image viewer">
        <div className="viewer-toolbar">
          <span>{selectedSample ? `Image: ${selectedSample.file.split('/').pop()}` : 'Uploaded image'}</span>
          <button type="button" onClick={openFull} aria-label="Open image in full size"><Expand size={17} /></button>
        </div>
        {imageUrl ? (
          <div className="image-stage">
            <img src={imageUrl} alt="Satellite surveillance input" />
            {detections.map((detection) => (
              <DetectionBox
                key={detection.id}
                detection={detection}
                selected={selectedDetection === detection.id}
                onSelect={onSelectDetection}
              />
            ))}
          </div>
        ) : (
          <div className="empty-viewer"><ImagePlus size={44} /><p>Select or upload an image to begin.</p></div>
        )}
      </div>
    </section>
  )
}
