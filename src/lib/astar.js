const key = ([x, y]) => `${x},${y}`

const heuristic = ([x, y], [gx, gy]) => Math.abs(gx - x) + Math.abs(gy - y)

export function findPath(width, height, start, goal, obstacles) {
  const blocked = new Set(obstacles.map(key))
  const open = [{ point: start, g: 0, f: heuristic(start, goal) }]
  const cameFrom = new Map()
  const best = new Map([[key(start), 0]])

  while (open.length) {
    open.sort((a, b) => a.f - b.f)
    const current = open.shift()
    const [x, y] = current.point
    if (x === goal[0] && y === goal[1]) {
      const path = [current.point]
      let cursor = key(current.point)
      while (cameFrom.has(cursor)) {
        const previous = cameFrom.get(cursor)
        path.unshift(previous)
        cursor = key(previous)
      }
      return path
    }

    for (const [dx, dy] of [[1, 0], [-1, 0], [0, 1], [0, -1]]) {
      const next = [x + dx, y + dy]
      const nextKey = key(next)
      if (next[0] < 0 || next[1] < 0 || next[0] >= width || next[1] >= height || blocked.has(nextKey)) continue
      const nextG = current.g + 1
      if (nextG >= (best.get(nextKey) ?? Infinity)) continue
      best.set(nextKey, nextG)
      cameFrom.set(nextKey, current.point)
      open.push({ point: next, g: nextG, f: nextG + heuristic(next, goal) })
    }
  }
  return []
}
