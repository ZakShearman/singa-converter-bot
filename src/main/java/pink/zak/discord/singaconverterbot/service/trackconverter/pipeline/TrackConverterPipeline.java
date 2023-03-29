package pink.zak.discord.singaconverterbot.service.trackconverter.pipeline;

import org.springframework.stereotype.Component;
import pink.zak.discord.singaconverterbot.model.dto.SingaSongDto;
import pink.zak.discord.singaconverterbot.service.trackconverter.pipeline.step.TrackArtistStep;
import pink.zak.discord.singaconverterbot.service.trackconverter.pipeline.step.TrackNameStep;
import pink.zak.discord.singaconverterbot.service.trackconverter.pipeline.step.TrackYearStep;
import se.michaelthelin.spotify.model_objects.specification.Track;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class TrackConverterPipeline {
    private final List<PipelineStep> steps = List.of(
            new TrackYearStep(),
            new TrackNameStep(),
            new TrackArtistStep()
    );

    /**
     * @param spotifyTrack the Spotify track to compare
     * @param singaTrack   the Singa track to compare
     * @return the match ratio (chance of being the same track), 0-100
     */
    public PipelineResult evaluateTrackMatch(Track spotifyTrack, SingaSongDto singaTrack) {
        Map<String, StepResult> stepResults = new HashMap<>();
        Set<Integer> ratios = new HashSet<>();

        boolean invalidated = false;

        for (PipelineStep step : this.steps) {
            StepResult result = step.process(spotifyTrack, singaTrack);
            stepResults.put(step.getClass().getSimpleName(), result);

            if (result.inconclusive()) continue;
            if (result.invalidated()) {
                invalidated = true;
                break;
            }

            PipelineStepData data = step.getClass().getAnnotation(PipelineStepData.class);
            if (data.useRatio() && result.matchRatio() < data.minimumRatio()) {
                invalidated = true;
                break;
            }

            if (data.useRatio()) ratios.add(result.matchRatio());
        }

        int averageRatio;
        if (invalidated) averageRatio = 0;
        else {
            averageRatio = ratios.stream().mapToInt(Integer::intValue).sum() / ratios.size();
        }

        return new PipelineResult(averageRatio >= 50, averageRatio, stepResults);
    }
}
