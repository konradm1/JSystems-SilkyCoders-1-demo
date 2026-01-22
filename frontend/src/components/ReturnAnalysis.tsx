import React, { useState, useRef } from 'react';
import { analyzeReturn, type AnalysisResult } from '../services/api';

const ReturnAnalysis: React.FC = () => {
    const [file, setFile] = useState<File | null>(null);
    const [description, setDescription] = useState('');
    const [isLoading, setIsLoading] = useState(false);
    const [result, setResult] = useState<AnalysisResult | null>(null);
    const [streamData, setStreamData] = useState('');
    const [error, setError] = useState<string | null>(null);
    const fileInputRef = useRef<HTMLInputElement>(null);

    const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        if (e.target.files && e.target.files[0]) {
            setFile(e.target.files[0]);
        }
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!file || !description) return;

        setIsLoading(true);
        setResult(null);
        setStreamData('');
        setError(null);

        try {
            const data = await analyzeReturn(file, description, (partial) => {
                setStreamData(partial);
            });
            setResult(data);
        } catch (err) {
            setError(err instanceof Error ? err.message : 'Analysis failed');
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="analysis-container">
            <div className="input-section">
                <h2>Submit Return for Analysis</h2>
                <form onSubmit={handleSubmit}>
                    <div className="form-group">
                        <label>Product Image</label>
                        <div
                            className="drop-zone"
                            onClick={() => fileInputRef.current?.click()}
                        >
                            {file ? file.name : 'Click to upload image'}
                        </div>
                        <input
                            type="file"
                            ref={fileInputRef}
                            onChange={handleFileChange}
                            accept="image/*"
                            hidden
                        />
                    </div>

                    <div className="form-group">
                        <label>Customer Description</label>
                        <textarea
                            value={description}
                            onChange={(e) => setDescription(e.target.value)}
                            placeholder="Why is the customer returning this?"
                            rows={4}
                        />
                    </div>

                    <button
                        type="submit"
                        disabled={!file || !description || isLoading}
                        className="submit-btn"
                    >
                        {isLoading ? 'Analyzing...' : 'Analyze Return'}
                    </button>
                </form>
            </div>

            <div className="result-section">
                {isLoading && !result && (
                    <div className="streaming-log">
                        <h3>Standard Output Stream</h3>
                        <pre>{streamData}</pre>
                    </div>
                )}

                {result && (
                    <div className={`verdict-card ${result.verdict.toLowerCase()}`}>
                        <div className="verdict-header">
                            <h3>{result.verdict}</h3>
                            <span className="confidence">
                                {(result.confidence * 100).toFixed(1)}% Confidence
                            </span>
                        </div>

                        <div className="verdict-body">
                            <div className="info-block">
                                <label>Reasoning</label>
                                <p>{result.reasoning}</p>
                            </div>

                            <div className="info-block">
                                <label>Suggested Action</label>
                                <p>{result.suggestedAction}</p>
                            </div>
                        </div>
                    </div>
                )}

                {error && <div className="error-message">{error}</div>}
            </div>
        </div>
    );
};

export default ReturnAnalysis;
