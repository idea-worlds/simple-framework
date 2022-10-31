package ${package}.api;

import ${package}.model.${sample}ModifyArgs;
import ${package}.model.${sample}PageQueryArgs;
import ${package}.model.${sample}Response;
import dev.simpleframework.core.PageResponse;

/**
 * 接口
 */
public interface ${sample}Api {

    /**
     * 新增
     */
    Long add${sample}(${sample}ModifyArgs args);

    /**
     * 修改
     */
    void update${sample}(Long id, ${sample}ModifyArgs args);

    /**
     * 删除
     */
    void remove${sample}(Long id);

    /**
     * 根据 id 查询
     */
    ${sample}Response find${sample}(Long id);

    /**
     * 分页查询
     */
    PageResponse<${sample}Response> page${sample}(${sample}PageQueryArgs query);

}
