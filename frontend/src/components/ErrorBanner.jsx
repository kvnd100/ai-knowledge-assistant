export default function ErrorBanner({ message, onDismiss, className = '' }) {
  if (!message) return null
  return (
    <div
      role="alert"
      className={`flex items-start justify-between gap-3 rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700 ${className}`}
    >
      <span>{message}</span>
      {onDismiss && (
        <button
          type="button"
          onClick={onDismiss}
          aria-label="Dismiss error"
          className="shrink-0 font-bold text-red-400 hover:text-red-600"
        >
          &times;
        </button>
      )}
    </div>
  )
}
