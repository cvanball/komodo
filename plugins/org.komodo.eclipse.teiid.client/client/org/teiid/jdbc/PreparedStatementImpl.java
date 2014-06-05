/*
 * JBoss, Home of Professional Open Source.
 * See the COPYRIGHT.txt file distributed with this work for information
 * regarding copyright ownership.  Some portions may be licensed
 * to Red Hat, Inc. under one or more contributor license agreements.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301 USA.
 */

package org.teiid.jdbc;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.regex.Matcher;

import javax.sql.rowset.serial.SerialArray;

import org.komodo.spi.runtime.version.TeiidServerVersion.Version;
import org.teiid.client.RequestMessage;
import org.teiid.client.RequestMessage.ResultsMode;
import org.teiid.client.RequestMessage.StatementType;
import org.teiid.client.metadata.MetadataResult;
import org.teiid.client.util.ResultsFuture;
import org.teiid.core.types.ArrayImpl;
import org.teiid.core.types.BlobImpl;
import org.teiid.core.types.ClobImpl;
import org.teiid.core.types.DataTypeManagerService.DefaultDataTypes;
import org.teiid.core.types.InputStreamFactory;
import org.teiid.core.types.JDBCSQLTypeInfo;
import org.teiid.core.types.Streamable;
import org.teiid.core.util.ReaderInputStream;
import org.teiid.core.util.TimestampWithTimezone;
import org.teiid.runtime.client.Messages;


/**
 * <p> Instances of PreparedStatement contain a SQL statement that has already been
 * compiled.  The SQL statement contained in a PreparedStatement object may have
 * one or more IN parameters. An IN parameter is a parameter whose value is not
 * specified when a SQL statement is created. Instead the statement has a placeholder
 * for each IN parameter.</p>
 * <p> The MMPreparedStatement object wraps the server's PreparedStatement object.
 * The methods in this class are used to set the IN parameters on a server's
 * preparedstatement object.</p>
 */

public class PreparedStatementImpl extends StatementImpl implements TeiidPreparedStatement {
	// sql, which this prepared statement is operating on
    protected String prepareSql;

    //map that holds parameter index to values for prepared statements
    private Map<Integer, Object> parameterMap;
    TreeMap<String, Integer> paramsByName;
    
    //a list of map that holds parameter index to values for prepared statements
    protected List<List<Object>> batchParameterList;

    // metadata
	private MetadataResult metadataResults;
    private ResultSetMetaData metadata;
    private ParameterMetaDataImpl parameterMetaData;
    
    private Calendar serverCalendar;
    private Object command;
    
    private boolean autoGeneratedKeys;

    /**
     * <p>MMPreparedStatement constructor.
     * @param Driver's connection object.
     * @param String object representing the prepared statement
     */
    PreparedStatementImpl(ConnectionImpl connection, String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        super(connection, resultSetType, resultSetConcurrency);

        if (sql == null) {
        	throw new SQLException(Messages.getString(Messages.JDBC.MMPreparedStatement_Err_prep_sql)); 
        }
        this.prepareSql = sql;

        TimeZone timezone = connection.getServerConnection().getLogonResult().getTimeZone();

        if (timezone != null && !timezone.hasSameRules(getDefaultCalendar().getTimeZone())) {
        	this.serverCalendar = Calendar.getInstance(timezone);
        }        
    }
    
    public void setAutoGeneratedKeys(boolean getAutoGeneratedKeys) {
		this.autoGeneratedKeys = getAutoGeneratedKeys;
	}

    /**
     * <p>Adds a set of parameters to this PreparedStatement object's list of commands
     * to be sent to the database for execution as a batch.
     * @throws SQLException if there is an error
     */
    @Override
    public void addBatch() throws SQLException {
        checkStatement();
    	if(batchParameterList == null){
    		batchParameterList = new ArrayList<List<Object>>();
		}
    	batchParameterList.add(getParameterValues());
		clearParameters();
    }

    /**
     * Makes the set of commands in the current batch empty.
     *
     * @throws SQLException if a database access error occurs or the
     * driver does not support batch statements
     */
    @Override
    public void clearBatch() throws SQLException {
    	if (batchParameterList != null ) {
    		batchParameterList.clear();
    	}
    }

    /**
     * <p>Clears the values set for the PreparedStatement object's IN parameters and
     * releases the resources used by those values. In general, parameter values
     * remain in force for repeated use of statement.
     * @throws SQLException if there is an error while clearing params
     */
    @Override
    public void clearParameters() throws SQLException {
        checkStatement();
        //clear the parameters list on servers prepared statement object
        if(parameterMap != null){
            parameterMap.clear();
        }
    }
    
