package ${package}.app.service;

import ${package}.api.${sample}Api;
import ${package}.app.executor.${sample}CommonCommand;
import ${package}.app.executor.${sample}CommonQuery;
import ${package}.model.${sample}ModifyArgs;
import ${package}.model.${sample}PageQueryArgs;
import ${package}.model.${sample}Response;
import dev.simpleframework.core.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 接口实现门面，转发请求到具体的执行器
 */
@Service
@RequiredArgsConstructor
public class ${sample}Service implements ${sample}Api {
    private final ${sample}CommonCommand commonCommand;
    private final ${sample}CommonQuery commonQuery;

    @Override
    public Long add${sample}(${sample}ModifyArgs args) {
        return this.commonCommand.add(args);
    }

    @Override
    public void update${sample}(Long id, ${sample}ModifyArgs args) {
        this.commonCommand.update(id, args);
    }

    @Override
    public void remove${sample}(Long id) {
        this.commonCommand.removeById(id);
    }

    @Override
    public ${sample}Response find${sample}(Long id) {
        return this.commonQuery.findById(id);
    }

    @Override
    public PageResponse<${sample}Response> page${sample}(${sample}PageQueryArgs query) {
        return this.commonQuery.findPage(query);
    }

}
