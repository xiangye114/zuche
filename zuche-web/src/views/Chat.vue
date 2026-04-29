
<template>
  <div class="chat-page">
    <div class="chat-container">
      <div class="sidebar">
        <div class="sidebar-header">
          <h3>我的对话</h3>
          <el-button type="primary" size="small" @click="createSession">
            <el-icon><Plus /></el-icon>
            新对话
          </el-button>
        </div>
        <div class="session-list">
          <div
            v-for="session in sessions"
            :key="session.sessionId"
            :class="['session-item', { active: currentSession?.sessionId === session.sessionId }]"
            @click="selectSession(session)"
          >
            <div class="session-title">{{ session.title }}</div>
            <div class="session-time">{{ formatTime(session.updateTime) }}</div>
          </div>
          <el-empty v-if="sessions.length === 0" description="暂无对话" />
        </div>
      </div>
      
      <div class="chat-main">
        <div v-if="!currentSession" class="welcome-screen">
          <el-empty description="请选择或创建一个对话">
            <el-button type="primary" @click="createSession">开始对话</el-button>
          </el-empty>
        </div>
        
        <template v-else>
          <div class="chat-header">
            <h3>{{ currentSession.title }}</h3>
            <div class="header-actions">
              <el-checkbox v-model="useRag" label="使用知识库" />
              <el-button type="danger" text @click="deleteCurrentSession">
                <el-icon><Delete /></el-icon>
                删除对话
              </el-button>
            </div>
          </div>
          
          <div class="message-list" ref="messageListRef">
            <div
              v-for="msg in messages"
              :key="msg.id"
              :class="['message', msg.role]"
            >
              <div class="message-avatar">
                <el-avatar v-if="msg.role === 'user'" :size="36">
                  {{ userStore.user?.username?.charAt(0)?.toUpperCase() }}
                </el-avatar>
                <el-avatar v-else :size="36" class="ai-avatar">AI</el-avatar>
              </div>
              <div class="message-content">
                <div class="message-text">{{ msg.content }}</div>
                <div class="message-time">{{ formatTime(msg.timestamp) }}</div>
              </div>
            </div>
            <div v-if="loading" class="message assistant">
              <div class="message-avatar">
                <el-avatar :size="36" class="ai-avatar">AI</el-avatar>
              </div>
              <div class="message-content">
                <div class="message-text typing">{{ streamingResponse || '正在输入...' }}</div>
              </div>
            </div>
          </div>
          
          <div class="chat-input">
            <el-input
              v-model="inputMessage"
              type="textarea"
              :rows="3"
              placeholder="输入您的问题..."
              @keydown.enter.ctrl="sendMessage"
            />
            <div class="input-actions">
              <el-button type="primary" @click="sendMessage" :loading="loading" :disabled="!inputMessage.trim()">
                发送
              </el-button>
              <el-button @click="sendMessageStream" :loading="loading" :disabled="!inputMessage.trim()">
                流式发送
              </el-button>
            </div>
          </div>
        </template>
      </div>
      
      <div class="faq-panel">
        <h4>常见问题</h4>
        <div class="faq-list">
          <div
            v-for="faq in faqList"
            :key="faq.id"
            class="faq-item"
            @click="quickAsk(faq.question)"
          >
            {{ faq.question }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useUserStore } from '@/store/user'
import { chatApi } from '@/api/chat'
import { ElMessage, ElMessageBox } from 'element-plus'

const userStore = useUserStore()

const sessions = ref([])
const currentSession = ref(null)
const messages = ref([])
const inputMessage = ref('')
const loading = ref(false)
const faqList = ref([])
const messageListRef = ref(null)
const useRag = ref(false)
const streamingResponse = ref('')

const loadSessions = async () => {
  try {
    const res = await chatApi.getSessions()
    sessions.value = res || []
  } catch (error) {
    console.error(error)
  }
}

const loadFaq = async () => {
  try {
    const res = await chatApi.getFaq()
    faqList.value = res || []
  } catch (error) {
    console.error(error)
  }
}

const createSession = async () => {
  try {
    const res = await chatApi.createSession()
    sessions.value.unshift(res)
    currentSession.value = res
    messages.value = []
  } catch (error) {
    ElMessage.error('创建对话失败')
  }
}

const selectSession = async (session) => {
  currentSession.value = session
  try {
    const res = await chatApi.getSession(session.sessionId)
    messages.value = res?.messages || []
    scrollToBottom()
  } catch (error) {
    console.error(error)
  }
}

