package com.frogparser.cli.command;

import com.frogparser.cli.domain.RunTimeVariableDB;
import com.frogparser.cli.domain.SaveVariableFormatEnum;
import com.frogparser.cli.repository.FlowExecutionRepository;
import com.frogparser.cli.repository.RunTimeVariableRepository;
import com.frogparser.cli.service.FlowExecutionManager;
import com.frogparser.common.exception.ApplicationException;
import com.frogparser.flow.domain.Flow;
import com.frogparser.flow.domain.FlowContent;
import com.frogparser.flow.executor.domain.FlowExecution;
import com.frogparser.flow.executor.domain.FlowExecutionStateEnum;
import com.frogparser.flow.executor.service.StringToFlowContentMapper;
import com.frogparser.flow.executor.service.runtime_variable.RunTimeVariableConvertToCSVService;
import com.frogparser.flow.executor.service.runtime_variable.RunTimeVariableConvertToJsonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "--execute-flow", description = "Execute flow")
@Component
public class ExecuteFlowSubCommand implements Callable<Integer> {

    private static final Logger log = LoggerFactory.getLogger(ExecuteFlowSubCommand.class);

    private final FlowExecutionManager flowExecutionManager;
    private final FlowExecutionRepository flowExecutionRepository;
    private final RunTimeVariableConvertToCSVService runTimeVariableConvertToCSVService;
    private final RunTimeVariableConvertToJsonService runTimeVariableConvertToJsonService;
    private final RunTimeVariableRepository runTimeVariableRepository;
    private final StringToFlowContentMapper stringToFlowContentMapper;

    public ExecuteFlowSubCommand(FlowExecutionManager flowExecutionManager,
                                 FlowExecutionRepository flowExecutionRepository,
                                 RunTimeVariableConvertToCSVService runTimeVariableConvertToCSVService,
                                 RunTimeVariableConvertToJsonService runTimeVariableConvertToJsonService,
                                 RunTimeVariableRepository runTimeVariableRepository,
                                 StringToFlowContentMapper stringToFlowContentMapper) {
        this.flowExecutionManager = flowExecutionManager;
        this.flowExecutionRepository = flowExecutionRepository;
        this.runTimeVariableConvertToCSVService = runTimeVariableConvertToCSVService;
        this.runTimeVariableConvertToJsonService = runTimeVariableConvertToJsonService;
        this.runTimeVariableRepository = runTimeVariableRepository;
        this.stringToFlowContentMapper = stringToFlowContentMapper;
    }

    @CommandLine.Option(names = {"--flow-file", "-ff"}, required = true, paramLabel = "FLOW_FILE")
    private File flowFile;

    @CommandLine.Option(names = {"--save-variables", "-sv"}, required = true)
    private Boolean saveVariables;

    @CommandLine.ArgGroup(exclusive = false, validate = true, multiplicity = "0..*")
    private List<SaveVariableArgGroup> saveVariableArgGroupList = new ArrayList<>();

    public File getFlowFile() {
        return flowFile;
    }

    public ExecuteFlowSubCommand setFlowFile(File flowFile) {
        this.flowFile = flowFile;
        return this;
    }

    public Boolean getSaveVariables() {
        return saveVariables;
    }

    public ExecuteFlowSubCommand setSaveVariables(Boolean saveVariables) {
        this.saveVariables = saveVariables;
        return this;
    }

    public List<SaveVariableArgGroup> getSaveVariableArgGroupList() {
        return saveVariableArgGroupList;
    }

    public ExecuteFlowSubCommand setSaveVariableArgGroupList(List<SaveVariableArgGroup> saveVariableArgGroupList) {
        this.saveVariableArgGroupList = saveVariableArgGroupList;
        return this;
    }

    @Override
    public Integer call() throws Exception {

        int result;

        try {

            final String flowContentAsString = readFileToString(this.flowFile);

            final FlowContent flowContent = stringToFlowContentMapper.map(flowContentAsString);

            final Flow flow = new Flow()
                    .setId(1L)
                    .setName("Frog Parser Flow")
                    .setDescription("Frog Parser Flow")
                    .setContent(flowContent);

            final Long flowExecutionId = flowExecutionManager.execute(flow);

            final FlowExecution flowExecution = flowExecutionRepository.findById(flowExecutionId).orElseThrow();

            if (FlowExecutionStateEnum.FINISHED.equals(flowExecution.getState())) {

                saveVariables(flowExecutionId);

            } else {
                final String statusValue = Optional.of(flowExecution)
                        .map(FlowExecution::getState)
                        .map(FlowExecutionStateEnum::getValue)
                        .orElse("Unknown");

                throw new ApplicationException(String.format("Flow execution has not been finished successfully, state '%s'", statusValue));
            }

            System.out.println("flow execution finished");
            log.info("flow execution finished");

            result = 0;

        } catch (Throwable throwable) {
            System.err.println("Cannot execute flow: " + throwable.getMessage());
            log.error(throwable.getMessage(), throwable);
            result = -1;
        }

        return result;
    }

    private void saveVariables(final Long flowExecutionId) {
        Optional.ofNullable(saveVariableArgGroupList)
                .orElse(new ArrayList<>())
                .forEach(variableArgGroup -> {

                    final Optional<RunTimeVariableDB> runTimeVariableOptional = runTimeVariableRepository.findByNameAndFlowExecutionId(variableArgGroup.getName(), flowExecutionId);

                    if (runTimeVariableOptional.isPresent()) {

                        final RunTimeVariableDB runTimeVariable = runTimeVariableOptional.get();

                        final byte[] bytes;

                        if (SaveVariableFormatEnum.json.equals(variableArgGroup.getFormat())) {

                            bytes = runTimeVariableConvertToJsonService.convert(runTimeVariable.getValue());

                        } else if (SaveVariableFormatEnum.csv.equals(variableArgGroup.getFormat())) {

                            bytes = runTimeVariableConvertToCSVService.convert(runTimeVariable.getValue());

                        } else {
                            throw new ApplicationException(String.format("Variable file format '%s' is not supported", variableArgGroup.getFormat()));
                        }

                        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes)) {

                            final CopyOption[] copyOptions = Boolean.TRUE.equals(variableArgGroup.getReplaceExistingFile()) ? new CopyOption[]{StandardCopyOption.REPLACE_EXISTING} : new CopyOption[]{};

                            Files.copy(inputStream, variableArgGroup.getFile().toPath(), copyOptions);

                        } catch (IOException e) {
                            throw new ApplicationException(String.format("Could not save variable '%s'", variableArgGroup.getName()), e);
                        }

                    } else {
                        log.warn("Variable '%s' not found".formatted(variableArgGroup.getName()));
                    }

                });
    }

    private static String readFileToString(File file) {
        try {
            final byte[] encoded = Files.readAllBytes(file.toPath());
            return new String(encoded, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ApplicationException(String.format("Can not read file, error '%s'", e.getMessage()), e);
        }
    }

}
