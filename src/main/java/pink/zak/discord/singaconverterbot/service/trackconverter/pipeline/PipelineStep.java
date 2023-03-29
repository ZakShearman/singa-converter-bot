package pink.zak.discord.singaconverterbot.service.trackconverter.pipeline;

import org.jetbrains.annotations.NotNull;
import pink.zak.discord.singaconverterbot.model.dto.SingaSongDto;
import se.michaelthelin.spotify.model_objects.specification.Track;

public interface PipelineStep {

    @NotNull StepResult process(@NotNull Track spotifyTrack, @NotNull SingaSongDto singaTrack);
}
