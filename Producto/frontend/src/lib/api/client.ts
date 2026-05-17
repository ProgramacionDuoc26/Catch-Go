/**
 * Base HTTP client para comunicarse con los microservicios Java.
 * Cada servicio tiene su propia URL base configurada en .env.local
 */

export interface ApiResponse<T> {
  data: T | null;
  error: string | null;
  status: number;
}

async function request<T>(
  url: string,
  options: RequestInit = {}
): Promise<ApiResponse<T>> {
  try {
    const token = typeof window !== 'undefined'
      ? localStorage.getItem('auth_token')
      : null;

    const response = await fetch(url, {
      ...options,
      headers: {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
        ...options.headers,
      },
    });

    if (!response.ok) {
      const errorText = await response.text();
      let errorMessage = errorText || response.statusText;
      if (errorText) {
        try {
          const parsedError = JSON.parse(errorText);
          errorMessage = parsedError.message || parsedError.error || errorMessage;
        } catch (e) {
          // Si no es JSON, se queda con errorText
        }
      }
      return { data: null, error: errorMessage, status: response.status };
    }

    // Si no hay contenido (204 No Content o body vacío), retornamos null sin intentar parsear JSON
    const text = await response.text();
    let data: T = null as T;
    if (text && text.trim().length > 0) {
      try {
        data = JSON.parse(text);
      } catch (e) {
        console.error('Error parsing JSON:', e, 'Text:', text);
      }
    }
    
    return { data, error: null, status: response.status };
  } catch (err) {
    return {
      data: null,
      error: err instanceof Error ? err.message : 'Error de red',
      status: 0,
    };
  }
}

export const api = {
  get: <T>(url: string) => request<T>(url),
  post: <T>(url: string, body: unknown) =>
    request<T>(url, { method: 'POST', body: JSON.stringify(body) }),
  put: <T>(url: string, body: unknown) =>
    request<T>(url, { method: 'PUT', body: JSON.stringify(body) }),
  delete: <T>(url: string) =>
    request<T>(url, { method: 'DELETE' }),
};
