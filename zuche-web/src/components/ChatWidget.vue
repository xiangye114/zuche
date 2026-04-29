<template>
  <div class="chat-widget">
    <div class="chat-trigger" @click="toggleChat">
      <el-badge :value="unreadCount" :hidden="!unreadCount">
        <el-icon :size="24"><Service /></el-icon>
      </el-badge>
    </div>
    
    <div class="chat-window" v-if="isOpen">
      <div class="chat-header">
        <span>智能客服</span>
        <el-button text @click="toggleChat">
          <el-icon><Close /></el-icon>
        </el-button>
      </div>
      
      <div class="chat-messages" ref="messagesRef">
        <div 
          v-for="msg in messages" 
          :key="msg.id" 
          :class="['message', msg.role]"
        >
          <div class="avatar">
            <el-avatar :size="32" v-if="msg.role === 'user'">
              {{ userStore.user?.username?.charAt(0)?.toUpperCase() }}
            </el-avatar>
            <el-avatar :size="32" v-else style="background: #409eff">
              <el-icon><Service /></el-icon>
            </el-avatar>
          </div>
          <div class="content">{{ msg.content }}</div>
        </div>
        <div v-if="loading" class="message assistant">
          <div class="avatar">
            <el-avatar :size="32" style="background: #409eff">
              <el-icon><Service /></el-icon>
            </el-avatar>
          </div>
          <div class="content typing">
            <span></span>
            <span></span>
            <span></span>
          </div>
        </div>
      </div>
      
      <div class="chat-quick-actions" v-if="messages.length === 0">
        <el-tag 
          v-for="faq in quickFaqs" 
          :key="faq" 
          class="quick-tag"
          @click="sendQuickMessage(faq)"
        >
          {{ faq }}
        </el-tag>
      </div>
      
      <div class="chat-input">
        <el-input 
          v-model="inputMessage" 
          placeholder="请输入您的问题..."
          @keyup.enter="sendMessage"
        />
        <el-button type="primary" @click="sendMessage" :loading="loading">
          发送
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted } from 'vue'
import { chatApi } from '@/api/chat'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()

const isOpen = ref(false)
const loading = ref(false)
const inputMessage = ref('')
const messages = ref([])
const messagesRef = ref(null)
const unreadCount = ref(0)
const sessionId = ref(null)

const quickFaqs = ['如何租车？', '需要什么证件？', '如何取消订单？', '费用包含哪些？']

const toggleChat = () => {
  isOpen.value = !isOpen.value
  if (isOpen.value && messages.value.length === 0) {
    messages.value.push({
      id: Date.now(),
      role: 'assistant',
      content: '您好！我是智能客服，有什么可以帮助您的吗？'
    })
  }
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesRef.value) {
      messagesRef.value.scrollTop = messagesRef.value.scrollHeight
    }
  })
}

const sendMessage = async () => {
  if (!inputMessage.value.trim() || loading.value) return
  
  const userMsg = inputMessage.value.trim()
  messages.value.push({
    id: Date.now(),
    role: 'user',
    content: userMsg
  })
  
  inputMessage.value = ''
  scrollToBottom()
  
  loading.value = true
  try {
    if (!sessionId.value) {
      const sessionRes = await chatApi.createSession()
      sessionId.value = sessionRes.sessionId
    }
    
    const res = await chatApi.send({
      sessionId: sessionId.value,
      content: userMsg,
      useRag: true
    })
    
    messages.value.push({
      id: Date.now() + 1,
      role: 'assistant',
      content: res.content
    })
    scrollToBottom()
  } catch (error) {
    // 显示更具体的错误信息
    let errorMessage = '抱歉，我遇到了一些问题，请稍后再试。'
    if (error.response && error.response.data) {
      errorMessage = error.response.data.message || errorMessage
    } else if (error.message) {
      errorMessage = error.message
    }
    messages.value.push({
      id: Date.now() + 1,
      role: 'assistant',
      content: errorMessage
    })
  } finally {
    loading.value = false
  }
}

const sendQuickMessage = (message) => {
  inputMessage.value = message
  sendMessage()
}
</script>

<style lang="scss" scoped>
.chat-widget {
  position: fixed;
  right: 20px;
  bottom: 20px;
  z-index: 1000;
  
  .chat-trigger {
    width: 56px;
    height: 56px;
    border-radius: 50%;
    background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
    transition: transform 0.3s;
    
    &:hover {
      transform: scale(1.1);
    }
    
    .el-icon {
      color: #fff;
    }
  }
  
  .chat-window {
    position: absolute;
    right: 0;
    bottom: 70px;
    width: 380px;
    height: 500px;
    background: #fff;
    border-radius: 12px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
    display: flex;
    flex-direction: column;
    overflow: hidden;
  }
  
  .chat-header {
    padding: 16px;
    background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
    color: #fff;
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-weight: 600;
    
    .el-button {
      color: #fff;
    }
  }
  
  .chat-messages {
    flex: 1;
    padding: 16px;
    overflow-y: auto;
    
    .message {
      display: flex;
      gap: 10px;
      margin-bottom: 16px;
      
      &.user {
        flex-direction: row-reverse;
        
        .content {
          background: #409eff;
          color: #fff;
        }
      }
      
      .content {
        max-width: 70%;
        padding: 10px 14px;
        border-radius: 12px;
        background: #f5f7fa;
        line-height: 1.5;
        word-break: break-word;
        
        &.typing {
          display: flex;
          gap: 4px;
          padding: 16px;
          
          span {
            width: 8px;
            height: 8px;
            background: #909399;
            border-radius: 50%;
            animation: typing 1s infinite;
            
            &:nth-child(2) { animation-delay: 0.2s; }
            &:nth-child(3) { animation-delay: 0.4s; }
          }
        }
      }
    }
  }
  
  .chat-quick-actions {
    padding: 0 16px 12px;
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    
    .quick-tag {
      cursor: pointer;
      
      &:hover {
        background: #409eff;
        color: #fff;
      }
    }
  }
  
  .chat-input {
    padding: 12px 16px;
    border-top: 1px solid #ebeef5;
    display: flex;
    gap: 10px;
    
    .el-input {
      flex: 1;
    }
  }
}

@keyframes typing {
  0%, 100% { opacity: 0.3; }
  50% { opacity: 1; }
}
</style>
