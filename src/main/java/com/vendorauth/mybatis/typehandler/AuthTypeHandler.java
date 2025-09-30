package com.vendorauth.mybatis.typehandler;

import com.vendorauth.enums.AuthType;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * MyBatis type handler for AuthType enum.
 * Handles conversion between AuthType enum and VARCHAR database column.
 */
@MappedTypes(AuthType.class)
public class AuthTypeHandler extends BaseTypeHandler<AuthType> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, AuthType parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, parameter.name());
    }

    @Override
    public AuthType getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return value == null ? null : AuthType.valueOf(value);
    }

    @Override
    public AuthType getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        return value == null ? null : AuthType.valueOf(value);
    }

    @Override
    public AuthType getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        return value == null ? null : AuthType.valueOf(value);
    }
}
