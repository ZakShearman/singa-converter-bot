package pink.zak.discord.singaconverterbot.service.trackconverter.pipeline;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

public record PipelineResult(boolean match, int matchRatio, @NotNull Map<String, StepResult> stepResults) {

}
