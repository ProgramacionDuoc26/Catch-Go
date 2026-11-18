package com.catchandgo.auth.supabase;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestClientException;

@Component
public class SupabaseIdentityClient {
    private final RestClient.Builder builder;
    private final String url;
    private final String apiKey;
    public record Identity(String id, String email, String name, String photo) {}

    public SupabaseIdentityClient(RestClient.Builder builder, @Value("${SUPABASE_URL:}") String url,
                                  @Value("${SUPABASE_ANON_KEY:}") String apiKey) {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(5000);
        this.builder = builder.requestFactory(factory);
        this.url = url;
        this.apiKey = apiKey;
    }

    public Identity verify(String token) {
        if (url.isBlank() || apiKey.isBlank()) throw new SupabaseAuthException("Supabase no esta configurado", true);
        if (token == null || token.isBlank()) throw new SupabaseAuthException("Sesion de Supabase invalida", false);
        try {
            JsonNode user = builder.baseUrl(url).build().get().uri("/auth/v1/user")
                    .header("apikey", apiKey).headers(h -> h.setBearerAuth(token)).retrieve().body(JsonNode.class);
            if (user == null || user.path("id").asText().isBlank()) throw new SupabaseAuthException("Sesion de Supabase invalida", false);
            JsonNode metadata = user.path("app_metadata");
            boolean google = "google".equals(metadata.path("provider").asText());
            for (JsonNode provider : metadata.path("providers")) {
                if ("google".equals(provider.asText())) google = true;
            }
            if (!google) throw new SupabaseAuthException("Se requiere una cuenta vinculada a Google", false);
            String id;
            try { id = UUID.fromString(user.path("id").asText()).toString(); }
            catch (IllegalArgumentException ex) { throw new SupabaseAuthException("Identidad de Supabase invalida", false); }
            String email = user.path("email").asText("");
            String name = user.path("user_metadata").path("full_name").asText("");
            if (name.isBlank()) name = user.path("user_metadata").path("name").asText("");
            if (name.isBlank()) name = email.isBlank() ? "Usuario" : email;
            String photo = user.path("user_metadata").path("avatar_url").asText("");
            return new Identity(id, email, name, photo);
        } catch (RestClientResponseException ex) {
            boolean unavailable = ex.getStatusCode().value() != 401 && ex.getStatusCode().value() != 403;
            throw new SupabaseAuthException(unavailable ? "No se pudo verificar la sesion" : "Sesion de Supabase invalida", unavailable);
        } catch (RestClientException ex) {
            throw new SupabaseAuthException("No se pudo verificar la sesion", true);
        }
    }
}
