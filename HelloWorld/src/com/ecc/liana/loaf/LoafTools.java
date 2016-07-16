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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;

import com.ecc.emp.core.EMPException;
import com.ecc.emp.data.IndexedCollection;
import com.ecc.emp.data.KeyedCollection;
import com.ecc.liana.base.Trace;
import com.ecc.liana.exception.TranFailException;

/**
 * LOAF方法工具类
 * 
 * @version 1.0 2008-01-16
 * @author zhanghao@yuchengtech.com
 */
public class LoafTools {
	/**
	 * 生成查询的SQL
	 */
	private LoafTools() {
		// 禁用构造函数
	}

	/**
	 * 生成查询的SQL
	 * 
	 * @param tableConfig
	 * @param sql
	 *            查询条件
	 * @return String
	 * @throws TranFailException
	 */
	public static String getQuerySql( TableConfig tableConfig, String sql )
	{
		Map tableToDataHashMap = tableConfig.getTableFieldToDataHashMap();
		String tableName = tableConfig.getTableName();
		StringBuffer resultSql = new StringBuffer( "SELECT " );
		for ( Iterator iterator = tableToDataHashMap.keySet().iterator(); iterator.hasNext(); )
		{
			String tableField = (String) iterator.next();
			resultSql.append( tableField ).append( "," );
		}
		LoafTools.removeLastComma( resultSql );
		resultSql.append( " FROM " ).append( tableName ).append( " WHERE " ).append( sql );
		return resultSql.toString();
	}

	/**
	 * 去掉最后一个逗号
	 * 
	 * @param str
	 */
	public static void removeLastComma( StringBuffer str )
	{
		int lastIndex = str.length() - 1;
		if ( str.charAt( lastIndex ) == ',' )
		{
			str.deleteCharAt( lastIndex );
		}
	}

	/**
	 * 将数据填充到context中
	 * 
	 * @param contextData
	 * @param fieldName
	 * @param value
	 */
	public static void fillDataField( KeyedCollection contextData, String fieldName, Object value )
	{
		try
		{
			if ( !contextData.containsKey( fieldName ) )
			{
				contextData.addDataField( fieldName, value );
			}
			else
			{
				contextData.setDataValue( fieldName, value );
			}
		}
		catch ( EMPException ex )
		{
			Trace.logError( Trace.COMPONENT_MAPPING, "数据填充失败", ex );
		}
	}

	/**
	 * 将查询取得的结果集数据，插入到保存结果的IndexedCollection中
	 * 
	 * @param tableConfig
	 * @param resultSet *
	 * @param input
	 * @throws SQLException
	 */
	static void fillResultIntoIcoll( TableConfig tableConfig, ResultSet resultSet, IndexedCollection input )
			throws SQLException
	{
		input.clear();
		KeyedCollection inputData = (KeyedCollection) input.getDataElement();
		Map dataToTableMap = tableConfig.getDataToTableFieldHashMap();
		// 处理查询结果，生成KeyedCollection填入IndexedCollection
		while ( resultSet.next() )
		{
			KeyedCollection singleRecord = new KeyedCollection();
			for ( Iterator iterator = inputData.keySet().iterator(); iterator.hasNext(); )
			{
				String dataFieldName = (String) iterator.next();
				String tableFieldName = (String) dataToTableMap.get( dataFieldName );
				String dataFieldValue = resultSet.getString( tableFieldName );
				try
				{
					singleRecord.addDataField( dataFieldName, dataFieldValue );
				}
				catch ( EMPException ex )
				{
					Trace.logError( Trace.COMPONENT_MAPPING, "生成结果集数据出错", ex );
				}
			}
			input.addDataElement( singleRecord );
		}
	}
}
