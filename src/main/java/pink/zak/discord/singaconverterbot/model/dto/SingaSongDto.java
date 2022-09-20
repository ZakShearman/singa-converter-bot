package pink.zak.discord.singaconverterbot.model.dto;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.StreamSupport;

public record SingaSongDto(int id, Instant created, Instant updated, String name, String originalName, String hash,
                           String slug, int year, String language, String author, String composer,
                           List<SingaArtistDto> artists, String uri, boolean plus,
                           boolean duet, boolean explicit, boolean original, boolean cover, Duration duration) {

    public static SingaSongDto fromJson(JsonNode node) {
        return new SingaSongDto(
                node.get("id").asInt(),
                Instant.parse(node.get("created").asText()),
                Instant.parse(node.get("updated").asText()),
                node.get("name").asText(),
                node.get("original_name").asText(),
                node.get("hash").asText(),
                node.get("slug").asText(),
                node.get("year").asInt(),
                node.get("language").asText(),
                node.get("author").asText(),
                node.get("composer").asText(),
                StreamSupport.stream(node.get("artists").spliterator(), true).map(SingaArtistDto::fromJson).toList(),
                node.get("canonical_url").asText(),
                node.get("has_plus").asBoolean(),
                node.get("has_duet").asBoolean(),
                node.get("has_explicit").asBoolean(),
                node.get("has_original").asBoolean(),
                node.get("has_cover").asBoolean(),
                Duration.of(node.get("duration").asInt(), ChronoUnit.SECONDS)
        );
    }
}
