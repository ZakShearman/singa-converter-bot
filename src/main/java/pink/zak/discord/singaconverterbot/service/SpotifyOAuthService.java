package pink.zak.discord.singaconverterbot.service;

import net.dv8tion.jda.api.entities.User;
import org.apache.hc.core5.http.ParseException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pink.zak.discord.singaconverterbot.model.BotUser;
import pink.zak.discord.singaconverterbot.repository.BotUserRepository;
import pink.zak.discord.singaconverterbot.repository.OAuthFlowTokenRepository;
import pink.zak.discord.singaconverterbot.repository.UserSpotifyApiRepository;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.enums.AuthorizationScope;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Service
public class SpotifyOAuthService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SpotifyOAuthService.class);

    private final SpotifyApi spotifyApi;
    private final String authUri;

    private final UserSpotifyApiRepository accessTokenRepository;
    private final OAuthFlowTokenRepository oAuthFlowTokenRepository;
    private final BotUserRepository botUserRepository;

    public SpotifyOAuthService(SpotifyApi spotifyApi, UserSpotifyApiRepository accessTokenRepository, OAuthFlowTokenRepository oAuthFlowTokenRepository, BotUserRepository botUserRepository) {
        this.spotifyApi = spotifyApi;
        this.accessTokenRepository = accessTokenRepository;
        this.oAuthFlowTokenRepository = oAuthFlowTokenRepository;
        this.botUserRepository = botUserRepository;
        this.authUri = this.spotifyApi.authorizationCodeUri()
                .scope(
                        AuthorizationScope.PLAYLIST_READ_COLLABORATIVE,
                        AuthorizationScope.PLAYLIST_READ_PRIVATE,
                        AuthorizationScope.USER_LIBRARY_READ,
                        AuthorizationScope.USER_READ_RECENTLY_PLAYED,
                        AuthorizationScope.USER_TOP_READ
                )
                .show_dialog(false)
                .build().execute().toString();
    }

    public void processCallback(@NotNull String code, @NotNull UUID token) throws IOException, ParseException, SpotifyWebApiException {
        AuthorizationCodeCredentials credentials = this.spotifyApi.authorizationCode(code).build().execute();
        SpotifyApi userApi = new SpotifyApi.Builder()
                .setAccessToken(credentials.getAccessToken())
                .setRefreshToken(credentials.getRefreshToken())
                .build();

        Optional<Long> optionalTokenOwner = this.oAuthFlowTokenRepository.getDiscordUserFromToken(token);
        if (optionalTokenOwner.isEmpty()) throw new IllegalStateException("Token does not exist or has expired.");
        long tokenOwner = optionalTokenOwner.get();

        se.michaelthelin.spotify.model_objects.specification.User spotifyUser = userApi.getCurrentUsersProfile().build().execute();

        // todo check if a user with the client ID already exists
        BotUser botUser = new BotUser(tokenOwner, spotifyUser.getId(), credentials.getRefreshToken());

        this.botUserRepository.save(botUser);
        this.accessTokenRepository.saveSpotifyApi(botUser.getSpotifyId(), userApi);
    }

    public @NotNull String createAuthUri(@NotNull User user) {
        return this.authUri + "&state=" + this.oAuthFlowTokenRepository.createToken(user.getIdLong());
    }
}
