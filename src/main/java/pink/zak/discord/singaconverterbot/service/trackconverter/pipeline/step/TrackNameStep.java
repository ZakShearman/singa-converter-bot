package pink.zak.discord.singaconverterbot.service.trackconverter.pipeline.step;

import me.xdrop.fuzzywuzzy.FuzzySearch;
import org.jetbrains.annotations.NotNull;
import pink.zak.discord.singaconverterbot.model.dto.SingaSongDto;
import pink.zak.discord.singaconverterbot.service.trackconverter.pipeline.PipelineStep;
import pink.zak.discord.singaconverterbot.service.trackconverter.pipeline.PipelineStepData;
import pink.zak.discord.singaconverterbot.service.trackconverter.pipeline.StepResult;
import se.michaelthelin.spotify.model_objects.specification.Track;

@PipelineStepData(minimumRatio = 50)
public class TrackNameStep implements PipelineStep {

    @Override
    public @NotNull StepResult process(@NotNull Track spotifyTrack, @NotNull SingaSongDto singaTrack) {
        String spotifyName = spotifyTrack.getName();
        String singaName = singaTrack.name();

        // If names are empty we want to invalidate, not skip the step.
        if (spotifyName == null || spotifyName.isEmpty() || singaName == null || singaName.isEmpty()) {
            return new StepResult(false, true, 0);
        }

        // TODO evaluate the use of ratio
        int ratio = FuzzySearch.ratio(spotifyName, singaName);

        return new StepResult(false, false, ratio);
    }
}
