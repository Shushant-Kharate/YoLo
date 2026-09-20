import { useEffect, useMemo, useState } from 'react'
import { Header } from './components/Header'
import { ImageWorkbench } from './components/ImageWorkbench'
import { Inspector } from './components/Inspector'
import { Planner } from './components/Planner'
import { GuideDialog } from './components/GuideDialog'
import { samples, uploadedFallback } from './data/samples'

const API = import.meta.env.VITE_API_URL || 'http://localhost:8000'

export default function App() {
  const [sample, setSample] = useState(samples[0])
  const [imageUrl, setImageUrl] = useState(samples[0].file)
  const [upload, setUpload] = useState(null)
  const [detections, setDetections] = useState(samples[0].detections)
  const [selectedDetection, setSelectedDetection] = useState(samples[0].detections[0].id)
  const [threshold, setThreshold] = useState(0.5)
  const [mode, setMode] = useState('demo')
  const [analyzing, setAnalyzing] = useState(false)
  const [guideOpen, setGuideOpen] = useState(false)

  useEffect(() => {
    fetch(`${API}/api/health`).then((response) => response.json()).then((data) => setMode(data.mode)).catch(() => setMode('demo'))
  }, [])

  useEffect(() => () => {
    if (upload?.preview) URL.revokeObjectURL(upload.preview)
  }, [upload])

  const visibleDetections = useMemo(() => detections.filter((d) => d.confidence >= threshold), [detections, threshold])

  const chooseSample = (nextSample) => {
    if (upload?.preview) URL.revokeObjectURL(upload.preview)
    setUpload(null)
    setSample(nextSample)
    setImageUrl(nextSample.file)
    setDetections(nextSample.detections)
    setSelectedDetection(nextSample.detections[0]?.id ?? null)
  }

  const uploadImage = (event) => {
    const file = event.target.files?.[0]
    if (!file || file.size > 10 * 1024 * 1024) return
    if (upload?.preview) URL.revokeObjectURL(upload.preview)
    const preview = URL.createObjectURL(file)
    setUpload({ file, preview })
    setSample(null)
    setImageUrl(preview)
    setDetections([])
    setSelectedDetection(null)
  }

  const analyze = async () => {
    setAnalyzing(true)
    try {
      const form = new FormData()
      form.append('confidence', String(threshold))
      form.append('scene', sample?.id || 'upload')
      if (upload?.file) {
        form.append('file', upload.file)
      } else if (sample) {
        const sampleBlob = await fetch(sample.file).then((response) => response.blob())
        form.append('file', sampleBlob, sample.file.split('/').pop())
      }
      const response = await fetch(`${API}/api/analyze`, { method: 'POST', body: form })
      if (!response.ok) throw new Error('Analysis service unavailable')
      const result = await response.json()
      setMode(result.mode)
      setDetections(result.detections)
      setSelectedDetection(result.detections[0]?.id ?? null)
    } catch {
      const fallback = sample?.detections || uploadedFallback
      setMode('demo')
      setDetections(fallback)
      setSelectedDetection(fallback[0]?.id ?? null)
    } finally {
      window.setTimeout(() => setAnalyzing(false), 450)
    }
  }

  return (
    <>
      <Header onGuide={() => setGuideOpen(true)} />
      <main>
        <div className="upper-grid">
          <ImageWorkbench
            samples={samples}
            selectedSample={sample}
            imageUrl={imageUrl}
            detections={visibleDetections}
            selectedDetection={selectedDetection}
            onSelectSample={chooseSample}
            onSelectDetection={setSelectedDetection}
            onUpload={uploadImage}
          />
          <Inspector
            mode={mode}
            threshold={threshold}
            onThreshold={setThreshold}
            detections={detections}
            selectedDetection={selectedDetection}
            onSelectDetection={setSelectedDetection}
            analyzing={analyzing}
            onAnalyze={analyze}
          />
        </div>
        <Planner />
      </main>
      <footer><span>{mode === 'live' ? 'Live custom-model inference is enabled.' : <>Demo mode is clearly labelled. Add <code>models/best.pt</code> for live YOLO inference.</>}</span><span>OrbitGuard v1.0 · University AI demonstration</span></footer>
      <GuideDialog open={guideOpen} onClose={() => setGuideOpen(false)} />
    </>
  )
}
