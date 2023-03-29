package pink.zak.discord.singaconverterbot.controller.commands.convert.spotify;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;
import pink.zak.discord.singaconverterbot.controller.commands.convert.ConvertCommand;
import pink.zak.discord.singaconverterbot.model.dto.SingaArtistDto;
import pink.zak.discord.singaconverterbot.model.dto.SingaSongDto;
import pink.zak.discord.singaconverterbot.service.TrackConverterService;
import pink.zak.discord.singaconverterbot.service.trackconverter.pipeline.PipelineResult;
import pink.zak.discord.singaconverterbot.service.trackconverter.pipeline.StepResult;
import pink.zak.discord.utils.discord.annotations.BotSubCommandComponent;
import pink.zak.discord.utils.discord.command.BotSubCommand;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;
import se.michaelthelin.spotify.model_objects.specification.Track;

import java.awt.*;
import java.util.Arrays;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@BotSubCommandComponent(parent = ConvertCommand.class, subCommandGroupId = "spotify", subCommandId = "track")
public class SpotifyTrackConvertSub implements BotSubCommand {
    private final TrackConverterService trackConverterService;
    private final SpotifyApi spotifyApi;

    @Override
    public void onExecute(@NotNull Member member, @NotNull SlashCommandInteractionEvent event) {
        String trackId = event.getOption("track").getAsString();

        this.spotifyApi.getTrack(trackId).build().executeAsync().thenAccept(track -> {
            this.trackConverterService.convertTrack(track).thenAccept(searchResult -> {
                event.replyEmbeds(this.createEmbed(searchResult)).queue();
            }).exceptionally(throwable -> {
                event.reply("An error occurred while trying to convert this track.").queue();
                throwable.printStackTrace();
                return null;
            });
        }).exceptionally(throwable -> {
            event.reply("An error occurred while trying to convert this track.").queue();
            throwable.printStackTrace();
            return null;
        });
    }

    private MessageEmbed createEmbed(TrackConverterService.SearchResult searchResult) {
        PipelineResult pipelineResult = searchResult.pipelineResult();
        Track spotifyTrack = searchResult.spotifyTrack();
        SingaSongDto singaTrack = searchResult.singaTrack();

        EmbedBuilder builder = new EmbedBuilder()
                .setColor(pipelineResult.match() ? Color.GREEN : Color.RED)
                .setTitle((pipelineResult.match() ? "Match found for " : "No match found for ") + spotifyTrack.getName())
                .setDescription("""
                        __Spotify__
                        Title: %s
                        Artists: %s
                        Release Year: %s
                                                
                        __Singa__
                        Title: %s
                        Artists: %s
                        Release Year: %s
                                                
                        __Pipeline__
                        Total Match Ratio: %s
                        %s
                        """
                        .formatted(
                                spotifyTrack.getName(),
                                Arrays.stream(spotifyTrack.getArtists()).map(ArtistSimplified::getName).collect(Collectors.joining(", ")),
                                spotifyTrack.getAlbum().getReleaseDate().substring(0, 4),

                                singaTrack == null ? "No results :|" : singaTrack.name(),
                                singaTrack == null ? "No results :|" : singaTrack.artists().stream().map(SingaArtistDto::name).collect(Collectors.joining(", ")),
                                singaTrack == null ? "No results :|" : singaTrack.year(),

                                pipelineResult.matchRatio(),
                                this.formatStepResults(pipelineResult.stepResults())
                        ));

        return builder.build();
    }

    private String formatStepResults(Map<String, StepResult> stepResults) {
        StringJoiner builder = new StringJoiner("\n");
        for (Map.Entry<String, StepResult> entry : stepResults.entrySet()) {
            builder.add(entry.getKey() + ": " + entry.getValue());
        }
        return builder.toString();
    }
}
