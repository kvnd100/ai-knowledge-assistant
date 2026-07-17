import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { FileText, Mail, MessageCircle } from 'lucide-react'
import { api } from '../api/client'
import { useAuth } from '../context/AuthContext.jsx'
import Card from '../components/Card.jsx'
import EmptyState from '../components/EmptyState.jsx'
import ErrorBanner from '../components/ErrorBanner.jsx'
import Spinner from '../components/Spinner.jsx'

function StatCard({ label, value, icon: Icon }) {
  return (
    <Card className="flex items-center gap-4">
      <Icon className="h-8 w-8 text-brand-500" aria-hidden="true" />
      <div>
        <p className="text-2xl font-bold text-ink">{value}</p>
        <p className="text-sm text-muted">{label}</p>
      </div>
    </Card>
  )
}

export default function DashboardPage() {
  const { user } = useAuth()
  const [stats, setStats] = useState(null)
  const [error, setError] = useState(null)

  useEffect(() => {
    let cancelled = false
    api
      .dashboardStats()
      .then((data) => {
        if (!cancelled) setStats(data)
      })
      .catch((err) => {
        if (!cancelled) setError(err.message)
      })
    return () => {
      cancelled = true
    }
  }, [])

  return (
    <div className="mx-auto max-w-4xl px-4 py-8">
      <h1 className="mb-1 text-xl font-bold text-ink">
        Welcome back{user ? `, ${user.displayName}` : ''}
      </h1>
      <p className="mb-6 text-sm text-muted">Overview of your activity.</p>

      <ErrorBanner message={error} className="mb-6" />

      {!stats && !error ? (
        <div className="flex justify-center py-16">
          <Spinner size="lg" className="text-brand-500" />
        </div>
      ) : stats ? (
        <>
          <div className="mb-6 grid gap-4 sm:grid-cols-3">
            <StatCard icon={MessageCircle} label="Conversations" value={stats.conversationCount} />
            <StatCard icon={Mail} label="Messages" value={stats.messageCount} />
            <StatCard icon={FileText} label="Documents" value={stats.documentCount} />
          </div>

          <Card title="Recent conversations">
            {stats.recentConversations.length === 0 ? (
              <EmptyState
                icon={MessageCircle}
                title="No conversations yet"
                subtitle="Recent conversations appear here."
              />
            ) : (
              <ul className="divide-y divide-line-soft">
                {stats.recentConversations.map((conversation) => (
                  <li key={conversation.id}>
                    <Link
                      to={`/chat/${conversation.id}`}
                      className="flex items-center justify-between gap-3 py-3 hover:bg-canvas"
                    >
                      <span className="min-w-0 truncate text-sm font-medium text-slate-800">
                        {conversation.type === 'DOCUMENT' && (
                          <FileText className="mr-1 inline h-3.5 w-3.5 align-text-bottom" aria-hidden="true" />
                        )}
                        {conversation.title}
                      </span>
                      <span className="shrink-0 text-xs text-faint">
                        {new Date(conversation.updatedAt).toLocaleDateString()}
                      </span>
                    </Link>
                  </li>
                ))}
              </ul>
            )}
          </Card>
        </>
      ) : null}
    </div>
  )
}
