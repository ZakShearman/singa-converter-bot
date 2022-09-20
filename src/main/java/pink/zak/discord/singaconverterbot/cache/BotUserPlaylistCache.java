package pink.zak.discord.singaconverterbot.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Component;
import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class BotUserPlaylistCache {
    private final Cache<Long, List<PlaylistSimplified>> cache = Caffeine.newBuilder()
            .expireAfterAccess(30, TimeUnit.SECONDS)
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .build();

    public Cache<Long, List<PlaylistSimplified>> getCache() {
        return this.cache;
    }
}
