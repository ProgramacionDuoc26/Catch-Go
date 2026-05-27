import { createClient } from './client';

/**
 * Sube un archivo al Supabase Storage y retorna la URL pública.
 * Usa el bucket 'profiles' en Supabase para almacenamiento persistente.
 * (El backend Java guarda en disco local que se borra en cada redeploy de Railway)
 */
export async function uploadFile(file: File, userId: string): Promise<string> {
  const supabase = createClient();

  // Generar nombre único con extensión original
  const ext = file.name.includes('.') ? file.name.substring(file.name.lastIndexOf('.')) : '';
  const fileName = `${userId}/${Date.now()}_${Math.random().toString(36).substring(2)}${ext}`;

  const { data, error } = await supabase.storage
    .from('profiles')
    .upload(fileName, file, {
      cacheControl: '3600',
      upsert: true,
      contentType: file.type || 'application/octet-stream',
    });

  if (error) {
    throw new Error(`Error al subir archivo: ${error.message}`);
  }

  // Obtener URL pública del archivo
  const { data: { publicUrl } } = supabase.storage
    .from('profiles')
    .getPublicUrl(data.path);

  return publicUrl;
}
