package pink.zak.discord.singaconverterbot.controller.commands.link;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.jetbrains.annotations.NotNull;
import pink.zak.discord.singaconverterbot.service.SpotifyOAuthService;
import pink.zak.discord.utils.discord.annotations.BotCommandComponent;
import pink.zak.discord.utils.discord.command.BotCommand;

@RequiredArgsConstructor
@BotCommandComponent(name = "link")
public class LinkCommand implements BotCommand {

    @Override
    public void onExecute(@NotNull Member sender, @NotNull SlashCommandInteractionEvent event) {
        // should not be able to get here
    }

    @Override
    public @NotNull CommandData createCommandData() {
        return Commands.slash("link", "Link an account within the bot's profile of you")
                .addSubcommands(
                        new SubcommandData("spotify", "Link your Spotify account")
                )
                .setGuildOnly(false);
    }
}
