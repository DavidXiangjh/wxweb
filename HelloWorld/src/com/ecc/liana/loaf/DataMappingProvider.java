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
import com.ecc.emp.data.IndexedCollection;
import com.ecc.emp.data.KeyedCollection;
import com.ecc.liana.base.LianaConstants;
import com.ecc.liana.base.Trace;
import com.ecc.liana.exception.TranFailException;

/**
 * Liana 数据映射公用方法提供者
 * 
 * @version 2.2 2008-01-16
 * @author zhanghao@yuchengtech.com
 */
public class DataMappingProvider {
	/**
	 * LOAF配置表，key为业务代码，value为TableConfig对象
	 * 
	 * @see TableConfig
	 */
	protected static Map TABLE_CONFIG_MAP = null;
	/**
	 * 网银异常:记录不存在
	 */
	private static final TranFailException EXCEPTION_DATA_NOT_FOUND = new TranFailException( "EBLN5001", "要查询的记录不存在" );
	/**
	 * 网银异常:主键不全
	 */
	private static final TranFailException EXCEPTION_INVALID_PK = new TranFailException( "EBLN5002", "查询主键不完整" );

	/**
	 * 禁用构造函数
	 */
	protected DataMappingProvider() {
		// 禁用构造函数
	}

	/**
	 * 从文件初始化LOAF配置
	 * 
	 * @param filePathName
	 * @throws TranFailException
	 */
	public static void initTableConfig( String filePathName ) throws TranFailException
	{
		TABLE_CONFIG_MAP = TableConfig.initFormFile( filePathName );
	}

	/**
	 * 检查操作类型
	 * 
	 * @param input
	 * @return int
	 */
	public static int getOpType( String input )
	{
		int result = Integer.parseInt( input );
		switch ( result ) {
			case LoafConstants.OP_TYPE_ADD :
			case LoafConstants.OP_TYPE_UPDATE :
			case LoafConstants.OP_TYPE_DELETE :
				return result;
			default :
				Trace.logError( Trace.COMPONENT_MAPPING, "错误的操作类型" + input );
				return LoafConstants.OP_TYPE_ADD;
		}
	}

	/**
	 * 根据交易代码，取得对应的表的主键SQL
	 * 
	 * @param businessCode
	 *            交易代码
	 * @param context
	 *            当前交易context
	 * @return 主键SQL
	 * @throws TranFailException
	 */
	public static String getPKSQL( String businessCode, Context context ) throws TranFailException
	{
		StringBuffer result = new StringBuffer();
		TableConfig tableConfig = getTableConfig( businessCode );
		// 处理所有的主键，生成SQL
		for ( Iterator iterator = tableConfig.getPrimaryKeyHashMap().keySet().iterator(); iterator.hasNext(); )
		{
			String tableKey = (String) iterator.next();
			String keyFieldValue = getPKFromContext( tableConfig, tableKey, context );
			if ( keyFieldValue != null && keyFieldValue.length() > 0 )
			{
				if ( result.length() > 0 )
				{
					// 如果有多个主键，用AND分割
					result.append( " AND " );
				}
				result.append( tableKey ).append( "='" ).append( keyFieldValue ).append( '\'' );
			}
		}
		if ( result.length() == 0 )
		{
			throw EXCEPTION_INVALID_PK;
		}
		return result.toString();
	}

