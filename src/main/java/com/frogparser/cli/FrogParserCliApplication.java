package com.frogparser.cli;

import com.frogparser.cli.command.FrogParserCliCommand;
import com.frogparser.cli.configuration.FlowExecutorConfiguration;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import picocli.CommandLine;

@SpringBootApplication
@EnableConfigurationProperties
@Import(FlowExecutorConfiguration.class)
public class FrogParserCliApplication implements CommandLineRunner, ExitCodeGenerator {

    private final CommandLine.IFactory factory;
    private final FrogParserCliCommand frogParserCliCommand;
    private int exitCode;

    public FrogParserCliApplication(CommandLine.IFactory factory, FrogParserCliCommand frogParserCliCommand) {
        this.factory = factory;
        this.frogParserCliCommand = frogParserCliCommand;
    }

    public static void main(String[] args) {
        System.exit(SpringApplication.exit(SpringApplication.run(FrogParserCliApplication.class, args)));
    }

    @Override
    public void run(String... args) throws Exception {
        exitCode = new CommandLine(frogParserCliCommand, factory).execute(args);
    }

    @Override
    public int getExitCode() {
        return exitCode;
    }
}