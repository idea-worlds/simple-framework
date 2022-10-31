package ${package}.event;

import ${package}.model.${sample}ModifyArgs;
import lombok.Data;

@Data
public class ${sample}UpdatedEvent {

    private Long id;
    private ${sample}ModifyArgs args;

    public static ${sample}UpdatedEvent of(Long id, ${sample}ModifyArgs args) {
        ${sample}UpdatedEvent event = new ${sample}UpdatedEvent();
        event.setId(id);
        event.setArgs(args);
        return event;
    }

}
