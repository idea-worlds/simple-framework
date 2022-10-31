package ${package}.domain;

import ${package}.infra.data.${sample};
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ${sample}Entity {

    private boolean isNew = true;
    private Long id;
    private ${sample} data;

    public static ${sample}Entity create() {
        return new ${sample}Entity();
    }

    public static ${sample}Entity create(Long id, ${sample} data) {
        ${sample}Entity entity = new ${sample}Entity();
        entity.isNew = false;
        entity.id = id;
        entity.data = data;
        return entity;
    }

    public Long id() {
        if (this.id == null && this.data != null) {
            this.id = this.data.getId();
        }
        return this.id;
    }

    public ${sample} data() {
        return this.data;
    }

    public ${sample}Entity data(${sample} data) {
        data.setId(this.id);
        if(this.data == null) {
            this.data = data;
            return this;
        }
        // todo set fields
        // this.data = data;
        return this;
    }

    public boolean isNew() {
        return this.isNew;
    }

}
