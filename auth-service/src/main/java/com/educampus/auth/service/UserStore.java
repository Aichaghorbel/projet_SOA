package com.educampus.auth.service;

import com.educampus.auth.config.AuthProperties;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Utilisateurs en mémoire (dev). Les mots de passe sont hachés au démarrage. */
@Component
public class UserStore {

    public record StoredUser(String username, String passwordHash, List<String> roles) {
    }

    private final Map<String, StoredUser> users;
    private final PasswordEncoder encoder;
    private final String dummyHash;

    public UserStore(AuthProperties properties, PasswordEncoder encoder) {
        this.encoder = encoder;
        this.dummyHash = encoder.encode("dummy");
        this.users = properties.users().stream()
                .map(u -> new StoredUser(u.username(), encoder.encode(u.password()), u.roles()))
                .collect(Collectors.toMap(StoredUser::username, u -> u));
    }

    public Optional<StoredUser> authenticate(String username, String password) {
        StoredUser user = users.get(username);
        if (user == null) {
            encoder.matches(password, dummyHash); // temps de réponse similaire si l'utilisateur est inconnu
            return Optional.empty();
        }
        return encoder.matches(password, user.passwordHash()) ? Optional.of(user) : Optional.empty();
    }
}