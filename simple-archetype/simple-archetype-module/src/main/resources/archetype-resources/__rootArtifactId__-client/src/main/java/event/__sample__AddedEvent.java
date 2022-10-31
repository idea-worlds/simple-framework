package ${package}.event;

import ${package}.model.${sample}ModifyArgs;
import lombok.Data;

@Data
public class ${sample}AddedEvent {

    private Long id;
    private ${sample}ModifyArgs args;

    public static ${sample}AddedEvent of(Long id, ${sample}ModifyArgs args) {
        ${sample}AddedEvent event = new ${sample}AddedEvent();
        event.setId(id);
        event.setArgs(args);
        return event;
    }

}
