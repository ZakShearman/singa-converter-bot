package pink.zak.discord.singaconverterbot.service.trackconverter.pipeline.step;

import org.jetbrains.annotations.NotNull;
import pink.zak.discord.singaconverterbot.model.dto.SingaSongDto;
import pink.zak.discord.singaconverterbot.service.trackconverter.pipeline.PipelineStep;
import pink.zak.discord.singaconverterbot.service.trackconverter.pipeline.PipelineStepData;
import pink.zak.discord.singaconverterbot.service.trackconverter.pipeline.StepResult;
import se.michaelthelin.spotify.model_objects.specification.AlbumSimplified;
import se.michaelthelin.spotify.model_objects.specification.Track;

@PipelineStepData(useRatio = false)
public class TrackYearStep implements PipelineStep {

    @Override
    public @NotNull StepResult process(@NotNull Track spotifyTrack, @NotNull SingaSongDto singaTrack) {
        AlbumSimplified spotifyAlbum = spotifyTrack.getAlbum();
        if (spotifyAlbum.getReleaseDate() == null || spotifyAlbum.getReleaseDate().isEmpty() || singaTrack.year() < 1000) {
            return new StepResult(true, false, 0);
        }

        int spotifyYear = Integer.parseInt(spotifyAlbum.getReleaseDate().substring(0, 4));
        int singaYear = singaTrack.year();

        int diff = Math.abs(spotifyYear - singaYear);

        // The match ratio here is not used as we only validate/invalidate the match
        if (diff <= 1) return new StepResult(false, false, 0);

        return new StepResult(false, true, 0);
    }
}
