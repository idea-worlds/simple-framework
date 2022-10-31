package ${package}.app.executor;

import ${package}.app.converter.${sample}Converter;
import ${package}.model.${sample}ModifyArgs;
import ${package}.domain.${sample}Entity;
import ${package}.event.${sample}AddedEvent;
import ${package}.event.${sample}RemovedEvent;
import ${package}.event.${sample}UpdatedEvent;
import ${package}.infra.data.${sample};
import ${package}.infra.repo.${sample}Repo;
import dev.simpleframework.util.SimpleSpringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 通用命令（简单增删改）执行器
 */
@Component
@RequiredArgsConstructor
public class ${sample}CommonCommand {
    private final ${sample}Repo repo;

    public Long add(${sample}ModifyArgs args) {
        ${sample} data = ${sample}Converter.toData(args);
        ${sample}Entity entity = ${sample}Entity.create().data(data);
        this.repo.save(entity);

        ${sample}AddedEvent event = ${sample}AddedEvent.of(entity.id(), args);
        SimpleSpringUtils.publishEvent(event);
        return entity.id();
    }

    public void update(Long id, ${sample}ModifyArgs args) {
        ${sample} data = ${sample}Converter.toData(args);
        data.setId(id);

        ${sample}Entity entity = this.repo.findEntity(id).data(data);
        this.repo.save(entity);

        ${sample}UpdatedEvent event = ${sample}UpdatedEvent.of(id, args);
        SimpleSpringUtils.publishEvent(event);
    }

    public void removeById(Long id) {
        this.repo.deleteById(id);

        ${sample}RemovedEvent event = ${sample}RemovedEvent.of(id);
        SimpleSpringUtils.publishEvent(event);
    }

}
