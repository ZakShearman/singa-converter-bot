package pink.zak.discord.singaconverterbot.controller.commands.convert.spotify;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import org.jetbrains.annotations.NotNull;
import pink.zak.discord.singaconverterbot.cache.BotUserPlaylistCache;
import pink.zak.discord.singaconverterbot.controller.commands.convert.ConvertCommand;
import pink.zak.discord.singaconverterbot.controller.commands.convert.spotify.menu.PlaylistConvertMenu;
import pink.zak.discord.singaconverterbot.model.BotUser;
import pink.zak.discord.singaconverterbot.repository.BotUserRepository;
import pink.zak.discord.singaconverterbot.service.SpotifyUserApiService;
import pink.zak.discord.singaconverterbot.service.TrackConverterService;
import pink.zak.discord.utils.autoconfig.ButtonRegistryAutoConfiguration;
import pink.zak.discord.utils.discord.annotations.BotSubCommandComponent;
import pink.zak.discord.utils.discord.command.AutoCompletable;
import pink.zak.discord.utils.discord.command.BotSubCommand;
import pink.zak.discord.utils.listener.ButtonRegistry;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.enums.ModelObjectType;
import se.michaelthelin.spotify.model_objects.IPlaylistItem;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;
import se.michaelthelin.spotify.model_objects.specification.Track;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;

@RequiredArgsConstructor
@BotSubCommandComponent(parent = ConvertCommand.class, subCommandGroupId = "spotify", subCommandId = "playlist")
public class SpotifyPlaylistConvertSub implements BotSubCommand, AutoCompletable {
    private final BotUserRepository botUserRepository;
    private final SpotifyUserApiService apiService;

    private final BotUserPlaylistCache playlistCache;
    private final TrackConverterService trackConverterService;

    private final ScheduledExecutorService scheduler;
    private final ButtonRegistry buttonRegistry;

    @SneakyThrows
    @Override
    public void onExecute(@NotNull Member sender, @NotNull SlashCommandInteractionEvent event) {
        String playlistId = event.getOption("playlist").getAsString();
        Optional<BotUser> optionalBotUser = this.botUserRepository.findById(sender.getIdLong());
        if (optionalBotUser.isEmpty()) {
            event.reply("You need to link your Spotify account first!").setEphemeral(true).queue();
            return;
        }
        BotUser botUser = optionalBotUser.get();
        SpotifyApi spotifyApi = this.apiService.getApi(botUser);
        if (spotifyApi == null) {
            event.reply("You need to link your Spotify account first!").setEphemeral(true).queue();
            return;
        }
        List<PlaylistSimplified> playlists = this.playlistCache.getCache().get(botUser.getDiscordId(), unused -> this.retrievePlaylists(spotifyApi));

        Optional<PlaylistSimplified> optionalPlaylist = playlists.stream()
                .filter(testPlaylist -> testPlaylist.getId().equals(playlistId))
                .findFirst();

        if (optionalPlaylist.isEmpty()) {
            event.reply("Could not find a suitable playlist with name %s".formatted(playlistId)).setEphemeral(true).queue();
            return;
        }

        event.reply("Converting playlist...").queue();

        PlaylistSimplified playlist = optionalPlaylist.get();
        List<Track> tracks = new ArrayList<>();
        for (int i = 0; i < playlist.getTracks().getTotal(); i += 100) {
            Paging<PlaylistTrack> pagedTracks = spotifyApi.getPlaylistsItems(playlist.getId())
                    .offset(i).build().execute();

            for (PlaylistTrack playlistTrack : pagedTracks.getItems()) {
                IPlaylistItem playlistItem = playlistTrack.getTrack();
                if (playlistItem.getType() != ModelObjectType.TRACK) continue;
                tracks.add((Track) playlistTrack.getTrack());
            }
        }

        List<TrackConverterService.SearchResult> convertedTracks = new ArrayList<>();
        PlaylistConvertMenu menu = new PlaylistConvertMenu(this.scheduler, this.buttonRegistry, convertedTracks);
        menu.editInitialInteraction(event.getHook());

        Instant lastEditTime = Instant.now();
        for (int i = 0; i < tracks.size(); i++) {
            Track track = tracks.get(i);
            System.out.printf("Converting track: %s%n", track.getName());
            try {
                TrackConverterService.SearchResult searchResult = this.trackConverterService.convertTrack(track).get();
                if (searchResult != null) convertedTracks.add(searchResult);

                if (i == tracks.size() - 1 || Duration.between(lastEditTime, Instant.now()).toSeconds() > 3) {
                    this.updateMessage(menu, i + 1, tracks.size());
                    lastEditTime = Instant.now();
                }
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void updateMessage(PlaylistConvertMenu menu, int progress, int total) {
        menu.update(progress, total);
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event) {
        String currentInput = event.getFocusedOption().getValue();

        long userId = event.getUser().getIdLong();
        BotUser botUser = this.botUserRepository.findById(userId).orElseThrow();
        SpotifyApi api = this.apiService.getApi(botUser);

        List<PlaylistSimplified> playlists = this.playlistCache.getCache().get(event.getUser().getIdLong(), unused -> this.retrievePlaylists(api));

        if (currentInput.isEmpty()) {
            event.replyChoices(
                    playlists.stream()
                            .limit(25)
                            .map(playlist -> new Command.Choice(playlist.getName(), playlist.getId()))
                            .toList()
            ).queue();
            return;
        }

        event.replyChoices(playlists.stream()
                .sorted((o1, o2) -> FuzzySearch.ratio(currentInput, o2.getName()) - FuzzySearch.ratio(currentInput, o1.getName()))
                .filter(name -> FuzzySearch.ratio(currentInput, name.getName()) > Math.min(32, currentInput.length() * 6))
                .limit(25)
                .map(playlist -> new Command.Choice(playlist.getName(), playlist.getId()))
                .toList()).queue();
    }

    @SneakyThrows
    private List<PlaylistSimplified> retrievePlaylists(SpotifyApi api) {
        return List.of(api.getListOfCurrentUsersPlaylists().limit(50).build().execute().getItems());
    }
}
