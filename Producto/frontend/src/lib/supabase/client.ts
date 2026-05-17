export function createClient() {
  return {
    auth: {
      getUser: async () => ({ data: { user: null }, error: null }),
      signOut: async () => ({ error: null }),
      signInWithOAuth: async () => ({ data: null, error: null }),
      exchangeCodeForSession: async () => ({ error: null })
    }
  };
}
