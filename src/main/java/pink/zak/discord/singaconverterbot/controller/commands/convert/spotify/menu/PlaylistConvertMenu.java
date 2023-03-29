package pink.zak.discord.singaconverterbot.controller.commands.convert.spotify.menu;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import pink.zak.discord.singaconverterbot.model.dto.SingaSongDto;
import pink.zak.discord.singaconverterbot.service.TrackConverterService;
import pink.zak.discord.utils.listener.ButtonRegistry;
import pink.zak.discord.utils.message.PageableButtonEmbedMenu;
import se.michaelthelin.spotify.model_objects.specification.Track;

import java.awt.*;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Function;

public class PlaylistConvertMenu extends PageableButtonEmbedMenu {
    private static final Function<String, String> SPOTIFY_URL_GENERATOR = id -> "https://s.zak.pink/t/" + id;

    private final List<TrackConverterService.SearchResult> convertedTracks;

    private int progress = 0;
    private int total = 1;

    public PlaylistConvertMenu(ScheduledExecutorService scheduler, ButtonRegistry buttonRegistry, List<TrackConverterService.SearchResult> convertedTracks) {
        super(scheduler, buttonRegistry);

        this.convertedTracks = convertedTracks;
        this.maxPage = Math.max(1, this.convertedTracks.size() / 20);
    }

    @Override
    public MessageEmbed createPage(int page) {
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle("Converted Songs")
                .setColor(Color.MAGENTA)
                .setTimestamp(Instant.now())
                .setFooter("Page %s/%s (%s items, %s%% processed)"
                        .formatted(this.currentPage.get(), this.maxPage, this.convertedTracks.size(), (int) ((double) this.progress / this.total * 100)));

        StringBuilder descriptionBuilder = new StringBuilder();

        for (int i = 20 * (page - 1); i < 20 * page && i < this.convertedTracks.size(); i++) {
            TrackConverterService.SearchResult searchResult = this.convertedTracks.get(i);
            Track track = searchResult.spotifyTrack();
            SingaSongDto singaSong = searchResult.singaTrack();

            boolean embolden = searchResult.pipelineResult().match();

            if (embolden) descriptionBuilder.append("**");
            descriptionBuilder
                    .append("[%s](%s)".formatted(track.getName(), SPOTIFY_URL_GENERATOR.apply(track.getId())))
                    .append(" -> ");

            if (singaSong != null)
                descriptionBuilder.append("[%s](%s)".formatted(singaSong.name(), singaSong.uri()));
            else
                descriptionBuilder.append("*No results :|*");

            descriptionBuilder.append(" (").append(searchResult.pipelineResult().matchRatio()).append("/100)");

            if (embolden) descriptionBuilder.append("**");

            descriptionBuilder.append("\n");
        }

        System.out.printf("Description length: %s/%s%n", descriptionBuilder.length(), MessageEmbed.DESCRIPTION_MAX_LENGTH);
        embedBuilder.setDescription(descriptionBuilder);

        return embedBuilder.build();
    }

    public void update(int progress, int total) {
        this.progress = progress;
        this.total = total;

        int previousMaxPage = this.maxPage;
        this.maxPage = (int) Math.ceil(this.convertedTracks.size() / 20.0);
        System.out.println("previousMaxPage: %s maxPage: %s currentPage: %s".formatted(previousMaxPage, this.maxPage, this.currentPage.get()));

        super.drawPage(this.currentPage.get()); // redraw every time as we provide stats in the footer

        if (this.maxPage > previousMaxPage && this.currentPage.get() == previousMaxPage) {
            super.updateButtonStates(this.currentPage.get());
        }

    }
}
