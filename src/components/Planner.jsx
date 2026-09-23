import { RotateCcw, Route, Satellite, Sparkles } from 'lucide-react'
import { useEffect, useMemo, useState } from 'react'
import { API } from '../config'

const WIDTH = 24
const HEIGHT = 10
const START = [2, 5]
const GOAL = [21, 5]
const initialObstacles = [[9, 3], [10, 3], [9, 4], [14, 5], [14, 6], [15, 6]]

export function Planner() {
  const [obstacles, setObstacles] = useState(initialObstacles)
  const [path, setPath] = useState([])
  const [planning, setPlanning] = useState(true)
  const [planError, setPlanError] = useState(false)
  const toPoint = ([x, y]) => `${((x + .5) / WIDTH) * 100},${((y + .5) / HEIGHT) * 100}`
  const obstacleSet = useMemo(() => new Set(obstacles.map(([x, y]) => `${x},${y}`)), [obstacles])

  useEffect(() => {
    const controller = new AbortController()
    setPlanning(true)
    setPlanError(false)
    fetch(`${API}/api/plan`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        width: WIDTH,
        height: HEIGHT,
        start: { x: START[0], y: START[1] },
        goal: { x: GOAL[0], y: GOAL[1] },
        obstacles: obstacles.map(([x, y]) => ({ x, y })),
      }),
      signal: controller.signal,
    })
      .then((response) => {
        if (!response.ok) throw new Error('Planning service unavailable')
        return response.json()
      })
      .then((result) => setPath(result.path.map(({ x, y }) => [x, y])))
      .catch((error) => {
        if (error.name !== 'AbortError') {
          setPath([])
          setPlanError(true)
        }
      })
      .finally(() => {
        if (!controller.signal.aborted) setPlanning(false)
      })

    return () => controller.abort()
  }, [obstacles])

  const toggleCell = (event) => {
    const box = event.currentTarget.getBoundingClientRect()
    const x = Math.min(WIDTH - 1, Math.max(0, Math.floor(((event.clientX - box.left) / box.width) * WIDTH)))
    const y = Math.min(HEIGHT - 1, Math.max(0, Math.floor(((event.clientY - box.top) / box.height) * HEIGHT)))
    if ((x === START[0] && y === START[1]) || (x === GOAL[0] && y === GOAL[1])) return
    const cell = `${x},${y}`
    setObstacles((current) => obstacleSet.has(cell) ? current.filter(([ox, oy]) => `${ox},${oy}` !== cell) : [...current, [x, y]])
  }

  return (
    <section className="planner panel" id="planning">
      <div className="planner-heading">
        <div><h2>Orbital collision avoidance planning</h2><p>Java A* searches for a safe route around detected debris. Click the grid to edit obstacles.</p></div>
        <div className={`path-status ${!planning && !path.length ? 'error' : ''}`} aria-live="polite"><Route size={18} />{planning ? 'Java A* planning…' : planError ? 'Java planner unavailable' : path.length ? `${path.length - 1} steps · route clear` : 'No safe route'}</div>
      </div>
      <div className="planner-layout">
        <aside className="legend">
          <div><Satellite size={19} className="cyan-text" /><span>Satellite start</span></div>
          <div><span className="legend-dot" /><span>Debris obstacle</span></div>
          <div><span className="legend-line" /><span>A* planned path</span></div>
          <div><Sparkles size={19} className="green-text" /><span>Safe waypoint</span></div>
          <hr />
          <p><strong>{obstacles.length}</strong> blocked cells</p>
          <p><strong>{Math.max(0, path.length - 1)}</strong> Java A* path cost</p>
          <button type="button" onClick={() => setObstacles(initialObstacles)}><RotateCcw size={17} /> Reset plan</button>
        </aside>
        <div className="orbit-grid" onClick={toggleCell} role="button" tabIndex="0" aria-label="Interactive A star planning grid. Click to add or remove debris obstacles.">
          <svg viewBox="0 0 100 100" preserveAspectRatio="none" aria-hidden="true">
            <defs>
              <pattern id="smallGrid" width={100 / WIDTH} height={100 / HEIGHT} patternUnits="userSpaceOnUse">
                <path d={`M ${100 / WIDTH} 0 L 0 0 0 ${100 / HEIGHT}`} fill="none" stroke="rgba(131,166,181,.18)" strokeWidth=".18" />
              </pattern>
            </defs>
            <rect width="100" height="100" fill="url(#smallGrid)" />
            {path.length > 0 && <polyline className="route-path" points={path.map(toPoint).join(' ')} fill="none" vectorEffect="non-scaling-stroke" />}
          </svg>
          {obstacles.map(([x, y]) => <span key={`${x},${y}`} className="grid-obstacle" style={{ left: `${((x + .5) / WIDTH) * 100}%`, top: `${((y + .5) / HEIGHT) * 100}%` }} />)}
          <span className="grid-start" style={{ left: `${((START[0] + .5) / WIDTH) * 100}%`, top: `${((START[1] + .5) / HEIGHT) * 100}%` }}><Satellite size={24} /></span>
          <span className="grid-goal" style={{ left: `${((GOAL[0] + .5) / WIDTH) * 100}%`, top: `${((GOAL[1] + .5) / HEIGHT) * 100}%` }}><Sparkles size={24} /></span>
          <span className="axis-label x">Relative X position</span>
          <span className="axis-label y">Relative Y</span>
        </div>
      </div>
    </section>
  )
}
