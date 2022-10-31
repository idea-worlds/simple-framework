package ${package}.infra.repo;

import ${package}.domain.${sample}Entity;
import ${package}.infra.data.${sample};
import dev.simpleframework.crud.core.Page;
import dev.simpleframework.crud.core.QueryConfig;

/**
 * 仓储接口
 */
public interface ${sample}Repo {

    /**
     * 根据 id 查询
     */
    ${sample}Entity findEntity(Long id);

    /**
     * 保存（无 id 新增，有 id 修改）
     *
     * @param entity 实体
     */
    void save(${sample}Entity entity);

    /**
     * 根据 id 删除
     */
    void deleteById(Long id);

    /**
     * 根据 id 查询
     */
    ${sample} findById(Long id);

    /**
     * 根据条件查询分页
     */
    Page<${sample}> pageByConditions(int pageNum, int pageSize, QueryConfig config);

}
