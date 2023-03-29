package pink.zak.discord.singaconverterbot.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.Pipeline;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;
import pink.zak.discord.singaconverterbot.model.dto.SingaSongDto;
import pink.zak.discord.singaconverterbot.service.trackconverter.pipeline.PipelineResult;
import pink.zak.discord.singaconverterbot.service.trackconverter.pipeline.TrackConverterPipeline;
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
    private final TrackConverterPipeline pipeline;

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
                        for (int i = 0; i < size && i < 5; i++) { // only get the top 5 for now. I don't trust my fuzzy search properly - needs more filtering, artists, etc..
                            singaSongs.add(SingaSongDto.fromJson(songsNode.get(i)));
                        }

                        // NOTE: The bestResult will also include an invalid result if no valid results were found.
                        // This is to provide better feedback when something goes wrong.
                        SingaSongDto bestResult = null;
                        PipelineResult bestResultPipeline = null;

                        for (SingaSongDto possibleSong : singaSongs) {
                            PipelineResult result = this.pipeline.evaluateTrackMatch(track, possibleSong);
                            if (bestResultPipeline == null || result.matchRatio() > bestResultPipeline.matchRatio()) {
                                bestResult = possibleSong;
                                bestResultPipeline = result;
                            }
                        }

                        return new SearchResult(track, bestResult, bestResultPipeline);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                }).exceptionally(throwable -> {
                    throwable.printStackTrace();
                    return null;
                });
    }

    public record SearchResult(@NotNull Track spotifyTrack, @Nullable SingaSongDto singaTrack, @Nullable PipelineResult pipelineResult) {
    }
}
