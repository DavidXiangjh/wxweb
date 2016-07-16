////////////////////////////////////////////////////////////////////////////
// 
// Copyright (C) 2008 e-Channels CORPORATION
// 
// ALL RIGHTS RESERVED BY e-Channels CORPORATION, THIS PROGRAM
// MUST BE USED SOLELY FOR THE PURPOSE FOR WHICH IT WAS  
// FURNISHED BY e-Channels CORPORATION, NO PART OF THIS PROGRAM
// MAY BE REPRODUCED OR DISCLOSED TO OTHERS, IN ANY FORM
// WITHOUT THE PRIOR WRITTEN PERMISSION OF e-Channels CORPORATION.
// USE OF COPYRIGHT NOTICE DOES NOT EVIDENCE PUBLICATION
// OF THE PROGRAM
// 
//			e-Channels CONFIDENTIAL AND PROPRIETARY
// 
////////////////////////////////////////////////////////////////////////////
package com.ecc.liana.loaf;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.ecc.emp.core.Context;
import com.ecc.emp.core.EMPException;
import com.ecc.emp.data.KeyedCollection;
import com.ecc.liana.base.LianaConstants;
import com.ecc.liana.base.Trace;
import com.ecc.liana.exception.TranFailException;

/**
 * LOAF 查询公用基类<br>
 * 实现了无数据库差异的查询接口，getQueryByPageStatement未实现
 * 
 * @version 1.0 2008-02-04
 * @author zhanghao@yuchengtech.com
 */
public abstract class LoafQueryCommon implements LoafQueryInterface {
	/**
	 * 查询的表名称
	 */
	protected String itsQueryTable;
	/**
	 * 查询结果中所有的列
	 */
	protected String itsQueryFields;
	/**
	 * 查询条件
	 */
	protected String itsQueryCondition;
	/**
	 * 排序条件
	 */
	protected String itsQueryOrder;
	/**
	 * 查询用的preparedStatement
	 */
	protected String itsQuerySql;

	/**
	 * @param context
	 * @param tableConfig
	 * @param resultKColl
	 * @param sortParam
	 *            String
	 * @throws TranFailException
	 */
	public LoafQueryCommon(Context context, TableConfig tableConfig, KeyedCollection resultKColl, String sortParam)
			throws TranFailException {
		Map dataToTableMap = tableConfig.getDataToTableFieldHashMap();
		StringBuffer querySql = new StringBuffer( "SELECT " );
		try
		{
			// 构建查询SQL的前半部分，即 SELECT xxx,xxx FROM xxx WHERE
			String tableName = tableConfig.getTableName();
			for ( Iterator iterator = dataToTableMap.keySet().iterator(); iterator.hasNext(); )
			{
				String dataField = (String) iterator.next();
				if ( resultKColl.containsKey( dataField ) )
				{
					String tableField = (String) dataToTableMap.get( dataField );
					querySql.append( tableField ).append( "," );
				}
			}
			LoafTools.removeLastComma( querySql );
			this.itsQueryFields = querySql.toString();
			this.itsQueryTable = tableName;
			querySql.append( " FROM " ).append( tableName ).append( " WHERE " );
			// 构建查询SQL的查询条件部分，即 xxx=? AND xxx=? ...
			StringBuffer conditionSql = new StringBuffer();
			List inputList = new ArrayList();
			for ( Iterator iterator = dataToTableMap.keySet().iterator(); iterator.hasNext(); )
			{
				// 将LOAF中定义了，且在context中也定义的数据，作为查询条件输入
				String dataFieldName = (String) iterator.next();
				if ( context.containsKey( dataFieldName ) )
				{
					String dataFieldValue = (String) context.getDataValue( dataFieldName );
					if ( dataFieldValue != null && dataFieldValue.length() > 0 )
					{
						String tableFieldName = (String) dataToTableMap.get( dataFieldName );
						if ( conditionSql.length() > 0 )
						{
							// 如果有多个主键，用AND分割
							conditionSql.append( " AND " );
						}
						conditionSql.append( tableFieldName ).append( "=?" );
						inputList.add( dataFieldValue );
					}
				}
			}
			this.itsQueryCondition = conditionSql.toString();
			querySql.append( conditionSql );
			// 构建排序部分，即 ORDER BY ...
			if ( sortParam != null )
			{
				querySql.append( ' ' ).append( sortParam );
			}
			this.itsQueryOrder = sortParam;
			this.itsQuerySql = querySql.toString();
		}
		catch ( EMPException ex )
		{
			throw new TranFailException( LianaConstants.DEFAULT_ERROR_CODE, ex );
		}
	}

	/**
	 * Method getQueryStatement.
	 * 
	 * @param connection
	 *            Connection
	 * @param input List
	 * @return PreparedStatement
	 * @throws SQLException
	 * @see com.ecc.liana.loaf.LoafQueryInterface#getQueryStatement(Connection)
	 */
	public PreparedStatement getQueryStatement( Connection connection, List input ) throws SQLException
	{
		PreparedStatement statement = connection.prepareStatement( itsQuerySql );
		// 填入输入数据
		int inputSize = input.size();
		for ( int i = 1; i <= inputSize; i++ )
		{
			statement.setString( i, (String) input.get( i - 1 ) );
		}
		return statement;
	}

	/**
	 * Method getCount.
	 * 
	 * @param connection
	 *            Connection
	 * @param input List
	 * @return int
	 * @see com.ecc.liana.loaf.LoafQueryInterface#getCount(Connection)
	 */
	public int getCount( Connection connection, List input )
	{
		int count = 0;
		ResultSet resultSet = null;
		PreparedStatement statement = null;
		try
		{
			statement = connection.prepareStatement( "SELECT COUNT(*) FROM ( " + itsQuerySql + ")" );
			// 填入输入数据
			int inputSize = input.size();
			for ( int i = 1; i <= inputSize; i++ )
			{
				statement.setString( i, (String) input.get( i - 1 ) );
			}
			resultSet = statement.executeQuery();
			if ( resultSet.next() )
			{
				// 获取总记录条数
				count = resultSet.getInt( 1 );
			}
		}
		catch ( SQLException ex )
		{
			Trace.logError( Trace.COMPONENT_MAPPING, "翻页查询获取总结果数量出错", ex );
		}
		finally
		{
			try
			{
				if ( resultSet != null )
				{
					resultSet.close();
				}
				if ( statement != null )
				{
					statement.close();
				}
			}
			catch ( SQLException ex )
			{
				Trace.logError( Trace.COMPONENT_JDBC, "结果集关闭时出错", ex );
			}
		}
		return count;
	}

	/**
	 * Method getQueryByPageStatement.
	 * 
	 * @param connection
	 *            Connection
	 * @param input List
	 * @param beginPos
	 *            int
	 * @param showNum
	 *            int
	 * @return PreparedStatement
	 * @throws SQLException
	 * @see com.ecc.liana.loaf.LoafQueryInterface#getQueryByPageStatement(Connection, int, int)
	 */
	public abstract PreparedStatement getQueryByPageStatement( Connection connection, List input, int beginPos,
			int showNum ) throws SQLException;

	/**
	 * Method toString.
	 * 
	 * @return String
	 */
	public String toString()
	{
		return itsQuerySql;
	}
}
