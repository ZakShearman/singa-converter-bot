package pink.zak.discord.singaconverterbot.controller.commands.link;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;
import pink.zak.discord.singaconverterbot.service.SpotifyOAuthService;
import pink.zak.discord.utils.discord.annotations.BotSubCommandComponent;
import pink.zak.discord.utils.discord.command.BotSubCommand;

@BotSubCommandComponent(parent = LinkCommand.class, subCommandId = "spotify")
@RequiredArgsConstructor
public class LinkSpotifySubCommand implements BotSubCommand {
    private final SpotifyOAuthService spotifyOAuthService;

    @Override
    public void onExecute(@NotNull Member sender, @NotNull SlashCommandInteractionEvent event) {
        String authUri = this.spotifyOAuthService.createAuthUri(sender.getUser());

        event.reply("Use this link to link your Spotify account: %s\n\n**Valid for 1 hour**".formatted(authUri))
                .setEphemeral(true).queue();
    }
}
