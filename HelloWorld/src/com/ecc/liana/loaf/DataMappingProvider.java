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
 * Liana ����ӳ�乫�÷����ṩ��
 * 
 * @version 2.2 2008-01-16
 * @author zhanghao@yuchengtech.com
 */
public class DataMappingProvider {
	/**
	 * LOAF���ñ�keyΪҵ����룬valueΪTableConfig����
	 * 
	 * @see TableConfig
	 */
	protected static Map TABLE_CONFIG_MAP = null;
	/**
	 * �����쳣:��¼������
	 */
	private static final TranFailException EXCEPTION_DATA_NOT_FOUND = new TranFailException( "EBLN5001", "Ҫ��ѯ�ļ�¼������" );
	/**
	 * �����쳣:������ȫ
	 */
	private static final TranFailException EXCEPTION_INVALID_PK = new TranFailException( "EBLN5002", "��ѯ����������" );

	/**
	 * ���ù��캯��
	 */
	protected DataMappingProvider() {
		// ���ù��캯��
	}

	/**
	 * ���ļ���ʼ��LOAF����
	 * 
	 * @param filePathName
	 * @throws TranFailException
	 */
	public static void initTableConfig( String filePathName ) throws TranFailException
	{
		TABLE_CONFIG_MAP = TableConfig.initFormFile( filePathName );
	}

