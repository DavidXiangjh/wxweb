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
 * LOAF����������
 * 
 * @version 1.0 2008-01-16
 * @author zhanghao@yuchengtech.com
 */
public class LoafTools {
	/**
	 * ���ɲ�ѯ��SQL
	 */
	private LoafTools() {
		// ���ù��캯��
	}

	/**
	 * ���ɲ�ѯ��SQL
	 * 
	 * @param tableConfig
	 * @param sql
	 *            ��ѯ����
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
	 * ȥ�����һ������
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
	 * ��������䵽context��
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
			Trace.logError( Trace.COMPONENT_MAPPING, "�������ʧ��", ex );
		}
	}

	/**
	 * ����ѯȡ�õĽ�������ݣ����뵽��������IndexedCollection��
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
		// �����ѯ���������KeyedCollection����IndexedCollection
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
					Trace.logError( Trace.COMPONENT_MAPPING, "���ɽ�������ݳ���", ex );
				}
			}
			input.addDataElement( singleRecord );
		}
	}
}
