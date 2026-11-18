package com.catchandgo.auth.supabase;

public class SupabaseAuthException extends RuntimeException {
    private final boolean unavailable;
    public SupabaseAuthException(String message, boolean unavailable) {
        super(message);
        this.unavailable = unavailable;
    }
    public boolean isUnavailable() { return unavailable; }
}
