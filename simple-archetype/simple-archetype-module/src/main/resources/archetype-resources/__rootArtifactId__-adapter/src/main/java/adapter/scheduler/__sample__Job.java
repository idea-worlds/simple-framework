package ${package}.adapter.scheduler;

import ${package}.api.${sample}Api;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ${sample}Job {
    private final ${sample}Api api;

}