import { createServerClient, type CookieOptions } from '@supabase/ssr'
import { cookies } from 'next/headers'
import { NextResponse } from 'next/server'

export async function GET(request: Request) {
  const requestUrl = new URL(request.url)
  const searchParams = requestUrl.searchParams
  const code = searchParams.get('code')
  const next = searchParams.get('next') ?? '/'

  // Reconstruct absolute URL using forwarded headers to avoid container internal IP (0.0.0.0)
  const host = request.headers.get('x-forwarded-host') || request.headers.get('host') || requestUrl.host;
  const proto = request.headers.get('x-forwarded-proto') || 'https';
  const cleanHost = host.includes('0.0.0.0') ? 'catch-go-production.up.railway.app' : host;
  const origin = `${proto}://${cleanHost}`;

  if (code) {
    const cookieStore = cookies()
    const supabase = createServerClient(
      process.env.NEXT_PUBLIC_SUPABASE_URL!,
      process.env.NEXT_PUBLIC_SUPABASE_ANON_KEY!,
      {
        cookies: {
          get(name: string) {
            return cookieStore.get(name)?.value
          },
          set(name: string, value: string, options: CookieOptions) {
            cookieStore.set({ name, value, ...options })
          },
          remove(name: string, options: CookieOptions) {
            cookieStore.set({ name, value: '', ...options })
          },
        },
      }
    )
    const { error } = await supabase.auth.exchangeCodeForSession(code)
    if (!error) {
      // Redirigir a la selección de rol después del login exitoso con Google
      return NextResponse.redirect(`${origin}/auth/role-selection`)
    }
  }

  // En caso de error, redirigir al login
  return NextResponse.redirect(`${origin}/login?error=auth_failed`)
}
