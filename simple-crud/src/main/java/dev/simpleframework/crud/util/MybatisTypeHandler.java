package dev.simpleframework.crud.util;

import dev.simpleframework.crud.ModelField;
import org.apache.ibatis.type.ArrayTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeException;
import org.apache.ibatis.type.TypeHandler;

import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

public final class MybatisTypeHandler {
    private static final ArrayTypeHandler ARRAY = new ArrayTypeHandler();
    private static final CollectionTypeHandler<?> LIST = new ListTypeHandler();
    private static final CollectionTypeHandler<?> SET = new SetTypeHandler();

    public static TypeHandler<?> typeHandler(ModelField<?> field) {
        Class<?> fieldType = field.fieldType();
        if (fieldType.isArray()) {
            return ARRAY;
        }
        if (List.class.isAssignableFrom(fieldType)) {
            return LIST;
        }
        if (Set.class.isAssignableFrom(fieldType)) {
            return SET;
        }
        return null;
    }

    /**
     * 当字段为 Array 或者 Collection 时，将 {@param fieldName} 转为 "fieldName,jdbcType=,typeHandler="
     *
     * @param field     字段信息
     * @param fieldName 字段名
     * @return 转换后的字符串
     */
    public static String resolveFieldName(ModelField<?> field, String fieldName) {
        Class<?> componentType = field.fieldComponentType();
        if (componentType == null) {
            return fieldName;
        }
        TypeHandler<?> typeHandler = null;
        JdbcType jdbcType;
        Class<?> fieldType = field.fieldType();
        if (fieldType.isArray()) {
            typeHandler = ARRAY;
            jdbcType = JdbcType.ARRAY;
        } else {
            String name = LIST.resolveTypeName(componentType);
            jdbcType = JdbcType.valueOf(name);
        }
        if (List.class.isAssignableFrom(fieldType)) {
            typeHandler = LIST;
        }
        if (Set.class.isAssignableFrom(fieldType)) {
            typeHandler = SET;
        }
        return typeHandler == null ? fieldName :
                String.format("%s,jdbcType=%s,typeHandler=%s", fieldName, jdbcType.name(), typeHandler.getClass().getName());
    }

    static abstract class CollectionTypeHandler<T> extends ArrayTypeHandler {

        @Override
        public void setParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType) throws SQLException {
            if (parameter == null) {
                try {
                    ps.setNull(i, JdbcType.ARRAY.TYPE_CODE);
                } catch (SQLException e) {
                    throw new TypeException("Error setting null for parameter #" + i + " with JdbcType " + JdbcType.ARRAY + " ." +
                            " Try setting a different JdbcType for this parameter or a different jdbcTypeForNull configuration property." +
                            " Cause: " + e, e);
                }
            } else {
                try {
                    this.setNonNullParameter(ps, i, parameter, jdbcType);
                } catch (Exception e) {
                    throw new TypeException("Error setting non null for parameter #" + i + " with JdbcType " + jdbcType.name() + " ." +
                            " Try setting a different JdbcType for this parameter or a different configuration property." +
                            " Cause: " + e, e);
                }
            }
        }

        @Override
        public void setNonNullParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType) throws SQLException {
            if (!Collection.class.isAssignableFrom(parameter.getClass())) {
                throw new TypeException("CollectionType Handler requires Collection parameter and does not support type " + parameter.getClass());
            }
            Object[] objects = ((Collection<?>) parameter).toArray();
            Array array = ps.getConnection().createArrayOf(jdbcType.name(), objects);
            ps.setArray(i, array);
            array.free();
        }

        @Override
        protected Object extractArray(Array array) throws SQLException {
            Object result = super.extractArray(array);
            return result == null ? null : this.parseResult((Object[]) result);
        }

        @Override
        public String resolveTypeName(Class<?> type) {
            return super.resolveTypeName(type);
        }

        protected abstract T parseResult(Object[] result);

    }

    public static class ListTypeHandler extends CollectionTypeHandler<List<?>> {
        @Override
        protected List<?> parseResult(Object[] result) {
            return Arrays.asList(result);
        }
    }

    public static class SetTypeHandler extends CollectionTypeHandler<Set<?>> {
        @Override
        protected Set<?> parseResult(Object[] result) {
            List<Object> list = Arrays.asList(result);
            return new LinkedHashSet<>(list);
        }
    }

}
