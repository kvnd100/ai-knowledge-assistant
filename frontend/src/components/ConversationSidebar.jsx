import { useState } from 'react'
import { NavLink } from 'react-router-dom'
import { FileText, History, Plus, X } from 'lucide-react'
import Button from './Button.jsx'
import ErrorBanner from './ErrorBanner.jsx'
import Spinner from './Spinner.jsx'

function ConversationItem({ conversation, onDelete, onNavigate }) {
  return (
    <div className="group relative">
      <NavLink
        to={`/chat/${conversation.id}`}
        onClick={onNavigate}
        className={({ isActive }) =>
          `block rounded-lg py-2 pl-3 pr-8 text-sm transition ${
            isActive
              ? 'bg-brand-50 font-medium text-brand-700'
              : 'text-slate-600 hover:bg-slate-100'
          }`
        }
      >
        <span className="block truncate">
          {conversation.type === 'DOCUMENT' && (
            <FileText className="mr-1 inline h-3.5 w-3.5 align-text-bottom" aria-hidden="true" />
          )}
          {conversation.title}
        </span>
      </NavLink>
      <button
        type="button"
        onClick={() => onDelete(conversation.id)}
        aria-label={`Delete conversation "${conversation.title}"`}
        className="absolute right-1.5 top-1/2 hidden -translate-y-1/2 rounded p-1 text-xs text-faint
          hover:bg-red-50 hover:text-red-600 group-hover:block"
      >
        <X className="h-3.5 w-3.5" aria-hidden="true" />
      </button>
    </div>
  )
}

export default function ConversationSidebar({ conversations, loading, error, onNewChat, onDelete }) {
  const [mobileOpen, setMobileOpen] = useState(false)
  const closeMobile = () => setMobileOpen(false)

  const list = (
    <>
      <Button
        onClick={() => {
          closeMobile()
          onNewChat()
        }}
        className="mb-3 w-full"
      >
        <Plus className="h-4 w-4" aria-hidden="true" />
        New chat
      </Button>
      <ErrorBanner message={error} className="mb-2" />
      {loading ? (
        <div className="flex justify-center py-6">
          <Spinner className="text-brand-500" />
        </div>
      ) : conversations.length === 0 ? (
        <p className="px-2 py-4 text-center text-xs text-faint">
          No conversations yet
        </p>
      ) : (
        <div className="flex flex-col gap-0.5 overflow-y-auto">
          {conversations.map((conversation) => (
            <ConversationItem
              key={conversation.id}
              conversation={conversation}
              onDelete={onDelete}
              onNavigate={closeMobile}
            />
          ))}
        </div>
      )}
    </>
  )

  return (
    <>
      {/* Desktop */}
      <aside className="hidden w-64 shrink-0 flex-col overflow-y-auto border-r border-line bg-surface p-3 lg:flex">
        {list}
      </aside>

      {/* Mobile: collapsible history panel */}
      <div className="lg:hidden">
        <button
          type="button"
          onClick={() => setMobileOpen((open) => !open)}
          aria-expanded={mobileOpen}
          className="fixed bottom-24 left-3 z-20 flex items-center gap-1.5 rounded-full border border-line bg-surface px-3 py-2 text-sm shadow-md"
        >
          {mobileOpen ? (
            <>
              <X className="h-4 w-4" aria-hidden="true" /> Close
            </>
          ) : (
            <>
              <History className="h-4 w-4" aria-hidden="true" /> History
            </>
          )}
        </button>
        {mobileOpen && (
          <div
            className="fixed inset-0 z-10 bg-slate-900/30"
            onClick={() => setMobileOpen(false)}
          >
            <aside
              className="h-full w-72 overflow-y-auto bg-surface p-3 shadow-xl"
              onClick={(event) => event.stopPropagation()}
            >
              {list}
            </aside>
          </div>
        )}
      </div>
    </>
  )
}
