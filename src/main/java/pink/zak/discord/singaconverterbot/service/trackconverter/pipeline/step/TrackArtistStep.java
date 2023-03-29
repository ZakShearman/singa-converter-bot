package pink.zak.discord.singaconverterbot.service.trackconverter.pipeline.step;

import me.xdrop.fuzzywuzzy.FuzzySearch;
import org.jetbrains.annotations.NotNull;
import pink.zak.discord.singaconverterbot.model.dto.SingaArtistDto;
import pink.zak.discord.singaconverterbot.model.dto.SingaSongDto;
import pink.zak.discord.singaconverterbot.service.trackconverter.pipeline.PipelineStep;
import pink.zak.discord.singaconverterbot.service.trackconverter.pipeline.PipelineStepData;
import pink.zak.discord.singaconverterbot.service.trackconverter.pipeline.StepResult;
import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;
import se.michaelthelin.spotify.model_objects.specification.Track;

import java.util.List;

@PipelineStepData
public class TrackArtistStep implements PipelineStep {

    @Override
    public @NotNull StepResult process(@NotNull Track spotifyTrack, @NotNull SingaSongDto singaTrack) {
        List<SingaArtistDto> singaArtists = singaTrack.artists();

        // Note that the length of spotify/singa artists can regularly mismatch
        int spotifyArtistsLength = spotifyTrack.getArtists().length;
        int singaArtistsLength = singaArtists.size();

        // If the Singa artists is empty, clearly there's some missing metadata
        if (spotifyArtistsLength == 0 || singaArtistsLength == 0) {
            return new StepResult(false, true, 0);
        }

        int matchingArtists = 0;

        for (ArtistSimplified spotifyArtist : spotifyTrack.getArtists()) {
            for (SingaArtistDto singaArtist : singaArtists) {
                if (FuzzySearch.ratio(spotifyArtist.getName(), singaArtist.name()) > 70) matchingArtists++;
            }
        }

        int percentageMatched = (int) Math.round((double) matchingArtists / (double) Math.max(spotifyArtistsLength, singaArtistsLength) * 100);

        return new StepResult(false, false, percentageMatched);
    }
}
