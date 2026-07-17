import { useCallback, useEffect, useRef, useState } from 'react'
import { Link } from 'react-router-dom'
import { File, FileText, Upload } from 'lucide-react'
import { api } from '../api/client'
import Button from '../components/Button.jsx'
import Card from '../components/Card.jsx'
import EmptyState from '../components/EmptyState.jsx'
import ErrorBanner from '../components/ErrorBanner.jsx'
import Spinner from '../components/Spinner.jsx'

const MAX_FILE_SIZE = 5 * 1024 * 1024

function formatSize(bytes) {
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`
}

export default function DocumentsPage() {
  const [documents, setDocuments] = useState([])
  const [loading, setLoading] = useState(true)
  const [listError, setListError] = useState(null)
  const [uploading, setUploading] = useState(false)
  const [uploadError, setUploadError] = useState(null)
  const [dragOver, setDragOver] = useState(false)
  const fileInputRef = useRef(null)

  const refresh = useCallback(async () => {
    setListError(null)
    try {
      setDocuments(await api.listDocuments())
    } catch (err) {
      setListError(err.message)
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    refresh()
  }, [refresh])

  async function handleFile(file) {
    if (!file) return
    setUploadError(null)
    const name = file.name.toLowerCase()
    if (!name.endsWith('.pdf') && !name.endsWith('.txt')) {
      setUploadError('Only PDF and TXT files are supported.')
      return
    }
    if (file.size > MAX_FILE_SIZE) {
      setUploadError('File exceeds the maximum allowed size of 5 MB.')
      return
    }
    setUploading(true)
    try {
      await api.uploadDocument(file)
      await refresh()
    } catch (err) {
      setUploadError(err.message)
    } finally {
      setUploading(false)
      if (fileInputRef.current) fileInputRef.current.value = ''
    }
  }

  return (
    <div className="mx-auto max-w-3xl px-4 py-8">
      <h1 className="mb-1 text-xl font-bold text-ink">Documents</h1>
      <p className="mb-6 text-sm text-muted">
        Upload a PDF or TXT file (max 5 MB) to ask questions about its contents.
      </p>

      {/* Upload dropzone */}
      <div
        onDragOver={(event) => {
          event.preventDefault()
          setDragOver(true)
        }}
        onDragLeave={() => setDragOver(false)}
        onDrop={(event) => {
          event.preventDefault()
          setDragOver(false)
          handleFile(event.dataTransfer.files?.[0])
        }}
        className={`mb-3 flex flex-col items-center justify-center gap-2 rounded-xl border-2 border-dashed
          px-6 py-10 text-center transition ${
            dragOver ? 'border-brand-400 bg-brand-50' : 'border-slate-300 bg-surface'
          }`}
      >
        {uploading ? (
          <>
            <Spinner size="lg" className="text-brand-500" />
            <p className="text-sm text-muted">Uploading and extracting text…</p>
          </>
        ) : (
          <>
            <Upload className="h-8 w-8 text-faint" aria-hidden="true" />
            <p className="text-sm font-medium text-slate-700">Drag & drop a PDF or TXT here</p>
            <p className="text-xs text-faint">or</p>
            <Button variant="secondary" onClick={() => fileInputRef.current?.click()}>
              Browse files
            </Button>
            <input
              ref={fileInputRef}
              type="file"
              accept=".pdf,.txt,application/pdf,text/plain"
              className="hidden"
              onChange={(event) => handleFile(event.target.files?.[0])}
            />
          </>
        )}
      </div>
      <ErrorBanner message={uploadError} onDismiss={() => setUploadError(null)} className="mb-6" />

      {/* Document list */}
      <Card title="Your documents">
        <ErrorBanner message={listError} className="mb-3" />
        {loading ? (
          <div className="flex justify-center py-8">
            <Spinner size="lg" className="text-brand-500" />
          </div>
        ) : documents.length === 0 ? (
          <EmptyState
            icon={FileText}
            title="No documents yet"
            subtitle="Uploaded documents appear here."
          />
        ) : (
          <ul className="divide-y divide-line-soft">
            {documents.map((document) => (
              <li key={document.id} className="flex flex-wrap items-center justify-between gap-3 py-3">
                <div className="min-w-0">
                  <p className="truncate text-sm font-medium text-slate-800">
                    {document.contentType === 'application/pdf' ? (
                      <File className="mr-1 inline h-3.5 w-3.5 align-text-bottom" aria-hidden="true" />
                    ) : (
                      <FileText className="mr-1 inline h-3.5 w-3.5 align-text-bottom" aria-hidden="true" />
                    )}
                    {document.filename}
                  </p>
                  <p className="text-xs text-faint">
                    {formatSize(document.sizeBytes)} · {new Date(document.createdAt).toLocaleString()}
                  </p>
                </div>
                {document.conversationId && (
                  <Link to={`/chat/${document.conversationId}`}>
                    <Button variant="secondary">Open chat</Button>
                  </Link>
                )}
              </li>
            ))}
          </ul>
        )}
      </Card>
    </div>
  )
}
