package pink.zak.discord.singaconverterbot.controller.commands.convert;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import org.jetbrains.annotations.NotNull;
import pink.zak.discord.utils.discord.annotations.BotCommandComponent;
import pink.zak.discord.utils.discord.command.BotCommand;

@BotCommandComponent(name = "convert")
public class ConvertCommand implements BotCommand {

    @Override
    public void onExecute(@NotNull Member sender, @NotNull SlashCommandInteractionEvent event) {

    }

    @Override
    public @NotNull CommandData createCommandData() {
        return Commands.slash("convert", "Convert your tracks and playlists to Singa")
                .addSubcommandGroups(
                        new SubcommandGroupData(
                                "spotify", "Convert your Spotify tracks and playlists to Singa"
                        ).addSubcommands(
                                new SubcommandData("track", "Convert a Spotify track to Singa")
                                        .addOption(OptionType.STRING, "track", "The Spotify track to convert", true)
                        ).addSubcommands(
                                new SubcommandData("playlist", "Convert a Spotify playlist to Singa")
                                        .addOption(OptionType.STRING, "playlist", "The Spotify playlist to convert", true, true)
                        )
                );
    }
}
