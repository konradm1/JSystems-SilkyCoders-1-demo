export interface AnalysisResult {
  verdict: string;
  confidence: number;
  reasoning: string;
  suggestedAction: string;
}

export const analyzeReturn = async (
  file: File,
  description: string,
  onStreamUpdate: (partial: string) => void
): Promise<AnalysisResult> => {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('description', description);

  const response = await fetch('/api/returns/analyze', {
    method: 'POST',
    body: formData,
  });

  if (!response.body) {
    throw new Error('No response body');
  }

  const reader = response.body.getReader();
  const decoder = new TextDecoder();
  let accumulatedText = '';

  while (true) {
    const { done, value } = await reader.read();
    if (done) break;

    const chunk = decoder.decode(value, { stream: true });

    // Parse SSE format
    const lines = chunk.split('\n');
    for (const line of lines) {
      if (line.trim().startsWith('data:')) {
        const content = line.substring(5).trim();
        // Skip empty keep-alive messages if any, though Spring AI usually sends content
        if (content) {
          accumulatedText += content;
          onStreamUpdate(accumulatedText);
        }
      }
    }
  }

  try {
    // Attempt to extract JSON object from potentially chatty response
    const jsonMatch = accumulatedText.match(/\{[\s\S]*\}/);
    if (!jsonMatch) {
      throw new Error('No JSON object found in response');
    }
    return JSON.parse(jsonMatch[0]) as AnalysisResult;
  } catch (e) {
    console.error('Failed to parse final JSON. Raw text:', accumulatedText);
    throw new Error('Invalid response format: ' + (e instanceof Error ? e.message : String(e)));
  }
};
