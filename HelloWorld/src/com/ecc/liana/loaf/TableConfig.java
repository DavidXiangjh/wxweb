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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import com.ecc.liana.base.LianaConstants;
import com.ecc.liana.exception.TranFailException;

/**
 * ���ݿ�ҵ����뽻�����ݶ�������<br>
 * ���������ݿ������ҵ��ı����ơ�����������Ӧ�Ľ������ƣ��Լ����ֶ��뽻���������ӳ��
 * 
 * @version 1.01 2007-11-01
 * @author xiaobin@yuchengtech.com, zhanghao@yuchengtech.com
 */
public class TableConfig {
	/**
	 * ������ҵ��ʱ�ķָ��
	 */
	private static final String BUSINESS_SEPERATOR = ",";
	/**
	 * ���������ֶεķָ��
	 */
	private static final String JOIN_SEPERATOR = "||";
	/**
	 * ���״���
	 */
	private String businessCode;
	/**
	 * ���ݿ�ҵ�������
	 */
	private String tableName;
	/**
	 * �����ֶ�Ϊkey�����ݿ���ֶ�Ϊvalue�ı�
	 */
	private Map dataToTableMap = new HashMap();
	/**
	 * ���ݿ���ֶ�Ϊkey�������ֶ�Ϊvalue�ı�
	 */
	private Map tableToDataMap = new HashMap();
	/**
	 * ���ݿ��������
	 */
	private Map primaryKeyHashMap = new HashMap();
	/**
	 * ��ҵ���������
	 */
	private List subBusinessList = new ArrayList();
	/**
	 * ������ʾҵ���������
	 */
	private List showBusinessList = new ArrayList();

	/**
	 * ���캯��
	 * 
	 * @param businessCode String
	 * @param tableName
	 * @param subBusinessCode String
	 * @param showBusinessCode String
	 */
	public TableConfig(String businessCode, String tableName, String subBusinessCode, String showBusinessCode) {
		this.businessCode = businessCode;
		this.tableName = tableName;
		setSubBusinessCode( subBusinessCode );
		setShowBusinessCode( showBusinessCode );
	}

	/**
	 * ��xml�ļ���ʼ������
	 * 
	 * @param filePathName String
	 * @return HashMap
	 * @throws TranFailException
	 */
	public static Map initFormFile( String filePathName ) throws TranFailException
	{
		Map opTableMap = new HashMap();
		TableConfigXMLReader reader = new TableConfigXMLReader( filePathName );
		opTableMap = reader.getConfigs();
		return opTableMap;
	}

	/**
	 * ��������һ�����ݶ���
	 * 
	 * @param dataField
	 * @param tableField
	 * @param isPrimaryKey
	 * @param isJoinedKey
	 * @throws TranFailException
	 */
	public void addDataTableField( String dataField, String tableField, String isPrimaryKey, String isJoinedKey )
			throws TranFailException
	{
		if ( dataToTableMap.containsKey( dataField ) || tableToDataMap.containsKey( tableField ) )
		{
			throw new TranFailException( LianaConstants.DEFAULT_ERROR_CODE, "�����ֶ������߱��ֶ����ظ����壺dataField = " + dataField
					+ ", tableField = " + tableField );
		}
		dataToTableMap.put( dataField, tableField );
		tableToDataMap.put( tableField, dataField );
		if ( "true".equals( isPrimaryKey ) )
		{
			if ( "true".equals( isJoinedKey ) )
			{
				primaryKeyHashMap.put( tableField, new Boolean( true ) );
			}
			else
			{
				primaryKeyHashMap.put( tableField, new Boolean( false ) );
			}
		}
	}

	/**
	 * Method getSubBusinessList.
	 * @return ArrayList
	 */
	public List getSubBusinessList()
	{
		return subBusinessList;
	}

	/**
	 * Method getShowBusinessList.
	 * @return ArrayList
	 */
	public List getShowBusinessList()
	{
		return showBusinessList;
	}

	/**
	 * Method setSubBusinessCode.
	 * @param subBsnCode String
	 */
	private final void setSubBusinessCode( String subBsnCode )
	{
		subBusinessList.clear();
		if ( subBsnCode != null )
		{
			StringTokenizer toker = new StringTokenizer( subBsnCode, BUSINESS_SEPERATOR );
			while ( toker.hasMoreElements() )
			{
				subBusinessList.add( toker.nextElement() );
			}
		}
	}

	/**
	 * Method setShowBusinessCode.
	 * @param showBsnCode String
	 */
	private final void setShowBusinessCode( String showBsnCode )
	{
		showBusinessList.clear();
		if ( showBsnCode != null )
		{
			StringTokenizer token = new StringTokenizer( showBsnCode, BUSINESS_SEPERATOR );
			while ( token.hasMoreElements() )
			{
				showBusinessList.add( token.nextElement() );
			}
		}
	}

	/**
	 * Method getTableName.
	 * @return String
	 */
	public String getTableName()
	{
		return tableName;
	}

	/**
	 * Method setTableName.
	 * @param tableName String
	 */
	public void setTableName( String tableName )
	{
		this.tableName = tableName;
	}

	/**
	 * Method getTableFieldToDataHashMap.
	 * @return HashMap
	 */
	public Map getTableFieldToDataHashMap()
	{
		return tableToDataMap;
	}

	/**
	 * Method getDataToTableFieldHashMap.
	 * @return HashMap
	 */
	public Map getDataToTableFieldHashMap()
	{
		return dataToTableMap;
	}

	/**
	 * Method getPrimaryKeyHashMap.
	 * @return HashMap
	 */
	public Map getPrimaryKeyHashMap()
	{
		return primaryKeyHashMap;
	}

	/**
	 * ȡ�ý��״���
	 * 
	 * @return String
	 */
	public String getBusinessCode()
	{
		int seperatorPos = businessCode.indexOf( BUSINESS_SEPERATOR );
		if ( seperatorPos > 0 )
		{
			return businessCode.substring( 0, seperatorPos );
		}
		return businessCode;
	}

	/**
	 * �ж�ָ���ı��ֶ��Ƿ�Ϊ���ӵ�����
	 * 
	 * @param tableFieldName
	 * @return boolean
	 */
	public boolean isJoinedPK( String tableFieldName )
	{
		if ( primaryKeyHashMap.containsKey( tableFieldName ) )
		{
			Boolean result = (Boolean) getPrimaryKeyHashMap().get( tableFieldName );
			return result.booleanValue();
		}
		return false;
	}

	/**
	 * ��ȡ���ӵ�������ÿһ��context�����������ɵ�ArrayList
	 * 
	 * @param tableFieldName
	 * @return ArrayList
	 */
	public List getJoinedPKList( String tableFieldName )
	{
		List result = new ArrayList();
		String joinedField = (String) tableToDataMap.get( tableFieldName );
		StringTokenizer token = new StringTokenizer( joinedField, JOIN_SEPERATOR );
		while ( token.hasMoreElements() )
		{
			result.add( token.nextElement() );
		}
		return result;
	}

	/**
	 * ��ȡ���ӵ�������ÿһ��context�����������ɵ�HashSet
	 * 
	 * @return HashSet
	 */
	public Set getJoinedPKHashSet()
	{
		Set result = new HashSet();
		for ( Iterator iterator = primaryKeyHashMap.keySet().iterator(); iterator.hasNext(); )
		{
			String pk = (String) iterator.next();
			if ( isJoinedPK( pk ) )
			{
				result.addAll( getJoinedPKList( pk ) );
			}
		}
		return result;
	}
}