	/**
	 * 取得表操作的SQL
	 * 
	 * @param opType
	 *            操作类型：1：插入；2：删除；3：修改
	 * @param businessCode
	 *            交易代码
	 * @param context
	 * @return 表操作的SQL，如果没有变更，返回空字符串
	 * @throws TranFailException
	 * @throws EMPException
	 */
	public static String getOperationSql( int opType, String businessCode, Context context ) throws TranFailException
	{
		TableConfig tableConfig = getTableConfig( businessCode );
		String tableName = tableConfig.getTableName();
		Map tableToDataHashMap = tableConfig.getTableFieldToDataHashMap();
		StringBuffer resultSql = new StringBuffer();// 表操作的SQL
		if ( LoafConstants.OP_TYPE_ADD == opType )// 新增指令
		{
			resultSql.append( "INSERT INTO " ).append( tableName ).append( '(' );
			StringBuffer insertTableFields = new StringBuffer();// 插入字段序列
			StringBuffer insertValues = new StringBuffer();// 插入值序列
			for ( Iterator iterator = tableToDataHashMap.keySet().iterator(); iterator.hasNext(); )
			{
				String tableFieldName = (String) iterator.next();
				String contextFieldName = (String) tableToDataHashMap.get( tableFieldName );
				String contextFieldValue = null;
				try
				{
					contextFieldValue = (String) context.getDataValue( contextFieldName );
				}
				catch ( EMPException ex )
				{
					contextFieldValue = "";
				}
				if ( contextFieldValue != null && contextFieldValue.length() > 0 )
				{
					insertTableFields.append( tableFieldName ).append( ',' );
					insertValues.append( '\'' ).append( contextFieldValue ).append( '\'' ).append( ',' );
				}
			}
			LoafTools.removeLastComma( insertTableFields );
			LoafTools.removeLastComma( insertValues );
			if ( insertTableFields.length() > 0 )
			{
				resultSql.append( insertTableFields ).append( ")VALUES(" ).append( insertValues ).append( ')' );
			}
		}
		else
		{
			String pkSql = getPKSQL( businessCode, context );// 主键sql语句
			if ( LoafConstants.OP_TYPE_DELETE == opType )
			{
				// 删除指令
				resultSql.append( "DELETE FROM " ).append( tableName ).append( " WHERE " ).append( pkSql );
			}
			else if ( LoafConstants.OP_TYPE_UPDATE == opType )
			{
				// 修改指令
				boolean hasChange = false;
				resultSql.append( "UPDATE " ).append( tableName ).append( " SET " );
				for ( Iterator iterator = tableToDataHashMap.keySet().iterator(); iterator.hasNext(); )
				{
					String tableFieldName = (String) iterator.next();
					if ( tableConfig.getPrimaryKeyHashMap().containsKey( tableFieldName ) )
					{
						continue;
					}
					String contextFieldName = (String) tableToDataHashMap.get( tableFieldName );
					String contextFieldValue = null;
					try
					{
						contextFieldValue = (String) context.getDataValue( contextFieldName );
					}
					catch ( EMPException ex )
					{
						// contextFieldValue = null;
					}
					if ( contextFieldValue != null )
					{
						resultSql.append( tableFieldName ).append( "='" ).append( contextFieldValue ).append( '\'' );
						resultSql.append( ',' );
						hasChange = true;
					}
				}
				if ( !hasChange )
				{
					return "";
				}
				LoafTools.removeLastComma( resultSql );
				resultSql.append( " WHERE " ).append( pkSql );
			}
		}
		return resultSql.toString();
	}

	/**
	 * 根据交易代码取得表映射定义，使用context中数据生成查询SQL，查询数据库后将查询结果填入context中
	 * 
	 * @param connection
	 * @param businessCode
	 * @param context
	 * @throws TranFailException
	 * @throws SQLException
	 */
	public static void fillTableDataIntoContext( Connection connection, String businessCode, Context context )
			throws TranFailException, SQLException
	{
		// TODO 需要优化
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		TableConfig tableConfig = getTableConfig( businessCode );
		Map tableFieldToDataHashMap = tableConfig.getTableFieldToDataHashMap();
		String queryOrgSql = getQuerySqlFromContext( businessCode, context );
		try
		{
			// 取得原始数据
			statement = connection.prepareStatement( queryOrgSql );
			resultSet = statement.executeQuery();
			if ( resultSet.next() )
			{
				for ( Iterator iterator = tableFieldToDataHashMap.keySet().iterator(); iterator.hasNext(); )
				{
					String tableField = (String) iterator.next();
					String contextFieldName = (String) tableFieldToDataHashMap.get( tableField );
					String tableValue = resultSet.getString( tableField );
					KeyedCollection contextData = (KeyedCollection) context.getDataElement();
					LoafTools.fillDataField( contextData, contextFieldName, tableValue );
				}
			}
			else
			{
				throw EXCEPTION_DATA_NOT_FOUND;
			}
		}
		finally
		{
			try
			{
				if ( statement != null )
					statement.close();
			}
			catch ( SQLException ex )
			{
				Trace.logError( Trace.COMPONENT_MAPPING, "查询失败", ex );
			}
		}
	}

	/**
	 * 取得交易定义
	 * 
	 * @param businessCode
	 *            交易代码
	 * @return OperationTableConf
	 * @throws TranFailException
	 */
	public static TableConfig getTableConfig( String businessCode ) throws TranFailException
	{
		TableConfig tableConfig = (TableConfig) TABLE_CONFIG_MAP.get( businessCode );
		if ( tableConfig == null )
		{
			throw new TranFailException( LianaConstants.DEFAULT_ERROR_CODE, "未定义的交易代码" + businessCode );
		}
		return tableConfig;
	}

	/**
	 * 根据context中的输入数据，和要输出的IndexedCollection的数据结构，生成查询语句并查询<br>
	 * 查询结果填充到IndexedCollection中
	 * 
	 * @param connection
	 * @param tableConfig
	 * @param context
	 * @param input
	 * @throws TranFailException
	 */
	public static void queryResultSet( Connection connection, TableConfig tableConfig, Context context,
			IndexedCollection input ) throws TranFailException
	{
		// 构造查询实体
		LoafQueryInterface query = LoafQueryFactory.createQueryEntity( context, tableConfig, (KeyedCollection) input
				.getDataElement() );
		List inputList = getQueryInputList( tableConfig, context );
		Trace.logDebug( Trace.COMPONENT_MAPPING, "查询SQL--->" + query );
		try
		{
			// 获取PreparedStatement
			PreparedStatement statement = query.getQueryStatement( connection, inputList );
			ResultSet resultSet = statement.executeQuery();
			LoafTools.fillResultIntoIcoll( tableConfig, resultSet, input );
		}
		catch ( SQLException ex )
		{
			throw new TranFailException( LianaConstants.DEFAULT_ERROR_CODE, ex );
		}
	}

