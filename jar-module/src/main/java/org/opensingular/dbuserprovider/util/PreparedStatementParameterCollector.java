package org.opensingular.dbuserprovider.util;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class PreparedStatementParameterCollector implements PreparedStatement {

    private Map<Integer, Object> parameters = new HashMap<>();

    Map<Integer, Object> getParameters() {
        return parameters;
    }

    @Override
    public ResultSet executeQuery() {
        return null;
    }

    @Override
    public int executeUpdate() {
        return 0;
    }

    @Override
    public void setNull(int parameterIndex, int sqlType) {
        parameters.put(parameterIndex, null);
    }

    @Override
    public void setBoolean(int parameterIndex, boolean x) {
        parameters.put(parameterIndex, x);
    }

    @Override
    public void setByte(int parameterIndex, byte x) {
        parameters.put(parameterIndex, x);
    }

    @Override
    public void setShort(int parameterIndex, short x) {
        parameters.put(parameterIndex, x);
    }

    @Override
    public void setInt(int parameterIndex, int x) {
        parameters.put(parameterIndex, x);
    }

    @Override
    public void setLong(int parameterIndex, long x) {
        parameters.put(parameterIndex, x);
    }

    @Override
    public void setFloat(int parameterIndex, float x) {
        parameters.put(parameterIndex, x);
    }

    @Override
    public void setDouble(int parameterIndex, double x) {
        parameters.put(parameterIndex, x);
    }

    @Override
    public void setBigDecimal(int parameterIndex, BigDecimal x) {
        parameters.put(parameterIndex, x);
    }

    @Override
    public void setString(int parameterIndex, String x) {
        parameters.put(parameterIndex, x);
    }

    @Override
    public void setBytes(int parameterIndex, byte[] x) {
        parameters.put(parameterIndex, x);
    }

    @Override
    public void setDate(int parameterIndex, Date x) {
        parameters.put(parameterIndex, x);
    }

    @Override
    public void setTime(int parameterIndex, Time x) {
        parameters.put(parameterIndex, x);
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x) {
        parameters.put(parameterIndex, x);
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, int length) {
        parameters.put(parameterIndex, x);
    }

    @Override
    public void setUnicodeStream(int parameterIndex, InputStream x, int length) {
        parameters.put(parameterIndex, x);
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, int length) {
        parameters.put(parameterIndex, x);
    }

    @Override
    public void clearParameters() {
        parameters.clear();
    }

    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType) {
        parameters.put(parameterIndex, x);
    }

    @Override
    public void setObject(int parameterIndex, Object x) {
        parameters.put(parameterIndex, x);
    }

    @Override
    public boolean execute() {
        return false;
    }

    @Override
    public void addBatch() {

    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, int length) {
        parameters.put(parameterIndex, reader);
    }

    @Override
    public void setRef(int parameterIndex, Ref x) {
        parameters.put(parameterIndex, x);
    }

    @Override
    public void setBlob(int parameterIndex, Blob x) {
        parameters.put(parameterIndex, x);
    }

    @Override
    public void setClob(int parameterIndex, Clob x) {
        parameters.put(parameterIndex, x);
    }

    @Override
    public void setArray(int parameterIndex, Array x) {
        parameters.put(parameterIndex, x);
    }

    @Override
    public ResultSetMetaData getMetaData() {
        return null;
    }

    @Override
    public void setDate(int parameterIndex, Date x, Calendar cal) {
        parameters.put(parameterIndex, x);
    }

    @Override
    public void setTime(int parameterIndex, Time x, Calendar cal) {
        parameters.put(parameterIndex, x);
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) {
        parameters.put(parameterIndex, x);
    }

    @Override
    public void setNull(int parameterIndex, int sqlType, String typeName) {
        parameters.put(parameterIndex, null);
    }

    @Override
    public void setURL(int parameterIndex, URL x) {
        parameters.put(parameterIndex, x);
    }

    @Override
    public ParameterMetaData getParameterMetaData() {
        return null;
    }

    @Override
    public void setRowId(int parameterIndex, RowId x) {
        parameters.put(parameterIndex, x);
    }

    @Override
    public void setNString(int parameterIndex, String value) {
        parameters.put(parameterIndex, value);
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value, long length) {
        parameters.put(parameterIndex, value);
    }

    @Override
    public void setNClob(int parameterIndex, NClob value) {
        parameters.put(parameterIndex, value);
    }

    @Override
    public void setClob(int parameterIndex, Reader reader, long length) {
        parameters.put(parameterIndex, reader);
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream, long length) {
        parameters.put(parameterIndex, inputStream);
    }

    @Override
    public void setNClob(int parameterIndex, Reader reader, long length) {
        parameters.put(parameterIndex, reader);
    }

    @Override
    public void setSQLXML(int parameterIndex, SQLXML xmlObject) {
        parameters.put(parameterIndex, xmlObject);
    }

    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength) {
        parameters.put(parameterIndex, x);
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, long length) {
        parameters.put(parameterIndex, x);
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, long length) {
        parameters.put(parameterIndex, x);
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, long length) {
        parameters.put(parameterIndex, reader);
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x) {
        parameters.put(parameterIndex, x);
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x) {
        parameters.put(parameterIndex, x);
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader) {
        parameters.put(parameterIndex, reader);
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value) {
        parameters.put(parameterIndex, value);
    }

    @Override
    public void setClob(int parameterIndex, Reader reader) {
        parameters.put(parameterIndex, reader);
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream) {
        parameters.put(parameterIndex, inputStream);
    }

    @Override
    public void setNClob(int parameterIndex, Reader reader) {
        parameters.put(parameterIndex, reader);
    }

    @Override
    public ResultSet executeQuery(String sql) {
        return null;
    }

    @Override
    public int executeUpdate(String sql) {
        return 0;
    }

    @Override
    public void close() {

    }

    @Override
    public int getMaxFieldSize() {
        return 0;
    }

    @Override
    public void setMaxFieldSize(int max) {

    }

    @Override
    public int getMaxRows() {
        return 0;
    }

    @Override
    public void setMaxRows(int max) {

    }

    @Override
    public void setEscapeProcessing(boolean enable) {

    }

    @Override
    public int getQueryTimeout() {
        return 0;
    }

    @Override
    public void setQueryTimeout(int seconds) {

    }

    @Override
    public void cancel() {

    }

    @Override
    public SQLWarning getWarnings() {
        return null;
    }

    @Override
    public void clearWarnings() {

    }

    @Override
    public void setCursorName(String name) {

    }

    @Override
    public boolean execute(String sql) {
        return false;
    }

    @Override
    public ResultSet getResultSet() {
        return null;
    }

    @Override
    public int getUpdateCount() {
        return 0;
    }

    @Override
    public boolean getMoreResults() {
        return false;
    }

    @Override
    public void setFetchDirection(int direction) {

    }

    @Override
    public int getFetchDirection() {
        //noinspection MagicConstant
        return 0;
    }

    @Override
    public void setFetchSize(int rows) {

    }

    @Override
    public int getFetchSize() {
        return 0;
    }

    @Override
    public int getResultSetConcurrency() {
        //noinspection MagicConstant
        return 0;
    }

    @Override
    public int getResultSetType() {
        //noinspection MagicConstant
        return 0;
    }

    @Override
    public void addBatch(String sql) {

    }

    @Override
    public void clearBatch() {

    }

    @Override
    public int[] executeBatch() {
        return new int[0];
    }

    @Override
    public Connection getConnection() {
        return null;
    }

    @Override
    public boolean getMoreResults(int current) {
        return false;
    }

    @Override
    public ResultSet getGeneratedKeys() {
        return null;
    }

    @Override
    public int executeUpdate(String sql, int autoGeneratedKeys) {
        return 0;
    }

    @Override
    public int executeUpdate(String sql, int[] columnIndexes) {
        return 0;
    }

    @Override
    public int executeUpdate(String sql, String[] columnNames) {
        return 0;
    }

    @Override
    public boolean execute(String sql, int autoGeneratedKeys) {
        return false;
    }

    @Override
    public boolean execute(String sql, int[] columnIndexes) {
        return false;
    }

    @Override
    public boolean execute(String sql, String[] columnNames) {
        return false;
    }

    @Override
    public int getResultSetHoldability() {
        return 0;
    }

    @Override
    public boolean isClosed() {
        return false;
    }

    @Override
    public void setPoolable(boolean poolable) {

    }

    @Override
    public boolean isPoolable() {
        return false;
    }

    @Override
    public void closeOnCompletion() {

    }

    @Override
    public boolean isCloseOnCompletion() {
        return false;
    }

    @Override
    public <T> T unwrap(Class<T> iface) {
        return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) {
        return false;
    }
}
