package pink.zak.discord.singaconverterbot.controller.rest;

import lombok.RequiredArgsConstructor;
import org.apache.hc.core5.http.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import pink.zak.discord.singaconverterbot.service.SpotifyOAuthService;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/spotify/callback")
@RequiredArgsConstructor
public class SpotifyCallbackController {
    private static final Logger LOGGER = LoggerFactory.getLogger(SpotifyCallbackController.class);

    private final SpotifyOAuthService oAuthService;

    @GetMapping
    public void parseCallback(@RequestParam String code, @RequestParam UUID state) {
        if (code == null || code.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No code provided");
        if (state == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No state token provided");

        try {
            this.oAuthService.processCallback(code, state);
        } catch (IOException | ParseException e) {
            LOGGER.error("Error processing callback: ", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while processing the callback", e);
        } catch (SpotifyWebApiException e) {
            LOGGER.error("Error processing callback: ", e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "An error occurred while processing the callback", e);
        }
    }
}