    @Override
    public boolean execute(String sql) throws SQLException {
    	String msg = Messages.getString(Messages.JDBC.Method_not_supported); 
        throw new SQLException(msg);
    }
    
    @Override
    public void submitExecute(String sql, StatementCallback callback, RequestOptions options) throws SQLException {
    	String msg = Messages.getString(Messages.JDBC.Method_not_supported); 
        throw new SQLException(msg);
    }
    
    @Override
    public ResultSet executeQuery(String sql) throws SQLException {
    	String msg = Messages.getString(Messages.JDBC.Method_not_supported); 
        throw new SQLException(msg);
    }
    
    @Override
    public int executeUpdate(String sql) throws SQLException {
    	String msg = Messages.getString(Messages.JDBC.Method_not_supported); 
        throw new SQLException(msg);
    }
    
    @Override
    public boolean execute(String sql, int autoGeneratedKeys)
    		throws SQLException {
    	String msg = Messages.getString(Messages.JDBC.Method_not_supported); 
        throw new SQLException(msg);
    }
    
    @Override
    public int executeUpdate(String sql, int autoGeneratedKeys)
    		throws SQLException {
    	String msg = Messages.getString(Messages.JDBC.Method_not_supported); 
        throw new SQLException(msg);
    }
    
    @Override
    public boolean execute(String sql, int[] columnIndexes) throws SQLException {
    	String msg = Messages.getString(Messages.JDBC.Method_not_supported); 
        throw new SQLException(msg);
    }
    
    @Override
    public boolean execute(String sql, String[] columnNames)
    		throws SQLException {
    	String msg = Messages.getString(Messages.JDBC.Method_not_supported); 
        throw new SQLException(msg);
    }
    
    @Override
    public int executeUpdate(String sql, int[] columnIndexes)
    		throws SQLException {
    	String msg = Messages.getString(Messages.JDBC.Method_not_supported); 
        throw new SQLException(msg);
    }
    
    @Override
    public int executeUpdate(String sql, String[] columnNames)
    		throws SQLException {
    	String msg = Messages.getString(Messages.JDBC.Method_not_supported); 
        throw new SQLException(msg);
    }
    
    @Override
    public void addBatch(String sql) throws SQLException {
    	String msg = Messages.getString(Messages.JDBC.Method_not_supported); 
        throw new SQLException(msg);
    }
    
    @Override
    public void submitExecute(StatementCallback callback, RequestOptions options)
    		throws SQLException {
    	NonBlockingRowProcessor processor = new NonBlockingRowProcessor(this, callback);
    	submitExecute(ResultsMode.EITHER, options).addCompletionListener(processor);
    }
    
    public ResultsFuture<Boolean> submitExecute(ResultsMode mode, RequestOptions options) throws SQLException {
        return executeSql(new String[] {this.prepareSql}, false, mode, false, options);
    }

	@Override
    public boolean execute() throws SQLException {
        executeSql(new String[] {this.prepareSql}, false, ResultsMode.EITHER, true, null);
        return hasResultSet();
    }
    
    @Override
    public int[] executeBatch() throws SQLException {
    	if (batchParameterList == null || batchParameterList.isEmpty()) {
   	     	return new int[0];
    	}
	   	try{
	   		executeSql(new String[] {this.prepareSql}, true, ResultsMode.UPDATECOUNT, true, null);
	   	}finally{
	   		batchParameterList.clear();
	   	}
	   	return this.updateCounts;
    }

	@Override
    public ResultSet executeQuery() throws SQLException {
        executeSql(new String[] {this.prepareSql}, false, ResultsMode.RESULTSET, true, null, autoGeneratedKeys);
        return resultSet;
    }

	@Override
    public int executeUpdate() throws SQLException {
        executeSql(new String[] {this.prepareSql}, false, ResultsMode.UPDATECOUNT, true, null, autoGeneratedKeys);
        if (this.updateCounts == null) {
        	return 0;
        }
        return this.updateCounts[0];
    }
    
    @Override
    protected RequestMessage createRequestMessage(String[] commands,
    		boolean isBatchedCommand, ResultsMode resultsMode) {
    	RequestMessage message = super.createRequestMessage(commands, false, resultsMode);
    	message.setStatementType(StatementType.PREPARED);
    	message.setParameterValues(isBatchedCommand?getParameterValuesList(): getParameterValues());
    	message.setBatchedUpdate(isBatchedCommand);
		if (isGreaterThanTeiidSeven())
	    	message.setCommand(this.command);

    	return message;
    }

