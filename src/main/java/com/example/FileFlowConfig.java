package com.example;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.file.FileReadingMessageSource;
import org.springframework.integration.file.FileWritingMessageHandler;
import org.springframework.integration.file.filters.SimplePatternFileListFilter;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

import java.io.File;

@Configuration
public class FileFlowConfig {

    private static final String INPUT_DIR = "input";
    private static final String OUTPUT_DIR = "output";

    // Channel to read files
    @Bean
    public MessageChannel fileInputChannel() {
        return new DirectChannel();
    }

    // Channel after writing
    @Bean
    public MessageChannel fileOutputChannel() {
        return new DirectChannel();
    }

    // Channel to read the written file again
    @Bean
    public MessageChannel fileReadAgainChannel() {
        return new DirectChannel();
    }

    // Step 1: Read file from "input/"
    @Bean
    public FileReadingMessageSource fileReader() {
        FileReadingMessageSource reader = new FileReadingMessageSource();
        reader.setDirectory(new File(INPUT_DIR));
        reader.setFilter(new SimplePatternFileListFilter("*.txt"));
        return reader;
    }

    // Step 2: Process file and write to "output/"
    @Bean
    @ServiceActivator(inputChannel = "fileInputChannel", outputChannel = "fileOutputChannel")
    public MessageHandler fileWriter() {
        FileWritingMessageHandler writer = new FileWritingMessageHandler(new File(OUTPUT_DIR));
        writer.setAutoCreateDirectory(true);
        writer.setExpectReply(false);
        return writer;
    }

    // Step 3: Read the newly written file again
    @Bean
    public IntegrationFlow readAgainFlow() {
        return IntegrationFlow
                .from("fileOutputChannel")
                .handle(message -> {
                    File writtenFile = (File) message.getPayload();
                    System.out.println("Reading again: " + writtenFile.getAbsolutePath());
                })
                .get();
    }
}
