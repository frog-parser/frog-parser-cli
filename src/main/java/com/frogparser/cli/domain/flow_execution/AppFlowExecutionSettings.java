package com.frogparser.cli.domain.flow_execution;

import com.frogparser.flow.executor.domain.FlowExecutionSettings;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "frog.flow.execution")
public class AppFlowExecutionSettings extends FlowExecutionSettings {

}
