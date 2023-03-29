package pink.zak.discord.singaconverterbot.service.trackconverter.pipeline;

/**
 * @param inconclusive whether the match is inconclusive based on the result (e.g. the required metadata was not present)
 * @param invalidated  whether the match is completely invalid based on the result
 * @param matchRatio   the chance of the spotify/singa track matching (0-100)
 */
public record StepResult(boolean inconclusive, boolean invalidated, int matchRatio) {
}
