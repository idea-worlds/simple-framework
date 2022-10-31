package ${package}.infra.exception;

/**
 * 异常：${sample} 不存在
 */
public class ${sample}NotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ${sample}NotFoundException(Long id) {
        super(String.format("%s [%s] cannot be found", "${sample}", id));
    }

}
