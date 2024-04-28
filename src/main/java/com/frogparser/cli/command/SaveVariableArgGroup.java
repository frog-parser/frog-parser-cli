package com.frogparser.cli.command;

import com.frogparser.cli.domain.SaveVariableFormatEnum;
import picocli.CommandLine;

import java.io.File;

public class SaveVariableArgGroup {
    @CommandLine.Option(names = "-format", required = true, paramLabel = "FORMAT", defaultValue = "json")
    private SaveVariableFormatEnum format = SaveVariableFormatEnum.json;

    @CommandLine.Option(names = "-name", required = true, paramLabel = "NAME")
    private String name;

    @CommandLine.Option(names = "-file", required = true, paramLabel = "FILE")
    private File file;

    @CommandLine.Option(names = "-ref", required = true, paramLabel = "REPLACE_EXISTING_FILE", defaultValue = "false")
    private Boolean replaceExistingFile = false;

    public SaveVariableFormatEnum getFormat() {
        return format;
    }

    public SaveVariableArgGroup setFormat(SaveVariableFormatEnum format) {
        this.format = format;
        return this;
    }

    public String getName() {
        return name;
    }

    public SaveVariableArgGroup setName(String name) {
        this.name = name;
        return this;
    }

    public File getFile() {
        return file;
    }

    public SaveVariableArgGroup setFile(File file) {
        this.file = file;
        return this;
    }

    public Boolean getReplaceExistingFile() {
        return replaceExistingFile;
    }

    public SaveVariableArgGroup setReplaceExistingFile(Boolean replaceExistingFile) {
        this.replaceExistingFile = replaceExistingFile;
        return this;
    }
}
