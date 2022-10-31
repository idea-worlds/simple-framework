package ${package}.event;

import lombok.Data;

@Data
public class ${sample}RemovedEvent {

    private Long id;

    public static ${sample}RemovedEvent of(Long id) {
        ${sample}RemovedEvent event = new ${sample}RemovedEvent();
        event.setId(id);
        return event;
    }

}
