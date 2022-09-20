package pink.zak.discord.singaconverterbot.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import org.springframework.stereotype.Service;
import pink.zak.discord.singaconverterbot.model.dto.SingaArtistDto;
import pink.zak.discord.singaconverterbot.model.dto.SingaSongDto;
import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;
import se.michaelthelin.spotify.model_objects.specification.Track;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class TrackConverterService {
    private static final String SINGA_API_URL = "https://api.singa.com/v1.4";
    private static final Function<String, URI> SEARCH_URL_CREATOR = query -> URI.create("%s/search/?market=GB&page=1&page_size=18&search=%s&type=songs"
            .formatted(SINGA_API_URL, query));

    private final HttpClient httpClient = HttpClient.newHttpClient();

    private final ObjectMapper objectMapper;

    public CompletableFuture<SearchResult> convertTrack(Track track) {
        String query = URLEncoder.encode(track.getName() + " " + track.getArtists()[0].getName(), Charset.defaultCharset());
        HttpRequest request = HttpRequest.newBuilder(SEARCH_URL_CREATOR.apply(query)).build();

        return this.httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(body -> {
                    try {
                        JsonNode baseNode = this.objectMapper.readTree(body);
                        if (!baseNode.has("songs")) return null;
                        JsonNode songsNode = baseNode.get("songs").get("items");
                        if (songsNode == null) return null;

                        int size = songsNode.size();

                        List<SingaSongDto> singaSongs = new ArrayList<>();
                        for (int i = 0; i < size && i < 3; i++) { // only get the top 3 for now. I don't trust my fuzzy search properly - needs more filtering, artists, etc..
                            singaSongs.add(SingaSongDto.fromJson(songsNode.get(i)));
                        }

                        SingaSongDto singaSong = null;
                        int maxMatch = -1;
                        for (SingaSongDto possibleSong : singaSongs) {
                            int ratio = this.createRatio(track, possibleSong);
                            if (ratio > maxMatch) {
                                singaSong = possibleSong;
                                maxMatch = ratio;
                            }
                        }

                        return new SearchResult(track, singaSong, maxMatch);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                }).exceptionally(throwable -> {
                    throwable.printStackTrace();
                    return null;
                });
    }

    private int createRatio(Track track, SingaSongDto singaSong) {
        int ratio = FuzzySearch.ratio(track.getName(), singaSong.name());
        if (ratio == 0) return 0;

        if (!this.anyArtistsMatch(track, singaSong)) ratio -= 30;
        if (ratio == 0) return 0;

        int spotifyYear = Integer.parseInt(track.getAlbum().getReleaseDate().substring(0, 4)); // Spotify may have limited precision, format YYYY-MM-dd???

        if (spotifyYear != singaSong.year()) ratio -= 50;
        if (ratio == 0) return 0;

        return Math.max(0, Math.min(ratio, 100));
    }

    private boolean anyArtistsMatch(Track track, SingaSongDto singaSongDto) {
        List<SingaArtistDto> singaArtists = singaSongDto.artists();

        for (ArtistSimplified spotifyArtist : track.getArtists()) {
            for (SingaArtistDto singaArtist : singaArtists) {
                if (FuzzySearch.ratio(spotifyArtist.getName(), singaArtist.name()) > 70) return true;
            }
        }
        return false;
    }

    public record SearchResult(Track track, SingaSongDto singaSong, int matchRatio) {
    }
}
