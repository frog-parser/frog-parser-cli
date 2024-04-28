package com.frogparser.cli.command;

import org.springframework.stereotype.Component;
import picocli.CommandLine;

@CommandLine.Command(
        name = "frog-parser-cli",
        description = "Frog Parser Command Line Interface",
        subcommands = {
                ExecuteFlowSubCommand.class
        }
)
@Component
public class FrogParserCliCommand {
    @CommandLine.Option(
            names = {"--spring.config.location"},
            paramLabel = "Config File",
            description = "the target config file",
            required = false,
            hidden = true)
    private String springConfigLocation;

}
