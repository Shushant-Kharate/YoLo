import { AlertTriangle, CheckCircle2, ChevronRight, LoaderCircle, Play, ServerCog } from 'lucide-react'

export function Inspector({ mode, threshold, onThreshold, detections, selectedDetection, onSelectDetection, analyzing, onAnalyze }) {
  const visible = detections.filter((item) => item.confidence >= threshold)
  return (
    <aside className="inspector panel">
      <section>
        <h2>Model status</h2>
        <div className="model-status">
          <span className={`status-dot ${mode === 'live' ? 'live' : ''}`} />
          <div><strong>{mode === 'live' ? 'Custom YOLO model' : 'Demonstration model'}</strong><small>{mode === 'live' ? 'Ready for inference' : 'Safe sample simulation'}</small></div>
          <ServerCog size={18} />
        </div>
      </section>
      <section>
        <div className="slider-label"><label htmlFor="confidence">Confidence threshold</label><strong>{threshold.toFixed(2)}</strong></div>
        <input id="confidence" type="range" min="0.25" max="0.95" step="0.01" value={threshold} onChange={(event) => onThreshold(Number(event.target.value))} />
        <div className="range-scale"><span>0.25</span><span>0.60</span><span>0.95</span></div>
      </section>
      <button className="primary-action" type="button" onClick={onAnalyze} disabled={analyzing}>
        {analyzing ? <LoaderCircle className="spin" size={18} /> : <Play size={18} fill="currentColor" />}
        {analyzing ? 'Analyzing image' : 'Run analysis'}
      </button>
      <section className="object-section">
        <div className="object-title"><h2>Detected objects</h2><span>{visible.length}</span></div>
        <div className="object-list">
          {visible.length ? visible.map((item) => (
            <button type="button" key={item.id} onClick={() => onSelectDetection(item.id)} className={selectedDetection === item.id ? 'selected' : ''}>
              <span className={`object-mark ${item.color}`} />
              <span><strong>{item.label}</strong><small>{item.label === 'Satellite' ? 'Tracked spacecraft' : 'Potential collision object'}</small></span>
              <b>{item.confidence.toFixed(2)}</b>
              <ChevronRight size={17} />
            </button>
          )) : <div className="empty-list"><CheckCircle2 size={20} /><span>No objects above the current threshold.</span></div>}
        </div>
      </section>
      <div className="notice">
        <AlertTriangle size={17} />
        <p>Image detections support a classroom demonstration. Real orbital decisions require multi-sensor tracking and human approval.</p>
      </div>
    </aside>
  )
}
