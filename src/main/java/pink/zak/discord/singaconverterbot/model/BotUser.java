package pink.zak.discord.singaconverterbot.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

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
