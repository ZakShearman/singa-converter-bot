package pink.zak.discord.singaconverterbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pink.zak.discord.singaconverterbot.model.BotUser;

@Repository
public interface BotUserRepository extends JpaRepository<BotUser, Long> {

    @Query("UPDATE BotUser botUser SET botUser.spotifyRefreshToken = :spotifyRefreshToken WHERE botUser.discordId = :discordId")
    @Modifying
    void updateRefreshToken(long discordId, String spotifyRefreshToken);
}