const deleteCurrentSession = async () => {
  try {
    await ElMessageBox.confirm('确定要删除这个对话吗？', '提示', {
      type: 'warning'
    })
    await chatApi.deleteSession(currentSession.value.sessionId)
    sessions.value = sessions.value.filter(s => s.sessionId !== currentSession.value.sessionId)
    currentSession.value = null
    messages.value = []
    ElMessage.success('删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const sendMessage = async () => {
  if (!inputMessage.value.trim() || loading.value) return
  
  const content = inputMessage.value.trim()
  inputMessage.value = ''
  
  const messageId = Date.now()
  messages.value.push({
    id: messageId,
    role: 'user',
    content: content,
    timestamp: Date.now()
  })
  
  scrollToBottom()
  loading.value = true
  
  try {
    const res = await chatApi.send({
      sessionId: currentSession.value.sessionId,
      content: content,
      useRag: useRag.value
    })
    messages.value.push({
      id: Date.now(),
      role: 'assistant',
      content: res.content,
      timestamp: res.timestamp
    })
    scrollToBottom()
    
    updateSessionTitle(content)
  } catch (error) {
    ElMessage.error('发送失败')
  } finally {
    loading.value = false
  }
}

const sendMessageStream = () => {
  if (!inputMessage.value.trim() || loading.value) return
  
  const content = inputMessage.value.trim()
  inputMessage.value = ''
  
  const messageId = Date.now()
  messages.value.push({
    id: messageId,
    role: 'user',
    content: content,
    timestamp: Date.now()
  })
  
  scrollToBottom()
  loading.value = true
  streamingResponse.value = ''
  
  try {
    const eventSource = chatApi.stream(currentSession.value.sessionId, content, useRag.value)
    
    eventSource.onmessage = (event) => {
      streamingResponse.value += event.data
      scrollToBottom()
    }
    
    eventSource.onerror = (error) => {
      console.error('SSE error:', error)
      eventSource.close()
      loading.value = false
      streamingResponse.value = ''
      ElMessage.error('连接错误')
    }
    
    eventSource.addEventListener('close', () => {
      eventSource.close()
      if (streamingResponse.value) {
        messages.value.push({
          id: Date.now(),
          role: 'assistant',
          content: streamingResponse.value,
          timestamp: Date.now()
        })
        updateSessionTitle(content)
      }
      streamingResponse.value = ''
      loading.value = false
      scrollToBottom()
    })
  } catch (error) {
    console.error(error)
    loading.value = false
    streamingResponse.value = ''
    ElMessage.error('发送失败')
  }
}

const updateSessionTitle = (content) => {
  const session = sessions.value.find(s => s.sessionId === currentSession.value.sessionId)
  if (session && session.title === '新对话') {
    session.title = content.length > 20 ? content.substring(0, 20) + '...' : content
    currentSession.value.title = session.title
  }
}

const quickAsk = async (question) => {
  if (!currentSession.value) {
    await createSession()
  }
  inputMessage.value = question
  sendMessage()
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messageListRef.value) {
      messageListRef.value.scrollTop = messageListRef.value.scrollHeight
    }
  })
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  return date.toLocaleDateString()
}

onMounted(() => {
  loadSessions()
  loadFaq()
})
</script>

<style lang="scss" scoped>
.chat-page {
  height: calc(100vh - 120px);
  padding: 20px;
  
  .chat-container {
    display: flex;
    height: 100%;
    background: #fff;
    border-radius: 8px;
    overflow: hidden;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  }
  
  .sidebar {
    width: 250px;
    border-right: 1px solid #ebeef5;
    display: flex;
    flex-direction: column;
    
    .sidebar-header {
      padding: 16px;
      border-bottom: 1px solid #ebeef5;
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      h3 {
        margin: 0;
        font-size: 16px;
      }
    }
    
    .session-list {
      flex: 1;
      overflow-y: auto;
      
      .session-item {
        padding: 12px 16px;
        cursor: pointer;
        border-bottom: 1px solid #f5f5f5;
        
        &:hover {
          background: #f5f7fa;
        }
        
        &.active {
          background: #ecf5ff;
        }
        
        .session-title {
          font-size: 14px;
          color: #303133;
          margin-bottom: 4px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
        
        .session-time {
          font-size: 12px;
          color: #909399;
        }
      }
    }
  }
  
  .chat-main {
    flex: 1;
    display: flex;
    flex-direction: column;
    
    .welcome-screen {
      flex: 1;
      display: flex;
      align-items: center;
      justify-content: center;
    }
    
    .chat-header {
      padding: 16px;
      border-bottom: 1px solid #ebeef5;
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      h3 {
        margin: 0;
        font-size: 16px;
      }
      
      .header-actions {
        display: flex;
        align-items: center;
        gap: 12px;
      }
    }
    
    .message-list {
      flex: 1;
      overflow-y: auto;
      padding: 16px;
      
      .message {
        display: flex;
        gap: 12px;
        margin-bottom: 16px;
        
        &.user {
          flex-direction: row-reverse;
          
          .message-content {
            align-items: flex-end;
          }
          
          .message-text {
            background: #409eff;
            color: #fff;
          }
        }
        
        &.assistant {
          .message-text {
            background: #f5f7fa;
          }
        }
        
        .message-content {
          display: flex;
          flex-direction: column;
          gap: 4px;
          max-width: 70%;
          
          .message-text {
            padding: 12px 16px;
            border-radius: 8px;
            line-height: 1.5;
            
            &.typing {
              color: #909399;
            }
          }
          
          .message-time {
            font-size: 12px;
            color: #909399;
          }
        }
      }
    }
    
    .chat-input {
      padding: 16px;
      border-top: 1px solid #ebeef5;
      display: flex;
      gap: 12px;
      
      .el-textarea {
        flex: 1;
      }
      
      .input-actions {
        display: flex;
        flex-direction: column;
        gap: 8px;
      }
    }
  }
  
  .faq-panel {
    width: 200px;
    border-left: 1px solid #ebeef5;
    padding: 16px;
    
    h4 {
      margin: 0 0 16px 0;
      font-size: 14px;
      color: #303133;
    }
    
    .faq-list {
      .faq-item {
        padding: 10px 12px;
        background: #f5f7fa;
        border-radius: 4px;
        margin-bottom: 8px;
        font-size: 13px;
        color: #606266;
        cursor: pointer;
        
        &:hover {
          background: #ecf5ff;
          color: #409eff;
        }
      }
    }
  }
}

.ai-avatar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}
</style>
