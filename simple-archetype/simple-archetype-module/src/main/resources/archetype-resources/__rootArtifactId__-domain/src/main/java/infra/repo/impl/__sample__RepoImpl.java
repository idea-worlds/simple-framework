package ${package}.infra.repo.impl;

import ${package}.domain.${sample}Entity;
import ${package}.infra.data.${sample};
import ${package}.infra.exception.${sample}NotFoundException;
import ${package}.infra.repo.${sample}Repo;
import dev.simpleframework.crud.core.Page;
import dev.simpleframework.crud.core.QueryConfig;
import org.springframework.stereotype.Component;

@Component
public class ${sample}RepoImpl implements ${sample}Repo {
    private static final ${sample} dao = new ${sample}();

    @Override
    public ${sample}Entity findEntity(Long id) {
        ${sample} data = this.findById(id);
        if(data == null) {
            throw new ${sample}NotFoundException(id);
        }
        return ${sample}Entity.create(id, data);
    }

    @Override
    public void save(${sample}Entity entity) {
        ${sample} data = entity.data();
        if (entity.isNew()) {
            data.insert();
        } else {
            data.updateById();
        }
    }

    @Override
    public void deleteById(Long id) {
        dao.deleteById(id);
    }

    @Override
    public ${sample} findById(Long id) {
        return dao.findById(id);
    }

    @Override
    public Page<${sample}> pageByConditions(int pageNum, int pageSize, QueryConfig config) {
        return dao.pageByConditions(pageNum, pageSize, config);
    }

}
