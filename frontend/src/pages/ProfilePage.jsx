import { useEffect, useState } from 'react'
import { api } from '../api/client'
import Card from '../components/Card.jsx'
import ErrorBanner from '../components/ErrorBanner.jsx'
import Spinner from '../components/Spinner.jsx'

function ProfileRow({ label, value }) {
  return (
    <div className="flex flex-col gap-0.5 py-3 sm:flex-row sm:items-center sm:justify-between">
      <dt className="text-sm text-muted">{label}</dt>
      <dd className="text-sm font-medium text-slate-800">{value}</dd>
    </div>
  )
}

export default function ProfilePage() {
  const [profile, setProfile] = useState(null)
  const [error, setError] = useState(null)

  useEffect(() => {
    let cancelled = false
    api
      .me()
      .then((data) => {
        if (!cancelled) setProfile(data)
      })
      .catch((err) => {
        if (!cancelled) setError(err.message)
      })
    return () => {
      cancelled = true
    }
  }, [])

  return (
    <div className="mx-auto max-w-xl px-4 py-8">
      <h1 className="mb-1 text-xl font-bold text-ink">Profile</h1>
      <p className="mb-6 text-sm text-muted">Account details.</p>

      <ErrorBanner message={error} className="mb-6" />

      {!profile && !error ? (
        <div className="flex justify-center py-16">
          <Spinner size="lg" className="text-brand-500" />
        </div>
      ) : profile ? (
        <Card>
          <div className="mb-4 flex items-center gap-4">
            <div className="flex h-14 w-14 items-center justify-center rounded-full bg-brand-100 text-xl font-bold text-brand-700">
              {profile.displayName?.charAt(0).toUpperCase()}
            </div>
            <div>
              <p className="font-semibold text-ink">{profile.displayName}</p>
              <p className="text-sm text-muted">{profile.email}</p>
            </div>
          </div>
          <dl className="divide-y divide-line-soft border-t border-slate-100">
            <ProfileRow label="Display name" value={profile.displayName} />
            <ProfileRow label="Email" value={profile.email} />
            <ProfileRow
              label="Member since"
              value={new Date(profile.createdAt).toLocaleDateString(undefined, {
                year: 'numeric',
                month: 'long',
                day: 'numeric',
              })}
            />
          </dl>
        </Card>
      ) : null}
    </div>
  )
}
