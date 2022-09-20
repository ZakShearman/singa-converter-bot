package pink.zak.discord.singaconverterbot.repository;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Repository
public class OAuthFlowTokenRepository {
    private final Cache<UUID, Long> oAuthFlowTokens = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS).build();

    public @NotNull UUID createToken(long discordId) {
        UUID token = this.randomToken();
        this.oAuthFlowTokens.put(token, discordId);
        return token;
    }

    public @NotNull Optional<Long> getDiscordUserFromToken(UUID token) {
        return Optional.ofNullable(this.oAuthFlowTokens.getIfPresent(token));
    }

    private @NotNull UUID randomToken() {
        UUID token = UUID.randomUUID();
        while (this.oAuthFlowTokens.getIfPresent(token) != null)
            token = UUID.randomUUID();

        return token;
    }
}
