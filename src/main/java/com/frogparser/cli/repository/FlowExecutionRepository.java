package com.frogparser.cli.repository;

import com.frogparser.flow.executor.domain.FlowExecution;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class FlowExecutionRepository {

    private final AtomicLong nextId = new AtomicLong(0);
    private final Map<Long, FlowExecution> flowExecutionMap = new ConcurrentHashMap<>();

    public Optional<FlowExecution> findById(Long id) {
        return Optional.ofNullable(flowExecutionMap.get(id));
    }

    public FlowExecution save(FlowExecution flowExecution) {
        final FlowExecution newFlowExecution = new FlowExecution()
                .setId(nextId.incrementAndGet())
                .setStarted(flowExecution.getStarted())
                .setFinished(flowExecution.getFinished())
                .setState(flowExecution.getState())
                .setMessage(flowExecution.getMessage())
                .setStackTrace(flowExecution.getStackTrace())
                .setVariables(flowExecution.getVariables());

        flowExecutionMap.put(newFlowExecution.getId(), newFlowExecution);
        return newFlowExecution;
    }

    public Optional<FlowExecution> deleteById(Long id) {
        return Optional.ofNullable(flowExecutionMap.remove(id));
    }

}
