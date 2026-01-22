import ReturnAnalysis from './components/ReturnAnalysis'
import './App.css'

function App() {
  return (
    <div className="app-container">
      <header className="app-header">
        <h1>Sinsay Returns AI Agent</h1>
        <p>Upload a product image and description for instant policy validation</p>
      </header>
      <main>
        <ReturnAnalysis />
      </main>
    </div>
  )
}

export default App
