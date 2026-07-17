import { useCallback, useEffect, useRef, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { FileText, MessageCircle } from 'lucide-react'
import { api } from '../api/client'
import Button from '../components/Button.jsx'
import ErrorBanner from '../components/ErrorBanner.jsx'
import EmptyState from '../components/EmptyState.jsx'
import MessageBubble from '../components/MessageBubble.jsx'
import Spinner from '../components/Spinner.jsx'
import TypingIndicator from '../components/TypingIndicator.jsx'
import ConversationSidebar from '../components/ConversationSidebar.jsx'

export default function ChatPage() {
  const { conversationId } = useParams()
  const navigate = useNavigate()

  const [conversations, setConversations] = useState([])
  const [conversationsLoading, setConversationsLoading] = useState(true)
  const [conversationsError, setConversationsError] = useState(null)

  const [detail, setDetail] = useState(null)
  const [messages, setMessages] = useState([])
  const [detailLoading, setDetailLoading] = useState(false)
  const [detailError, setDetailError] = useState(null)

  const [draft, setDraft] = useState('')
  const [sending, setSending] = useState(false)
  const [sendError, setSendError] = useState(null)

  const bottomRef = useRef(null)

  const refreshConversations = useCallback(async () => {
    setConversationsError(null)
    try {
      setConversations(await api.listConversations())
    } catch (err) {
      setConversationsError(err.message)
    } finally {
      setConversationsLoading(false)
    }
  }, [])

  useEffect(() => {
    refreshConversations()
  }, [refreshConversations])

  useEffect(() => {
    if (!conversationId) {
      setDetail(null)
      setMessages([])
      setDetailError(null)
      return
    }
    let cancelled = false
    setDetailLoading(true)
    setDetailError(null)
    api
      .getConversation(conversationId)
      .then((data) => {
        if (cancelled) return
        setDetail(data)
        setMessages(data.messages)
      })
      .catch((err) => {
        if (!cancelled) setDetailError(err.message)
      })
      .finally(() => {
        if (!cancelled) setDetailLoading(false)
      })
    return () => {
      cancelled = true
    }
  }, [conversationId])

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages, sending])

  async function handleSend(event) {
    event.preventDefault()
    const content = draft.trim()
    if (!content || sending) return

    setSendError(null)
    setSending(true)
    setDraft('')
    setMessages((current) => [...current, { id: `local-${Date.now()}`, role: 'USER', content }])

    try {
      let targetId = conversationId
      if (!targetId) {
        const created = await api.createConversation()
        targetId = created.id
      }
      const response = await api.sendMessage(targetId, content)
      setMessages((current) => [
        ...current.slice(0, -1),
        response.userMessage,
        response.assistantMessage,
      ])
      setDetail((current) =>
        current ? { ...current, title: response.conversationTitle } : current,
      )
      refreshConversations()
      if (!conversationId) {
        navigate(`/chat/${targetId}`, { replace: true })
      }
    } catch (err) {
      setSendError(err.message)
      setDraft(content)
      setMessages((current) => current.slice(0, -1))
    } finally {
      setSending(false)
    }
  }

  async function handleNewChat() {
    navigate('/chat')
  }

  async function handleDelete(id) {
    if (!window.confirm('Delete this conversation and all of its messages?')) return
    try {
      await api.deleteConversation(id)
      setConversations((current) => current.filter((conversation) => conversation.id !== id))
      if (String(id) === conversationId) navigate('/chat')
    } catch (err) {
      setConversationsError(err.message)
    }
  }

  const isDocumentChat = detail?.type === 'DOCUMENT'

  return (
    <div className="flex h-full">
      <ConversationSidebar
        conversations={conversations}
        loading={conversationsLoading}
        error={conversationsError}
        onNewChat={handleNewChat}
        onDelete={handleDelete}
      />

      <div className="flex min-w-0 flex-1 flex-col">
        {/* Conversation header */}
        <div className="border-b border-line bg-surface px-4 py-3">
          <h2 className="truncate text-sm font-semibold text-slate-800">
            {detail ? detail.title : 'New conversation'}
          </h2>
          {isDocumentChat && (
            <p className="truncate text-xs text-muted">
              <FileText className="mr-1 inline h-3.5 w-3.5 align-text-bottom" aria-hidden="true" />
              Answering from: {detail.documentFilename}
            </p>
          )}
        </div>

        {/* Message thread */}
        <div className="flex-1 overflow-y-auto px-4 py-4">
          {detailLoading ? (
            <div className="flex h-full items-center justify-center">
              <Spinner size="lg" className="text-brand-500" />
            </div>
          ) : detailError ? (
            <ErrorBanner message={detailError} />
          ) : messages.length === 0 && !sending ? (
            <EmptyState
              icon={MessageCircle}
              title={isDocumentChat ? 'Ask about this document' : 'Start a conversation'}
              subtitle={
                isDocumentChat
                  ? 'Answers are based on the contents of the uploaded document.'
                  : 'Conversations are saved and can be resumed at any time.'
              }
            />
          ) : (
            <div className="mx-auto flex max-w-3xl flex-col gap-3">
              {messages.map((message) => (
                <MessageBubble key={message.id} role={message.role} content={message.content} />
              ))}
              {sending && <TypingIndicator />}
              <div ref={bottomRef} />
            </div>
          )}
        </div>

        {/* Composer */}
        <div className="border-t border-line bg-surface p-4">
          <ErrorBanner message={sendError} onDismiss={() => setSendError(null)} className="mx-auto mb-3 max-w-3xl" />
          <form onSubmit={handleSend} className="mx-auto flex max-w-3xl items-end gap-2">
            <textarea
              value={draft}
              onChange={(event) => setDraft(event.target.value)}
              onKeyDown={(event) => {
                if (event.key === 'Enter' && !event.shiftKey) {
                  event.preventDefault()
                  handleSend(event)
                }
              }}
              placeholder={isDocumentChat ? 'Ask about the document…' : 'Send a message…'}
              rows={1}
              maxLength={8000}
              className="max-h-40 min-h-[42px] flex-1 resize-y rounded-lg border border-slate-300 px-3 py-2.5 text-sm
                outline-none transition placeholder:text-faint focus:border-brand-400 focus:ring-2 focus:ring-brand-100"
            />
            <Button type="submit" loading={sending} disabled={!draft.trim()}>
              Send
            </Button>
          </form>
        </div>
      </div>
    </div>
  )
}
