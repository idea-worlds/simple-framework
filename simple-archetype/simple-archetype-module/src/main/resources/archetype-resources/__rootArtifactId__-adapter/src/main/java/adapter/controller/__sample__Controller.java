package ${package}.adapter.controller;

import ${package}.api.${sample}Api;
import ${package}.model.${sample}ModifyArgs;
import ${package}.model.${sample}PageQueryArgs;
import ${package}.model.${sample}Response;
import dev.simpleframework.core.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/${sample.toLowerCase()}")
@RequiredArgsConstructor
public class ${sample}Controller {
    private final ${sample}Api api;

    @GetMapping(value = "/{id}")
    public ${sample}Response get(@PathVariable Long id) {
        return this.api.find${sample}(id);
    }

    @GetMapping(value = "/page")
    public PageResponse<${sample}Response> page(@ModelAttribute ${sample}PageQueryArgs query) {
        return this.api.page${sample}(query);
    }

    @PostMapping(value = "")
    public Long add(@RequestBody ${sample}ModifyArgs args) {
        return this.api.add${sample}(args);
    }

    @PutMapping("/{id}")
    public Long update(@PathVariable Long id, @RequestBody ${sample}ModifyArgs args) {
        this.api.update${sample}(id, args);
        return id;
    }

    @DeleteMapping("/{id}")
    public Long delete(@PathVariable Long id) {
        this.api.remove${sample}(id);
        return id;
    }

}
