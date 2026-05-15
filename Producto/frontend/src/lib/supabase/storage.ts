const PROFILE_BASE = process.env.NEXT_PUBLIC_PROFILE_SERVICE_URL || 'http://localhost:8082';

/**
 * Sube un archivo al backend Java (profile-service).
 * Retorna la URL pública completa del archivo guardado.
 */
export async function uploadFile(file: File, userId: string): Promise<string> {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('userId', userId);

  const response = await fetch(`${PROFILE_BASE}/files/upload`, {
    method: 'POST',
    body: formData,
  });

  if (!response.ok) {
    let errorMsg = 'Error al subir archivo';
    try {
      const errData = await response.json();
      errorMsg = errData.error || errorMsg;
    } catch { /* ignore */ }
    throw new Error(errorMsg);
  }

  const data = await response.json();
  return `${PROFILE_BASE}${data.url}`;
}
