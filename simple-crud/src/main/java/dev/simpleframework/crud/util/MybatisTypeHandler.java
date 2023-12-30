package dev.simpleframework.crud.util;

import dev.simpleframework.crud.ModelField;
import dev.simpleframework.util.Jsons;
import dev.simpleframework.util.Strings;
import lombok.Setter;
import org.apache.ibatis.type.*;

import java.net.URL;
import java.sql.*;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.*;

public final class MybatisTypeHandler {
    private static final ArrayTypeHandler ARRAY = new ArrayTypeHandler();
    private static final CollectionTypeHandler<?> LIST = new ListTypeHandler();
    private static final CollectionTypeHandler<?> SET = new SetTypeHandler();

    public static TypeHandler<?> typeHandler(ModelField<?> field) {
        Class<?> fieldType = field.fieldType();
        TypeHandler<?> typedHandler = typeHandler(fieldType);
        if (typedHandler instanceof JsonTypeHandler handler) {
            handler.setComponentType(field.fieldComponentType());
        }
        return typedHandler;
    }

    public static TypeHandler<?> typeHandler(Class<?> fieldType) {
        if (fieldType.isPrimitive()
                || String.class.isAssignableFrom(fieldType)
                || Boolean.class.isAssignableFrom(fieldType)
                || Enum.class.isAssignableFrom(fieldType)
                || Number.class.isAssignableFrom(fieldType)
                || Character.class.isAssignableFrom(fieldType)
                || Calendar.class.isAssignableFrom(fieldType)
                || Date.class.isAssignableFrom(fieldType)
                || TemporalAccessor.class.isAssignableFrom(fieldType)
                || URL.class.isAssignableFrom(fieldType)) {
            return null;
        }
        if (fieldType.isArray()) {
            return ARRAY;
        }
        if (List.class.isAssignableFrom(fieldType)) {
            return LIST;
        }
        if (Set.class.isAssignableFrom(fieldType)) {
            return SET;
        }
        return new JsonTypeHandler(fieldType);
    }

    /**
     * 将 {@param fieldName} 转为 "fieldName,jdbcType=,typeHandler="
     *
     * @param field     字段信息
     * @param fieldName 字段名
     * @return 转换后的字符串
     */
    public static String resolveFieldName(ModelField<?> field, String fieldName) {
        TypeHandler<?> typeHandler = typeHandler(field);
        if (typeHandler == null) {
            return fieldName;
        }
        JdbcType jdbcType = null;
        if (typeHandler instanceof CollectionTypeHandler<?> handler) {
            String name = handler.resolveTypeName(field.fieldComponentType());
            jdbcType = JdbcType.valueOf(name);
        } else if (typeHandler instanceof ArrayTypeHandler) {
            jdbcType = JdbcType.ARRAY;
        } else if (typeHandler instanceof JsonTypeHandler) {
            jdbcType = JdbcType.VARCHAR;
        }
        if (jdbcType == null) {
            return fieldName;
        }
        return String.format("%s,jdbcType=%s,typeHandler=%s", fieldName, jdbcType.name(), typeHandler.getClass().getName());
    }

    /**
     * 将 {@param fieldName} 转为 "fieldName,typeHandler="
     *
     * @param fieldType 字段类型
     * @param fieldName 字段名
     * @return 转换后的字符串
     */
    public static String resolveFieldName(Class<?> fieldType, String fieldName) {
        TypeHandler<?> typeHandler = typeHandler(fieldType);
        if (typeHandler == null) {
            return fieldName;
        }
        return String.format("%s,typeHandler=%s", fieldName, typeHandler.getClass().getName());
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
            if (jdbcType == null) {
                jdbcType = JdbcType.VARCHAR;
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
            List<Object> list = Arrays.asList(result);
            return new ArrayList<>(list);
        }
    }

    public static class SetTypeHandler extends CollectionTypeHandler<Set<?>> {
        @Override
        protected Set<?> parseResult(Object[] result) {
            List<Object> list = Arrays.asList(result);
            return new LinkedHashSet<>(list);
        }
    }

    public static class JsonTypeHandler extends BaseTypeHandler<Object> {
        private final Class<?> type;
        @Setter
        private Class<?> componentType;

        public JsonTypeHandler(Class<?> type) {
            this.type = type;
        }

        @Override
        public void setNonNullParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType) throws SQLException {
            String json = Jsons.write(parameter);
            ps.setString(i, json);
        }

        @Override
        public Object getNullableResult(ResultSet rs, String columnName) throws SQLException {
            return this.extractJson(rs.getString(columnName));
        }

        @Override
        public Object getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
            return this.extractJson(rs.getString(columnIndex));
        }

        @Override
        public Object getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
            return this.extractJson(cs.getString(columnIndex));
        }

        protected Object extractJson(String json) {
            if (Strings.isBlank(json)) {
                return null;
            }
            if (this.componentType == null) {
                return Jsons.read(json, this.type);
            }
            if (Map.class.isAssignableFrom(this.type)) {
                return Jsons.readWithGeneric(json, this.type, String.class, this.componentType);
            }
            return Jsons.readWithGeneric(json, this.type, this.componentType);
        }

    }

}
