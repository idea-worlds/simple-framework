package ${package}.app.converter;

import ${package}.model.${sample}ModifyArgs;
import ${package}.model.${sample}Response;
import ${package}.infra.data.${sample};

/**
 * 数据转换器
 */
public final class ${sample}Converter {

    public static ${sample}Response toDto(${sample} data) {
        ${sample}Response result = new ${sample}Response();
        result.setId(data.getId());
        // todo  data -> dto
        return result;
    }

    public static ${sample} toData(${sample}ModifyArgs args) {
        ${sample} result = new ${sample}();
        // todo  args -> data
        return result;
    }

}
