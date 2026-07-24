export function createSSEConnection(url, onMessage, onError, onDone) {
  const token = localStorage.getItem('qkc_token')
  const fullUrl = `/api/ai/chat/stream?${url}`

  fetch(fullUrl, {
    headers: { 'Authorization': `Bearer ${token}` }
  }).then(response => {
    if (!response.ok) throw new Error('SSE connection failed')
    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    let buffer = ''

    function read() {
      reader.read().then(({ done, value }) => {
        if (done) { onDone?.(); return }
        buffer += decoder.decode(value, { stream: true })
        const lines = buffer.split('\n')
        buffer = lines.pop() || ''
        for (const line of lines) {
          if (!line.trim() || line.startsWith(':')) continue  // skip empty/comment
          // 处理双重 "data:data:" 前缀（Spring AI M6 bug）
          let content = line
          while (content.startsWith('data:')) {
            content = content.substring(5).trim()
          }
          if (content) onMessage(content)
        }
        read()
      }).catch(e => onError?.(e))
    }
    read()
  }).catch(e => onError?.(e))
}
