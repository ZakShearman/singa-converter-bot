package pink.zak.discord.singaconverterbot.service;

import lombok.RequiredArgsConstructor;
import org.apache.hc.core5.http.ParseException;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pink.zak.discord.singaconverterbot.model.BotUser;
import pink.zak.discord.singaconverterbot.repository.BotUserRepository;
import pink.zak.discord.singaconverterbot.repository.UserSpotifyApiRepository;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class SpotifyUserApiService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SpotifyUserApiService.class);

    private final UserSpotifyApiRepository userSpotifyApiRepository;
    private final BotUserRepository botUserRepository;

    private final SpotifyApi spotifyApi;

    public @Nullable SpotifyApi getApi(BotUser user) {
        String clientId = user.getSpotifyId();
        return this.userSpotifyApiRepository.getCache().get(clientId, unused -> {
            SpotifyApi userApi = new SpotifyApi.Builder()
                    .setClientSecret(this.spotifyApi.getClientSecret())
                    .setClientId(this.spotifyApi.getClientId())
                    .setRefreshToken(user.getSpotifyRefreshToken())
                    .build();


            try {
                AuthorizationCodeCredentials credentials = userApi.authorizationCodeRefresh().build().execute();
                userApi.setAccessToken(credentials.getAccessToken());

                // update the refresh token in the database if it has changed
                if (credentials.getRefreshToken() != null && !user.getSpotifyRefreshToken().equals(credentials.getRefreshToken()))
                    this.botUserRepository.updateRefreshToken(user.getDiscordId(), credentials.getRefreshToken());

                return userApi;
            } catch (IOException | SpotifyWebApiException | ParseException e) {
                LOGGER.error("Failed to refresh access token for user " + user.getDiscordId(), e);
                return null;
            }
        });
    }
}
