package com.frogparser.cli.service;

import com.frogparser.cli.domain.flow_execution.AppFlowExecutionSettings;
import com.frogparser.cli.repository.FlowExecutionRepository;
import com.frogparser.flow.domain.Flow;
import com.frogparser.flow.domain.FlowExecutionId;
import com.frogparser.flow.exception.FlowExecutionException;
import com.frogparser.flow.executor.domain.FlowExecution;
import com.frogparser.flow.executor.domain.FlowExecutionStateEnum;
import com.frogparser.flow.executor.service.FlowExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.function.Supplier;

@Service
public class FlowExecutionManager {

    private static final Logger log = LoggerFactory.getLogger(FlowExecutionManager.class);

    private final AppFlowExecutionSettings flowExecutionSettings;
    private final FlowExecutionRepository flowExecutionRepository;
    private final FlowExecutorService flowExecutorService;

    public FlowExecutionManager(AppFlowExecutionSettings flowExecutionSettings, FlowExecutionRepository flowExecutionRepository, FlowExecutorService flowExecutorService) {
        this.flowExecutionSettings = flowExecutionSettings;
        this.flowExecutionRepository = flowExecutionRepository;
        this.flowExecutorService = flowExecutorService;
    }

    public Long execute(Flow flow) {

        final FlowExecution flowExecution = flowExecutionRepository.save(new FlowExecution()
                .setState(FlowExecutionStateEnum.PENDING)
                .setVariables(new ArrayList<>()));

        flowExecution.setStarted(LocalDateTime.now());
        flowExecution.setState(FlowExecutionStateEnum.RUNNING);

        boolean success;
        String message;
        String stackTrace;

        final Supplier<Boolean> flowExecutionRunning = () -> FlowExecutionStateEnum.RUNNING.equals(flowExecution.getState());

        try {

            final FlowExecutionId flowExecutionId = new FlowExecutionId(flowExecution.getId());
            flowExecutorService.execute(flowExecutionSettings, flowExecutionId, flowExecutionRunning, flow);

            message = "";
            stackTrace = "";
            success = true;

        } catch (FlowExecutionException e) {
            message = e.getMessage();
            stackTrace = getStackTrace(e);
            success = false;
        }

        flowExecution.setMessage(message);
        flowExecution.setStackTrace(stackTrace);

        if (flowExecutionRunning.get()) {
            if (success) {
                flowExecution.setState(FlowExecutionStateEnum.FINISHED);
            } else {
                flowExecution.setState(FlowExecutionStateEnum.FAILED);
            }
        }

        flowExecution.setFinished(LocalDateTime.now());

        return flowExecution.getId();
    }

    private String getStackTrace(Throwable t) {

        String result = "";

        try (StringWriter sw = new StringWriter()) {

            final PrintWriter pw = new PrintWriter(sw);
            t.printStackTrace(pw);

            result = sw.toString();

        } catch (IOException ex) {
            log.error(String.format("Can not get exception stack trace: '%s'", ex.getMessage()), t);
        }

        return result;
    }
}
