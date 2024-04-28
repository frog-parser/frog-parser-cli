package com.frogparser.cli.service;

import com.frogparser.common.exception.ApplicationException;
import com.frogparser.common.exception.NotFoundException;
import com.frogparser.cli.domain.RunTimeVariableDB;
import com.frogparser.cli.repository.FlowExecutionRepository;
import com.frogparser.cli.repository.RunTimeVariableRepository;
import com.frogparser.flow.domain.FlowExecutionId;
import com.frogparser.flow.domain.runtime_variable.AbstractRunTimeVariable;
import com.frogparser.flow.domain.runtime_variable.RunTimeVariableType;
import com.frogparser.flow.exception.CommandExecutionException;
import com.frogparser.flow.executor.domain.FlowExecution;
import com.frogparser.flow.executor.service.AbstractFlowVariableSaveService;
import com.frogparser.flow.executor.service.validator.FlowExecutionIdValidator;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class FlowVariableSaveServiceImpl extends AbstractFlowVariableSaveService {

    private final FlowExecutionIdValidator flowExecutionIdValidator;
    private final FlowExecutionRepository flowExecutionRepository;
    private final RunTimeVariableRepository runTimeVariableRepository;

    public FlowVariableSaveServiceImpl(FlowExecutionIdValidator flowExecutionIdValidator,
                                       FlowExecutionRepository flowExecutionRepository,
                                       RunTimeVariableRepository runTimeVariableRepository) {
        this.flowExecutionIdValidator = flowExecutionIdValidator;
        this.flowExecutionRepository = flowExecutionRepository;
        this.runTimeVariableRepository = runTimeVariableRepository;
    }

    @Override
    public void saveVariable(FlowExecutionId flowExecutionId, String runTimeVariableName, AbstractRunTimeVariable<?> runTimeVariable) throws CommandExecutionException {
        flowExecutionIdValidator.validate(flowExecutionId);

        final FlowExecution flowExecution = flowExecutionRepository.findById(flowExecutionId.getId())
                .orElseThrow(() -> new NotFoundException("Flow execution not found"));

        if (supportedClasses.contains(runTimeVariable.getClass())) {

            final Optional<RunTimeVariableDB> existingRunTimeVariableOptional = runTimeVariableRepository.findByNameAndFlowExecutionId(runTimeVariableName, flowExecutionId.getId());

            final Class<? extends AbstractRunTimeVariable<?>> clazz = getClazz(runTimeVariable);

            final RunTimeVariableType runTimeVariableType = RunTimeVariableType.getType(clazz)
                    .orElseThrow(() -> new ApplicationException("Unknown runtime variable type: '%s'".formatted(runTimeVariable.getClass().getSimpleName())));

            if (existingRunTimeVariableOptional.isEmpty()) {

                final RunTimeVariableDB newRunTimeVariableDB = new RunTimeVariableDB()
                        .setFlowExecution(flowExecution)
                        .setName(runTimeVariableName)
                        .setVariableType(runTimeVariableType)
                        .setValue(runTimeVariable);

                runTimeVariableRepository.save(newRunTimeVariableDB);

            } else {

                final RunTimeVariableDB existingRunTimeVariable = existingRunTimeVariableOptional.get();

                existingRunTimeVariable.setVariableType(runTimeVariableType);
                existingRunTimeVariable.setValue(runTimeVariable);

            }

        } else {
            throw new CommandExecutionException("Save variable operation does not support variable of type: '%s'".formatted(runTimeVariable.getClass().getSimpleName()));
        }

    }
}
