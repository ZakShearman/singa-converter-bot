package pink.zak.discord.singaconverterbot.model.dto;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.Instant;

public record SingaArtistDto(int id, String hash, Instant created, Instant updated, String name, String slug,
                             boolean isFeaturing) {

    public static SingaArtistDto fromJson(JsonNode node) {
        return new SingaArtistDto(
                node.get("id").asInt(),
                node.get("hash").asText(),
                Instant.parse(node.get("created").asText()),
                Instant.parse(node.get("updated").asText()),
                node.get("name").asText(),
                node.get("slug").asText(),
                node.get("is_featuring").asBoolean()
        );
    }
}