    @Override
    public ResultSetMetaData getMetaData() throws SQLException {

        // check if the statement is open
        checkStatement();

        if(metadata == null) {
        	if (updateCounts != null) {
        		return null;
        	} else if(resultSet != null) {
                metadata = resultSet.getMetaData();
            } else {
				Matcher matcher = StatementImpl.SHOW_STATEMENT.matcher(prepareSql);
				if (matcher.matches()) {
					this.executeShow(matcher);
					metadata = this.resultSet.getMetaData();
					this.resultSet = null;
					return metadata;
				} 
				if (getMetadataResults().getColumnMetadata() == null) {
					return null;
				}
                MetadataProvider provider = new MetadataProvider(getMetadataResults().getColumnMetadata());
                metadata = new ResultSetMetaDataImpl(provider, this.getExecutionProperty(ExecutionProperties.JDBC4COLUMNNAMEANDLABELSEMANTICS));
            }
        }

        return metadata;
    }

	private MetadataResult getMetadataResults() throws SQLException {
		if (metadataResults == null) {
			if (StatementImpl.SET_STATEMENT.matcher(prepareSql).matches() 
					|| StatementImpl.TRANSACTION_STATEMENT.matcher(prepareSql).matches()
					|| StatementImpl.SHOW_STATEMENT.matcher(prepareSql).matches()) {
				metadataResults = new MetadataResult();
			} else {
				try {
					metadataResults = this.getDQP().getMetadata(this.currentRequestID, prepareSql, Boolean.valueOf(getExecutionProperty(ExecutionProperties.ANSI_QUOTED_IDENTIFIERS)).booleanValue());
				} catch (Exception e) {
					throw new SQLException(e);
				}
			}
		}
		return metadataResults;
	}

    @Override
    public void setAsciiStream(int parameterIndex, java.io.InputStream in, int length) throws SQLException {
    	setAsciiStream(parameterIndex, in);
    }

    @Override
    public void setBigDecimal (int parameterIndex, java.math.BigDecimal value) throws SQLException {
        setObject(parameterIndex, value);
    }

    @Override
    public void setBinaryStream(int parameterIndex, java.io.InputStream in, int length) throws SQLException {
    	setBlob(parameterIndex, in);
    }

    @Override
    public void setBlob (int parameterIndex, Blob x) throws SQLException {
        setObject(parameterIndex, x);
    }

    @Override
    public void setBoolean (int parameterIndex, boolean value) throws SQLException {
        setObject(parameterIndex, Boolean.valueOf(value));
    }

    @Override
    public void setByte(int parameterIndex, byte value) throws SQLException {
        setObject(parameterIndex, Byte.valueOf(value));
    }

    @Override
    public void setBytes(int parameterIndex, byte bytes[]) throws SQLException {
    	setObject(parameterIndex, bytes);
    }

    @Override
    public void setCharacterStream (int parameterIndex, java.io.Reader reader, int length) throws SQLException {
    	setCharacterStream(parameterIndex, reader);
    }

    @Override
    public void setClob (int parameterIndex, Clob x) throws SQLException {
        setObject(parameterIndex, x);
    }

    @Override
    public void setDate(int parameterIndex, java.sql.Date value) throws SQLException {
        setDate(parameterIndex, value, null);
    }
    
    @Override
    public void setDate(int parameterIndex, java.sql.Date x ,java.util.Calendar cal) throws SQLException {
    	setDate(Integer.valueOf(parameterIndex), x, cal);
    }

    void setDate(Object parameterIndex, java.sql.Date x ,java.util.Calendar cal) throws SQLException {

        if (cal == null || x == null) {
            setObject(parameterIndex, x);
            return;
        }
                
        // set the parameter on the stored procedure
        setObject(parameterIndex, TimestampWithTimezone.createDate(x, cal.getTimeZone(), getDefaultCalendar()));
    }

    @Override
    public void setDouble(int parameterIndex, double value) throws SQLException {
        setObject(parameterIndex, new Double(value));
    }

    @Override
    public void setFloat(int parameterIndex, float value) throws SQLException {
        setObject(parameterIndex, new Float(value));
    }

    @Override
    public void setInt(int parameterIndex, int value) throws SQLException {
        setObject(parameterIndex, Integer.valueOf(value));
    }

