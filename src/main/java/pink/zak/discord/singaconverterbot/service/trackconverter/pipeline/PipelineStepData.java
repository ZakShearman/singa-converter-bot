package pink.zak.discord.singaconverterbot.service.trackconverter.pipeline;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface PipelineStepData {

    /**
     * If the returned ratio is less than this, the match will be considered invalid.
     *
     * @return the minimum ratio
     */
    int minimumRatio() default 0;

    /**
     * If true, the ratio will be used to determine the match. If false, the pipeline step only has the function
     * of invalidating the match.
     *
     * @return if the ratio should be used
     */
    boolean useRatio() default true;
}
