import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { api } from '../api/client'
import { useAuth } from '../context/AuthContext.jsx'
import Card from '../components/Card.jsx'
import EmptyState from '../components/EmptyState.jsx'
import ErrorBanner from '../components/ErrorBanner.jsx'
import Spinner from '../components/Spinner.jsx'

function StatCard({ label, value, icon }) {
  return (
    <Card className="flex items-center gap-4">
      <span className="text-3xl" aria-hidden="true">{icon}</span>
      <div>
        <p className="text-2xl font-bold text-slate-900">{value}</p>
        <p className="text-sm text-slate-500">{label}</p>
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
      <h1 className="mb-1 text-xl font-bold text-slate-900">
        Welcome back{user ? `, ${user.displayName}` : ''} 👋
      </h1>
      <p className="mb-6 text-sm text-slate-500">Here is a summary of your account activity.</p>

      <ErrorBanner message={error} className="mb-6" />

      {!stats && !error ? (
        <div className="flex justify-center py-16">
          <Spinner size="lg" className="text-indigo-500" />
        </div>
      ) : stats ? (
        <>
          <div className="mb-6 grid gap-4 sm:grid-cols-3">
            <StatCard icon="💬" label="Conversations" value={stats.conversationCount} />
            <StatCard icon="✉️" label="Messages" value={stats.messageCount} />
            <StatCard icon="📄" label="Documents" value={stats.documentCount} />
          </div>

          <Card title="Recent conversations">
            {stats.recentConversations.length === 0 ? (
              <EmptyState
                icon="💬"
                title="Nothing here yet"
                subtitle="Start your first conversation to see it listed here."
              />
            ) : (
              <ul className="divide-y divide-slate-100">
                {stats.recentConversations.map((conversation) => (
                  <li key={conversation.id}>
                    <Link
                      to={`/chat/${conversation.id}`}
                      className="flex items-center justify-between gap-3 py-3 hover:bg-slate-50"
                    >
                      <span className="min-w-0 truncate text-sm font-medium text-slate-800">
                        {conversation.type === 'DOCUMENT' && <span aria-hidden="true">📄 </span>}
                        {conversation.title}
                      </span>
                      <span className="shrink-0 text-xs text-slate-400">
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