    @Override
    public void setLong(int parameterIndex, long value) throws SQLException {
        setObject(parameterIndex, Long.valueOf(value));
    }

    @Override
    public void setNull(int parameterIndex, int jdbcType) throws SQLException {
        setObject(parameterIndex, null);
    }

    @Override
    public void setNull(int parameterIndex, int jdbcType, String typeName) throws SQLException {
        setObject(parameterIndex, null);
    }
    
    @Override
    public void setObject (int parameterIndex, Object value, int targetJdbcType, int scale) throws SQLException {
    	setObject(parameterIndex, value, targetJdbcType, scale);
    }

    void setObject (Object parameterIndex, Object value, int targetJdbcType, int scale) throws SQLException {

       if(value == null) {
            setObject(parameterIndex, null);
            return;
        }

       if(targetJdbcType != Types.DECIMAL || targetJdbcType != Types.NUMERIC) {
            setObject(parameterIndex, value, targetJdbcType);
        // Decimal and NUMERIC types correspond to java.math.BigDecimal
        } else {
            // transform the object to a BigDecimal
            BigDecimal bigDecimalObject = DataTypeTransformer.getBigDecimal(getTeiidVersion(), value);
            // set scale on the BigDecimal
            setObject(parameterIndex, bigDecimalObject.setScale(scale));
        }
    }
    
    @Override
    public void setObject(int parameterIndex, Object value, int targetJdbcType) throws SQLException {
    	setObject(Integer.valueOf(parameterIndex), value, targetJdbcType);
    }

    void setObject(Object parameterIndex, Object value, int targetJdbcType) throws SQLException {
        Object targetObject = value;

        if(value == null) {
            setObject(parameterIndex, null);
            return;
        }

        // get the java class name for the given JDBC type
        String typeName = JDBCSQLTypeInfo.getTypeName(targetJdbcType);
        DefaultDataTypes dataType = getDataTypeManager().getDataType(typeName);

        // transform the value to the target datatype
        switch (dataType) {
        case STRING:
        	targetObject = DataTypeTransformer.getString(getTeiidVersion(), value);
        	break;
        case CHAR:
        	targetObject = DataTypeTransformer.getCharacter(getTeiidVersion(), value);
        	break;
        case INTEGER:
        	targetObject = DataTypeTransformer.getInteger(getTeiidVersion(), value);
        	break;
        case BYTE:
        	targetObject = DataTypeTransformer.getByte(getTeiidVersion(), value);
        	break;
        case SHORT:
        	targetObject = DataTypeTransformer.getShort(getTeiidVersion(), value);
        	break;
        case LONG:
        	targetObject = DataTypeTransformer.getLong(getTeiidVersion(), value);
        	break;
        case FLOAT:
        	targetObject = DataTypeTransformer.getFloat(getTeiidVersion(), value);
        	break;
        case DOUBLE:
        	targetObject = DataTypeTransformer.getDouble(getTeiidVersion(), value);
        	break;
        case BOOLEAN:
        	targetObject = DataTypeTransformer.getBoolean(getTeiidVersion(), value);
        	break;
        case BIG_DECIMAL:
        	targetObject = DataTypeTransformer.getBigDecimal(getTeiidVersion(), value);
        	break;
        case TIMESTAMP:
        	targetObject = DataTypeTransformer.getTimestamp(getTeiidVersion(), value);
        	break;
        case DATE:
        	targetObject = DataTypeTransformer.getDate(getTeiidVersion(), value);
        	break;
        case TIME:
        	targetObject = DataTypeTransformer.getTime(getTeiidVersion(), value);
        	break;
        case BLOB:
        	targetObject = DataTypeTransformer.getBlob(getTeiidVersion(), value);
        	break;
        case CLOB:
        	targetObject = DataTypeTransformer.getClob(getTeiidVersion(), value);
        	break;
        case XML:
        	targetObject = DataTypeTransformer.getSQLXML(getTeiidVersion(), value);
        	break;
        case VARBINARY:
        	targetObject = DataTypeTransformer.getBytes(getTeiidVersion(), value);
        	break;
        }

        setObject(parameterIndex, targetObject);
    }
    
    @Override
    public void setObject(int parameterIndex, Object value) throws SQLException {
    	setObject(Integer.valueOf(parameterIndex), value);
    }

