package com.frogparser.cli.repository;

import com.frogparser.cli.domain.RunTimeVariableDB;
import com.frogparser.flow.executor.domain.FlowExecution;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class RunTimeVariableRepository {

    private final AtomicLong nextId = new AtomicLong(0);
    private final Map<Long, RunTimeVariableDB> runTimeVariableMap = new ConcurrentHashMap<>();

    public RunTimeVariableDB save(RunTimeVariableDB runTimeVariable) {
        final RunTimeVariableDB newVariable = new RunTimeVariableDB()
                .setId(nextId.incrementAndGet())
                .setFlowExecution(runTimeVariable.getFlowExecution())
                .setName(runTimeVariable.getName())
                .setVariableType(runTimeVariable.getVariableType())
                .setValue(runTimeVariable.getValue());

        runTimeVariableMap.put(newVariable.getId(), newVariable);
        return newVariable;
    }

    public Optional<RunTimeVariableDB> findByNameAndFlowExecutionId(String name, Long id) {
        return runTimeVariableMap
                .values()
                .stream()
                .filter(v -> Objects.equals(v.getName(), name) && Optional
                        .of(v)
                        .map(RunTimeVariableDB::getFlowExecution)
                        .map(FlowExecution::getId)
                        .filter(eid -> Objects.equals(eid, id))
                        .isPresent())
                .findFirst();
    }

    public Optional<RunTimeVariableDB> findById(Long id) {
        return Optional.ofNullable(runTimeVariableMap.get(id));
    }

    public List<RunTimeVariableDB> findByFlowExecutionId(Long id) {
        return runTimeVariableMap
                .values()
                .stream()
                .filter(v -> Optional
                        .of(v)
                        .map(RunTimeVariableDB::getFlowExecution)
                        .map(FlowExecution::getId)
                        .filter(eid -> Objects.equals(eid, id))
                        .isPresent())
                .toList();
    }

    public Optional<RunTimeVariableDB> deleteById(Long id) {
        return Optional.ofNullable(runTimeVariableMap.remove(id));
    }

    public List<RunTimeVariableDB> deleteByFlowExecutionId(Long id) {
        var list = findByFlowExecutionId(id);
        list.forEach(v -> runTimeVariableMap.remove(v.getId()));
        return list;
    }

}
