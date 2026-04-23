package dev.simpleframework.crud;

/**
 * 模型操作标记接口。
 * <p>
 * {@link Models} 是框架提供的标准实现，通过静态工厂方法 {@link Models#wrap(Object)} /
 * {@link Models#wrap(Class)} 创建实例，无需继承或实现本接口。
 * <p>
 * 如需自定义操作类（如限制访问范围、添加业务方法），可实现本接口并配合相应的
 * {@code BaseInsert}、{@code BaseDelete}、{@code BaseUpdate}、{@code BaseQuery} 接口，
 * 参考 {@link Models} 的实现方式直接调用 {@code XxxDefinition.exec()} 方法。
 * <p>
 * {@code @ModelScan} 的 {@code operatorClass} 属性必须是本接口的子类型。
 *
 * @param <T> POJO 实体类型
 * @author loyayz (loyayz@foxmail.com)
 * @see Models
 */
public interface ModelOperator<T> {
}