    void setObject(Object parameterIndex, Object value) throws SQLException {
    	if (parameterIndex instanceof String) {
	    	String s = (String)parameterIndex;
			if (paramsByName == null) {
				paramsByName = new TreeMap<String, Integer>(String.CASE_INSENSITIVE_ORDER);
				ParameterMetaDataImpl pmdi = getParameterMetaData();
				for (int i = 1; i <= pmdi.getParameterCount(); i++) {
					String name = pmdi.getParameterName(i);
					paramsByName.put(name, i);
				}
			}
			parameterIndex = paramsByName.get(s);
			if (parameterIndex == null) {
				throw new SQLException(Messages.getString(Messages.JDBC.MMCallableStatement_Param_not_found, s)); 
			}
    	}
		if ((Integer)parameterIndex < 1) {
			throw new SQLException(Messages.getString(Messages.JDBC.MMPreparedStatement_Invalid_param_index)); 
		}

        if(parameterMap == null){
            parameterMap = new TreeMap<Integer, Object>();
        }
        
        if (serverCalendar != null && value instanceof java.util.Date) {
            value = TimestampWithTimezone.create((java.util.Date)value, getDefaultCalendar().getTimeZone(), serverCalendar, value.getClass());
        }
        parameterMap.put((Integer)parameterIndex, value);
    }

    @Override
    public void setShort(int parameterIndex, short value) throws SQLException {
        setObject(parameterIndex, Short.valueOf(value));
    }

    @Override
    public void setString(int parameterIndex, String value) throws SQLException {
        setObject(parameterIndex, value);
    }

    @Override
    public void setTime(int parameterIndex, java.sql.Time value) throws SQLException {
        setTime(parameterIndex, value, null);
    }
    
    @Override
    public void setTime(int parameterIndex, java.sql.Time x, java.util.Calendar cal) throws SQLException {
    	setTime(Integer.valueOf(parameterIndex), x, cal);
    }

    void setTime(Object parameterIndex, java.sql.Time x, java.util.Calendar cal) throws SQLException {

       if (cal == null || x == null) {
           setObject(parameterIndex, x);
           return;
       }
               
       // set the parameter on the stored procedure
       setObject(parameterIndex, TimestampWithTimezone.createTime(x, cal.getTimeZone(), getDefaultCalendar()));
    }

    @Override
    public void setTimestamp(int parameterIndex, java.sql.Timestamp value) throws SQLException {
        setTimestamp(parameterIndex, value, null);
    }
    
    @Override
    public void setTimestamp(int parameterIndex, java.sql.Timestamp x, java.util.Calendar cal) throws SQLException {
    	setTimestamp(Integer.valueOf(parameterIndex), x, cal);
    }

    void setTimestamp(Object parameterIndex, java.sql.Timestamp x, java.util.Calendar cal) throws SQLException {

        if (cal == null || x == null) {
            setObject(parameterIndex, x);
            return;
        }
                
        // set the parameter on the stored procedure
        setObject(parameterIndex, TimestampWithTimezone.createTimestamp(x, cal.getTimeZone(), getDefaultCalendar()));
    }

    @Override
    public void setURL(int parameterIndex, URL x) throws SQLException {
        setObject(parameterIndex, x);
    }

    List<List<Object>> getParameterValuesList() {
    	if(batchParameterList == null || batchParameterList.isEmpty()){
    		return Collections.emptyList();
    	}
    	return new ArrayList<List<Object>>(batchParameterList);
    }
    
    List<Object> getParameterValues() {
        if(parameterMap == null || parameterMap.isEmpty()){
            return Collections.emptyList();
        }
        return new ArrayList<Object>(parameterMap.values());
    }

	@Override
    public ParameterMetaDataImpl getParameterMetaData() throws SQLException {
		if (parameterMetaData == null) {
			//TODO: some of the base implementation of ResultSetMetadata could be on the MetadataProvider
			this.parameterMetaData = new ParameterMetaDataImpl(new ResultSetMetaDataImpl(new MetadataProvider(getMetadataResults().getParameterMetadata()), this.getExecutionProperty(ExecutionProperties.JDBC4COLUMNNAMEANDLABELSEMANTICS)));
		}
		return parameterMetaData;
	}

    /**
     * Exposed for unit testing 
     */
    void setServerCalendar(Calendar serverCalendar) {
        this.serverCalendar = serverCalendar;
    }