	/**
	 * ����������
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
				Trace.logError( Trace.COMPONENT_MAPPING, "����Ĳ�������" + input );
				return LoafConstants.OP_TYPE_ADD;
		}
	}

	/**
	 * ���ݽ��״��룬ȡ�ö�Ӧ�ı������SQL
	 * 
	 * @param businessCode
	 *            ���״���
	 * @param context
	 *            ��ǰ����context
	 * @return ����SQL
	 * @throws TranFailException
	 */
	public static String getPKSQL( String businessCode, Context context ) throws TranFailException
	{
		StringBuffer result = new StringBuffer();
		TableConfig tableConfig = getTableConfig( businessCode );
		// �������е�����������SQL
		for ( Iterator iterator = tableConfig.getPrimaryKeyHashMap().keySet().iterator(); iterator.hasNext(); )
		{
			String tableKey = (String) iterator.next();
			String keyFieldValue = getPKFromContext( tableConfig, tableKey, context );
			if ( keyFieldValue != null && keyFieldValue.length() > 0 )
			{
				if ( result.length() > 0 )
				{
					// ����ж����������AND�ָ�
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
	 * ȡ�ñ������SQL
	 * 
	 * @param opType
	 *            �������ͣ�1�����룻2��ɾ����3���޸�
	 * @param businessCode
	 *            ���״���
	 * @param context
	 * @return �������SQL�����û�б�������ؿ��ַ���
	 * @throws TranFailException
	 * @throws EMPException
	 */
	public static String getOperationSql( int opType, String businessCode, Context context ) throws TranFailException
	{
		TableConfig tableConfig = getTableConfig( businessCode );
		String tableName = tableConfig.getTableName();
		Map tableToDataHashMap = tableConfig.getTableFieldToDataHashMap();
		StringBuffer resultSql = new StringBuffer();// �������SQL
		if ( LoafConstants.OP_TYPE_ADD == opType )// ����ָ��
		{
			resultSql.append( "INSERT INTO " ).append( tableName ).append( '(' );
			StringBuffer insertTableFields = new StringBuffer();// �����ֶ�����
			StringBuffer insertValues = new StringBuffer();// ����ֵ����
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
			String pkSql = getPKSQL( businessCode, context );// ����sql���
			if ( LoafConstants.OP_TYPE_DELETE == opType )
			{
				// ɾ��ָ��
				resultSql.append( "DELETE FROM " ).append( tableName ).append( " WHERE " ).append( pkSql );
			}
			else if ( LoafConstants.OP_TYPE_UPDATE == opType )
			{
				// �޸�ָ��
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
	 * ���ݽ��״���ȡ�ñ�ӳ�䶨�壬ʹ��context���������ɲ�ѯSQL����ѯ���ݿ�󽫲�ѯ�������context��
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
		// TODO ��Ҫ�Ż�
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		TableConfig tableConfig = getTableConfig( businessCode );
		Map tableFieldToDataHashMap = tableConfig.getTableFieldToDataHashMap();
		String queryOrgSql = getQuerySqlFromContext( businessCode, context );
		try
		{
			// ȡ��ԭʼ����
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
				Trace.logError( Trace.COMPONENT_MAPPING, "��ѯʧ��", ex );
			}
		}
	}

	/**
	 * ȡ�ý��׶���
	 * 
	 * @param businessCode
	 *            ���״���
	 * @return OperationTableConf
	 * @throws TranFailException
	 */
	public static TableConfig getTableConfig( String businessCode ) throws TranFailException
	{
		TableConfig tableConfig = (TableConfig) TABLE_CONFIG_MAP.get( businessCode );
		if ( tableConfig == null )
		{
			throw new TranFailException( LianaConstants.DEFAULT_ERROR_CODE, "δ����Ľ��״���" + businessCode );
		}
		return tableConfig;
	}

	/**
	 * ����context�е��������ݣ���Ҫ�����IndexedCollection�����ݽṹ�����ɲ�ѯ��䲢��ѯ<br>
	 * ��ѯ�����䵽IndexedCollection��
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
		// �����ѯʵ��
		LoafQueryInterface query = LoafQueryFactory.createQueryEntity( context, tableConfig, (KeyedCollection) input
				.getDataElement() );
		List inputList = getQueryInputList( tableConfig, context );
		Trace.logDebug( Trace.COMPONENT_MAPPING, "��ѯSQL--->" + query );
		try
		{
			// ��ȡPreparedStatement
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
	 * ����context�е��������ݣ���Ҫ�����IndexedCollection�����ݽṹ�����ɲ�ѯ��䲢��ҳ��ѯ<br>
	 * ��ѯ�����䵽IndexedCollection��
	 * 
	 * @param connection
	 * @param tableConfig
	 * @param context
	 * @param input
	 * @param beginPos
	 *            ��ѯ��ʼλ��
	 * @param showNum
	 *            ÿҳ��ʾ����
	 * @param totalNumFieldName
	 *            ������������Field����
	 * @param sortParam
	 *            ��������
	 * @throws TranFailException
	 */
	public static void queryResultSetByPage( Connection connection, TableConfig tableConfig, Context context,
			IndexedCollection input, int beginPos, int showNum, String totalNumFieldName, String sortParam )
			throws TranFailException
	{
		int totalNum = 0;
		// �����ѯʵ��
		LoafQueryInterface query = LoafQueryFactory.createQueryEntity( context, tableConfig, (KeyedCollection) input
				.getDataElement() );
		Trace.logDebug( Trace.COMPONENT_MAPPING, "��ѯSQL--->" + query );
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
			Trace.logError( Trace.COMPONENT_MAPPING, "�����ѯ�������", ex );
		}
	}

	/**
	 * ��ȡ��������б�
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
			// ��LOAF�ж����ˣ�����context��Ҳ��������ݣ���Ϊ��ѯ��������
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
	 * ȡ�ñ��ѯ��SQL
	 * 
	 * @param businessCode
	 *            ���״���
	 * @param context
	 *            ��ǰ����context
	 * @return �������SQL
	 * @throws TranFailException
	 */
	public static String getQuerySqlFromContext( String businessCode, Context context ) throws TranFailException
	{
		String pkSql = getPKSQL( businessCode, context );
		TableConfig tableConfig = getTableConfig( businessCode );
		return LoafTools.getQuerySql( tableConfig, pkSql );
	}

	/**
	 * �������ã��ӵ�ǰ���ݼ��ϣ�KeyedCollection���л�ȡ����
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
			Trace.logDebug( Trace.COMPONENT_MAPPING, "�޷��ҵ�����", ex );
		}
		return result.toString();
	}
}
