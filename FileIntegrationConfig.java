import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.file.FileReadingMessageSource;
import org.springframework.integration.file.FileWritingMessageHandler;
import org.springframework.integration.file.filters.SimplePatternFileListFilter;
import org.springframework.integration.stream.CharacterStreamReadingMessageSource;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

import java.io.File;

@Configuration
public class FileIntegrationConfig {

    private static final String INPUT_DIR = "input";
    private static final String OUTPUT_DIR = "output";

    @Bean
    public MessageChannel fileInputChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel fileOutputChannel() {
        return new DirectChannel();
    }

    @Bean
    public FileReadingMessageSource fileReader() {
        FileReadingMessageSource reader = new FileReadingMessageSource();
        reader.setDirectory(new File(INPUT_DIR));
        reader.setFilter(new SimplePatternFileListFilter("*.txt"));
        return reader;
    }

    @Bean
    @ServiceActivator(inputChannel = "fileInputChannel", outputChannel = "fileOutputChannel")
    public MessageHandler fileProcessor() {
        return message -> {
            System.out.println("Processing file: " + message.getPayload());
        };
    }

    @Bean
    @ServiceActivator(inputChannel = "fileOutputChannel")
    public MessageHandler fileWriter() {
        FileWritingMessageHandler writer = new FileWritingMessageHandler(new File(OUTPUT_DIR));
        writer.setAutoCreateDirectory(true);
        writer.setExpectReply(false);
        return writer;
    }
}