	/**
	 * 根据context中的输入数据，和要输出的IndexedCollection的数据结构，生成查询语句并分页查询<br>
	 * 查询结果填充到IndexedCollection中
	 * 
	 * @param connection
	 * @param tableConfig
	 * @param context
	 * @param input
	 * @param beginPos
	 *            查询开始位置
	 * @param showNum
	 *            每页显示条数
	 * @param totalNumFieldName
	 *            保存总条数的Field名称
	 * @param sortParam
	 *            排序条件
	 * @throws TranFailException
	 */
	public static void queryResultSetByPage( Connection connection, TableConfig tableConfig, Context context,
			IndexedCollection input, int beginPos, int showNum, String totalNumFieldName, String sortParam )
			throws TranFailException
	{
		int totalNum = 0;
		// 构造查询实体
		LoafQueryInterface query = LoafQueryFactory.createQueryEntity( context, tableConfig, (KeyedCollection) input
				.getDataElement() );
		Trace.logDebug( Trace.COMPONENT_MAPPING, "查询SQL--->" + query );
		List inputList = getQueryInputList( tableConfig, context );
		totalNum = query.getCount( connection, inputList );
		try
		{
			PreparedStatement statement = query.getQueryByPageStatement( connection, inputList, beginPos, showNum );
			ResultSet resultSet = statement.executeQuery();
			LoafTools.fillResultIntoIcoll( tableConfig, resultSet, input );
		}
		catch ( SQLException ex )
		{
			throw new TranFailException( LianaConstants.DEFAULT_ERROR_CODE, ex );
		}
		try
		{
			context.setDataValue( totalNumFieldName, String.valueOf( totalNum ) );
		}
		catch ( EMPException ex )
		{
			Trace.logError( Trace.COMPONENT_MAPPING, "输出查询结果出错", ex );
		}
	}

	/**
	 * 获取输入参数列表
	 * 
	 * @param tableConfig
	 * @param context
	 * @return List
	 * @throws TranFailException
	 */
	private static List getQueryInputList( TableConfig tableConfig, Context context ) throws TranFailException
	{
		List inputList = new ArrayList();
		Map dataToTableMap = tableConfig.getDataToTableFieldHashMap();
		for ( Iterator iterator = dataToTableMap.keySet().iterator(); iterator.hasNext(); )
		{
			// 将LOAF中定义了，且在context中也定义的数据，作为查询条件输入
			String dataFieldName = (String) iterator.next();
			if ( context.containsKey( dataFieldName ) )
			{
				String dataFieldValue;
				try
				{
					dataFieldValue = (String) context.getDataValue( dataFieldName );
				}
				catch ( EMPException ex )
				{
					throw new TranFailException( LianaConstants.DEFAULT_ERROR_CODE, ex );
				}
				if ( dataFieldValue != null && dataFieldValue.length() > 0 )
				{
					inputList.add( dataFieldValue );
				}
			}
		}
		return inputList;
	}

	/**
	 * 取得表查询的SQL
	 * 
	 * @param businessCode
	 *            交易代码
	 * @param context
	 *            当前交易context
	 * @return 表操作的SQL
	 * @throws TranFailException
	 */
	public static String getQuerySqlFromContext( String businessCode, Context context ) throws TranFailException
	{
		String pkSql = getPKSQL( businessCode, context );
		TableConfig tableConfig = getTableConfig( businessCode );
		return LoafTools.getQuerySql( tableConfig, pkSql );
	}

	/**
	 * 根据配置，从当前数据集合（KeyedCollection）中获取主键
	 * 
	 * @param config
	 * @param tableKey
	 * @param context
	 * @return String
	 */
	private static String getPKFromContext( TableConfig config, String tableKey, Context context )
	{
		boolean isJoinedKey = config.isJoinedPK( tableKey );
		String keyFieldName = null;
		StringBuffer result = new StringBuffer();
		try
		{
			if ( isJoinedKey )
			{
				List array = config.getJoinedPKList( tableKey );
				int size = array.size();
				for ( int i = 0; i < size; i++ )
				{
					keyFieldName = (String) array.get( i );
					result.append( (String) context.getDataValue( keyFieldName ) );
				}
			}
			else
			{
				keyFieldName = (String) config.getTableFieldToDataHashMap().get( tableKey );
				result.append( (String) context.getDataValue( keyFieldName ) );
			}
		}
		catch ( EMPException ex )
		{
			Trace.logDebug( Trace.COMPONENT_MAPPING, "无法找到数据", ex );
		}
		return result.toString();
	}
}
