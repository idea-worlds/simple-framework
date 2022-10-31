package ${package}.adapter.listener;

import ${package}.api.${sample}Api;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ${sample}EventHandler {
    private final ${sample}Api api;

}