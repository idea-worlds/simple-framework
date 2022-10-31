package ${package}.app.executor;

import ${package}.app.converter.${sample}Converter;
import ${package}.model.${sample}PageQueryArgs;
import ${package}.model.${sample}Response;
import ${package}.infra.data.${sample};
import ${package}.infra.repo.${sample}Repo;
import dev.simpleframework.core.PageResponse;
import dev.simpleframework.crud.core.Page;
import dev.simpleframework.crud.core.QueryConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 通用查询（简单查询）执行器
 */
@Component
@RequiredArgsConstructor
public class ${sample}CommonQuery {
    private final ${sample}Repo repo;

    public ${sample}Response findById(Long id) {
        ${sample} data = this.repo.findById(id);
        return ${sample}Converter.toDto(data);
    }

    public PageResponse<${sample}Response> findPage(${sample}PageQueryArgs query) {
        QueryConfig config = QueryConfig.of();
        // todo  query -> config
        // config.addCondition();
        Page<${sample}> page = this.repo.pageByConditions(query.getPageNum(), query.getPageSize(), config);
        return page.toResponse(${sample}Converter::toDto);
    }

}
