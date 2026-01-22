import { useState, ChangeEvent } from 'react'
import { SinsayChat } from './components/Chat'
import './App.css'

function App() {
  const [step, setStep] = useState<'form' | 'chat'>('form');
  const [formData, setFormData] = useState({
    orderId: '',
    intent: 'return',
    description: '',
    image: null as File | null
  });

  const handleImageChange = (e: ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      setFormData({ ...formData, image: e.target.files[0] });
    }
  };

  const startChat = () => {
    // Basic validation
    if (!formData.orderId || !formData.description) {
      alert("Please fill in Order ID and Description");
      return;
    }
    setStep('chat');
  };

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col items-center p-4">
      <header className="mb-8 mt-4">
        <h1 className="text-3xl font-bold tracking-tight text-black">Sinsay Verification PoC</h1>
      </header>

      <main className="w-full max-w-2xl bg-white p-6 rounded-lg shadow-md">
        {step === 'form' ? (
          <div className="space-y-6">
            <h2 className="text-xl font-semibold">Step 1: Application Details</h2>

            <div>
              <label className="block text-sm font-medium mb-1">I want to make a:</label>
              <div className="flex gap-4">
                <label className="flex items-center gap-2 cursor-pointer">
                  <input
                    type="radio"
                    name="intent"
                    value="return"
                    checked={formData.intent === 'return'}
                    onChange={e => setFormData({ ...formData, intent: e.target.value })}
                  />
                  Return (Zwrot)
                </label>
                <label className="flex items-center gap-2 cursor-pointer">
                  <input
                    type="radio"
                    name="intent"
                    value="complaint"
                    checked={formData.intent === 'complaint'}
                    onChange={e => setFormData({ ...formData, intent: e.target.value })}
                  />
                  Complaint (Reklamacja)
                </label>
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium mb-1">Order Number</label>
              <input
                className="w-full border p-2 rounded"
                value={formData.orderId}
                onChange={e => setFormData({ ...formData, orderId: e.target.value })}
                placeholder="e.g. PLUA12345678"
              />
            </div>

            <div>
              <label className="block text-sm font-medium mb-1">Damage/Issue Description</label>
              <textarea
                className="w-full border p-2 rounded h-24"
                value={formData.description}
                onChange={e => setFormData({ ...formData, description: e.target.value })}
                placeholder="Describe the issue..."
              />
            </div>

            <div>
              <label className="block text-sm font-medium mb-1">Upload Photo</label>
              <input
                type="file"
                accept="image/*"
                onChange={handleImageChange}
                className="block w-full text-sm text-slate-500
                         file:mr-4 file:py-2 file:px-4
                         file:rounded-full file:border-0
                         file:text-sm file:font-semibold
                         file:bg-violet-50 file:text-violet-700
                         hover:file:bg-violet-100"
              />
            </div>

            <button
              onClick={startChat}
              className="w-full bg-black text-white py-2 rounded hover:bg-gray-800 transition-colors"
            >
              Proceed to Verification
            </button>
          </div>
        ) : (
          <SinsayChat
            intent={formData.intent}
            orderId={formData.orderId}
            description={formData.description}
            image={formData.image}
          />
        )}
      </main>
    </div>
  )
}

export default App
