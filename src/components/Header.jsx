import { FolderGit2, Orbit } from 'lucide-react'

export function Header({ onGuide }) {
  return (
    <header className="topbar">
      <a className="brand" href="#analyze" aria-label="OrbitGuard home">
        <Orbit size={28} strokeWidth={1.7} />
        <span>Orbit<span>Guard</span></span>
      </a>
      <nav aria-label="Primary navigation">
        <a className="active" href="#analyze">Analyze</a>
        <a href="#planning">Planning</a>
        <button type="button" onClick={onGuide}>Guide</button>
      </nav>
      <a className="repo-link" href="https://github.com/Shushant-Kharate/YoLo" target="_blank" rel="noreferrer">
        <FolderGit2 size={17} /> Repository
      </a>
    </header>
  )
}
