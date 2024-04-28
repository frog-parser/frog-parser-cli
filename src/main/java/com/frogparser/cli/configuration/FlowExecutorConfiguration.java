package com.frogparser.cli.configuration;

import com.frogparser.cli.domain.flow_execution.AppFlowExecutionSettings;
import com.frogparser.cli.repository.FlowExecutionRepository;
import com.frogparser.cli.repository.RunTimeVariableRepository;
import com.frogparser.cli.service.FlowExecutionManager;
import com.frogparser.cli.service.FlowVariableSaveServiceImpl;
import com.frogparser.flow.executor.domain.web_driver_settings.WebDriverSettings;
import com.frogparser.flow.executor.service.FlowExecutorService;
import com.frogparser.flow.executor.service.SeleniumFindByService;
import com.frogparser.flow.executor.service.StringToFlowContentMapper;
import com.frogparser.flow.executor.service.command.AbstractCommandExecutorService;
import com.frogparser.flow.executor.service.os.ZombieProcessKillerService;
import com.frogparser.flow.executor.service.runtime_variable.RunTimeVariableConvertToCSVService;
import com.frogparser.flow.executor.service.runtime_variable.RunTimeVariableConvertToJsonService;
import com.frogparser.flow.executor.service.validator.AcceptLanguageHeaderValidatorService;
import com.frogparser.flow.executor.service.validator.FlowExecutionIdValidator;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        AcceptLanguageHeaderValidatorService.class,
        AppFlowExecutionSettings.class,
        FlowExecutionIdValidator.class,
        FlowExecutionManager.class,
        FlowExecutionRepository.class,
        FlowExecutorService.class,
        FlowVariableSaveServiceImpl.class,
        RunTimeVariableConvertToCSVService.class,
        RunTimeVariableConvertToJsonService.class,
        RunTimeVariableRepository.class,
        SeleniumFindByService.class,
        StringToFlowContentMapper.class,
        WebDriverSettings.class,
        ZombieProcessKillerService.class
})
@ComponentScan(basePackageClasses = {AbstractCommandExecutorService.class})
public class FlowExecutorConfiguration {

}
