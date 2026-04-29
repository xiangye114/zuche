
import request from '@/utils/request'

export const chatApi = {
  createSession: () => request.post('/chat/sessions'),
  getSessions: () => request.get('/chat/sessions'),
  getSession: (sessionId) => request.get(`/chat/sessions/${sessionId}`),
  send: (data) => request.post('/chat/send', data),
  deleteSession: (sessionId) => request.delete(`/chat/sessions/${sessionId}`),
  getFaq: () => request.get('/chat/faq'),
  stream: (sessionId, content, useRag = false) => {
    const token = localStorage.getItem('token')
    return new EventSource(`/chat/stream?sessionId=${sessionId}&content=${encodeURIComponent(content)}&useRag=${useRag}&token=${token}`)
  }
}
