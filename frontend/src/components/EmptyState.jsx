export default function EmptyState({ icon: Icon, title, subtitle, action }) {
  return (
    <div className="flex flex-col items-center justify-center gap-2 py-12 text-center">
      {Icon && <Icon className="h-10 w-10 text-slate-300" aria-hidden="true" />}
      <h3 className="text-base font-semibold text-slate-700">{title}</h3>
      {subtitle && <p className="max-w-sm text-sm text-muted">{subtitle}</p>}
      {action && <div className="mt-3">{action}</div>}
    </div>
  )
}
