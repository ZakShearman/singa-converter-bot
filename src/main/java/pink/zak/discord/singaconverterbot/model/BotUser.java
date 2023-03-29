package pink.zak.discord.singaconverterbot.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;



@Entity

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BotUser {

    @Id
    @Column(name = "discord_id")
    private long discordId;

    @Column(name = "spotify_id")
    private String spotifyId;

    @Column(name = "spotify_refresh_token")
    private String spotifyRefreshToken;
}