	@Override
    public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
		setObject(parameterIndex, xmlObject);
	}

	@Override
    public void setArray(int parameterIndex, Array x) throws SQLException {
		checkSupportedVersion(Version.TEIID_7_7);
		if (x instanceof ArrayImpl) {
			setObject(parameterIndex, x);
		} else {
			setObject(parameterIndex, new SerialArray(x));
		}
	}

	@Override
	public void setAsciiStream(int parameterIndex, final InputStream x) 
		throws SQLException {
		setAsciiStream(Integer.valueOf(parameterIndex), x);
	}

	void setAsciiStream(Object parameterIndex, final InputStream x)
			throws SQLException {
		if (x == null) {
			this.setObject(parameterIndex, null);
			return;
		}
		this.setObject(parameterIndex, new ClobImpl(new InputStreamFactory() { 
			@Override
			public InputStream getInputStream() throws IOException {
				return x;
			}
		}, -1));
	}

	@Override
    public void setAsciiStream(int parameterIndex, InputStream x, long length)
			throws SQLException {
		setAsciiStream(parameterIndex, x);
	}

	@Override
    public void setBinaryStream(int parameterIndex, InputStream x)
			throws SQLException {
		setBlob(parameterIndex, x);
	}

	@Override
    public void setBinaryStream(int parameterIndex, InputStream x, long length)
			throws SQLException {
		setBinaryStream(parameterIndex, x);
	}

	@Override
    public void setBlob(int parameterIndex, final InputStream inputStream)
	throws SQLException {
		setBlob(Integer.valueOf(parameterIndex), inputStream);
	}
	
	void setBlob(Object parameterIndex, final InputStream inputStream)
			throws SQLException {
		if (inputStream == null) {
			this.setObject(parameterIndex, null);
			return;
		}
		this.setObject(parameterIndex, new BlobImpl(new InputStreamFactory() {
			@Override
			public InputStream getInputStream() throws IOException {
				return inputStream;
			}
		}));
	}

	@Override
    public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {
		setBlob(parameterIndex, inputStream);
	}

	@Override
    public void setCharacterStream(int parameterIndex, Reader reader)
			throws SQLException {
		setClob(parameterIndex, reader);
	}

	@Override
    public void setCharacterStream(int parameterIndex, Reader reader,
			long length) throws SQLException {
		setCharacterStream(parameterIndex, reader);
	}
	
	@Override
    public void setClob(int parameterIndex, final Reader reader) throws SQLException {
		setClob(Integer.valueOf(parameterIndex), reader);
	}

	void setClob(Object parameterIndex, final Reader reader) throws SQLException {
		if (reader == null) {
			this.setObject(parameterIndex, null);
			return;
		}
		this.setObject(parameterIndex, new ClobImpl(new InputStreamFactory() {
			
			@Override
			public InputStream getInputStream() throws IOException {
				return new ReaderInputStream(reader, Charset.forName(Streamable.ENCODING));
			}
		}, -1));
	}

	@Override
    public void setClob(int parameterIndex, Reader reader, long length)
			throws SQLException {
		setClob(parameterIndex, reader);
	}

	@Override
    public void setNCharacterStream(int parameterIndex, Reader value)
			throws SQLException {
		checkSupportedVersion(Version.TEIID_7_7);
		setClob(parameterIndex, value);
	}

	@Override
    public void setNCharacterStream(int parameterIndex, Reader value,
			long length) throws SQLException {
		checkSupportedVersion(Version.TEIID_7_7);
		setCharacterStream(parameterIndex, value);
	}

	@Override
    public void setNClob(int parameterIndex, NClob value) throws SQLException {
		checkSupportedVersion(Version.TEIID_7_7);
		setObject(parameterIndex, value);
	}

	@Override
    public void setNClob(int parameterIndex, Reader reader) throws SQLException {
		checkSupportedVersion(Version.TEIID_7_7);
		setClob(parameterIndex, reader);
	}

	@Override
    public void setNClob(int parameterIndex, Reader reader, long length)
			throws SQLException {
		checkSupportedVersion(Version.TEIID_7_7);
		setClob(parameterIndex, reader);
	}

	@Override
    public void setNString(int parameterIndex, String value)
			throws SQLException {
		checkSupportedVersion(Version.TEIID_7_7);
		setObject(parameterIndex, value);
	}

	@Override
    public void setRef(int parameterIndex, Ref x) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
    public void setRowId(int parameterIndex, RowId x) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
    public void setUnicodeStream(int parameterIndex, InputStream x, int length)
			throws SQLException {
		throw new UnsupportedOperationException();
	}
	
	public void setCommand(Object command) {
		this.command = command;
	}
}
