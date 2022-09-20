package pink.zak.discord.singaconverterbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties
@ConfigurationPropertiesScan
public class SingaConverterBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(SingaConverterBotApplication.class, args);
    }

}
