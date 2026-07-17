export default function Card({ title, children, className = '' }) {
  return (
    <div className={`rounded-xl border border-line bg-surface p-5 ${className}`}>
      {title && <h2 className="mb-3 text-sm font-semibold text-muted uppercase tracking-wide">{title}</h2>}
      {children}
    </div>
  )
}
