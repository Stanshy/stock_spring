package com.chris.fin_shark.common.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * PostgreSQL JSONB <-> Map<String, Object> TypeHandler
 */
@MappedTypes(Map.class)
@MappedJdbcTypes(JdbcType.OTHER)
public class JsonbTypeHandler extends BaseTypeHandler<Map<String, Object>> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i,
                                    Map<String, Object> parameter, JdbcType jdbcType) throws SQLException {
        try {
            String json = OBJECT_MAPPER.writeValueAsString(parameter);
            ps.setObject(i, json, Types.OTHER);
        } catch (JsonProcessingException e) {
            throw new SQLException("Failed to convert Map to JSON string", e);
        }
    }

    @Override
    public Map<String, Object> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String json = rs.getString(columnName);
        return toMap(json);
    }

    @Override
    public Map<String, Object> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String json = rs.getString(columnIndex);
        return toMap(json);
    }

    @Override
    public Map<String, Object> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String json = cs.getString(columnIndex);
        return toMap(json);
    }

    private Map<String, Object> toMap(String json) throws SQLException {
        if (json == null || json.isEmpty()) {
            return new HashMap<>();
        }
        try {
            //noinspection unchecked
            return OBJECT_MAPPER.readValue(json, Map.class);
        } catch (IOException e) {
            throw new SQLException("Failed to convert JSON string to Map", e);
        }
    }
}
