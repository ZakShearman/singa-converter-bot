package pink.zak.discord.singaconverterbot.repository;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Repository;
import se.michaelthelin.spotify.SpotifyApi;

import java.util.concurrent.TimeUnit;

@Repository
public class UserSpotifyApiRepository {
    private final @NotNull Cache<String, SpotifyApi> accessTokens = Caffeine.newBuilder()
            .expireAfterWrite(59, TimeUnit.MINUTES).build();

    public void saveSpotifyApi(String clientId, @NotNull SpotifyApi api) {
        this.accessTokens.put(clientId, api);
    }

    public @NotNull Cache<String, SpotifyApi> getCache() {
        return this.accessTokens;
    }
}
