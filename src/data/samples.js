export const samples = [
  {
    id: 'leo',
    name: 'LEO scene',
    file: '/samples/leo-scene.png',
    description: 'Satellite and isolated fragment above Earth',
    detections: [
      { id: 'leo-sat', label: 'Satellite', confidence: 0.93, color: 'cyan', box: [69, 26, 25, 43] },
      { id: 'leo-debris', label: 'Debris', confidence: 0.87, color: 'amber', box: [45, 14, 8, 10] },
    ],
  },
  {
    id: 'station',
    name: 'Station vicinity',
    file: '/samples/station-vicinity.png',
    description: 'Orbital station with nearby objects',
    detections: [
      { id: 'station-main', label: 'Satellite', confidence: 0.96, color: 'cyan', box: [4, 5, 54, 66] },
      { id: 'station-debris-a', label: 'Debris', confidence: 0.89, color: 'amber', box: [75, 24, 9, 17] },
      { id: 'station-debris-b', label: 'Debris', confidence: 0.81, color: 'amber', box: [85, 51, 6, 12] },
    ],
  },
  {
    id: 'field',
    name: 'Debris field',
    file: '/samples/debris-field.png',
    description: 'Sparse multi-object surveillance scene',
    detections: [
      { id: 'field-sat', label: 'Satellite', confidence: 0.91, color: 'cyan', box: [78, 4, 14, 16] },
      { id: 'field-a', label: 'Debris', confidence: 0.88, color: 'amber', box: [6, 28, 10, 23] },
      { id: 'field-b', label: 'Debris', confidence: 0.84, color: 'amber', box: [55, 45, 6, 11] },
      { id: 'field-c', label: 'Debris', confidence: 0.78, color: 'amber', box: [87, 56, 8, 20] },
      { id: 'field-d', label: 'Debris', confidence: 0.72, color: 'amber', box: [31, 16, 5, 8] },
    ],
  },
]

export const uploadedFallback = [
  { id: 'upload-object', label: 'Unverified object', confidence: 0.68, color: 'amber', box: [38, 28, 22, 28] },
]
