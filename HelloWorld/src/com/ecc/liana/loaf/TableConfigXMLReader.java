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

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ecc.liana.base.LianaConstants;
import com.ecc.liana.base.Trace;
import com.ecc.liana.exception.TranFailException;

/**
 * 从XML文件中解析数据库业务表与交易数据定义配置的类
 * 
 * @version 2.0 2007-11-01
 * @author zhanghao@yuchengtech.com
 */
public class TableConfigXMLReader {
	/**
	 * Field XML_TAG_ITEM_ID. (value is ""businessCode"")
	 */
	private static final String XML_TAG_ITEM_ID = "businessCode";
	/**
	 * Field XML_TAG_SUB_BSNCODE. (value is ""subBusinessCode"")
	 */
	private static final String XML_TAG_SUB_BSNCODE = "subBusinessCode";
	/**
	 * Field XML_TAG_SHOW_BSNCODE. (value is ""showBusinessCode"")
	 */
	private static final String XML_TAG_SHOW_BSNCODE = "showBusinessCode";
	/**
	 * Field XML_TAG_TABLE_NAME. (value is ""tableName"")
	 */
	private static final String XML_TAG_TABLE_NAME = "tableName";
	/**
	 * Field XML_TAG_CONFIG_FIELD. (value is ""configField"")
	 */
	private static final String XML_TAG_CONFIG_FIELD = "configField";
	/**
	 * Field XML_TAG_DATA_FIELD. (value is ""dataField"")
	 */
	private static final String XML_TAG_DATA_FIELD = "dataField";
	/**
	 * Field XML_TAG_TABLE_FIELD. (value is ""tableField"")
	 */
	private static final String XML_TAG_TABLE_FIELD = "tableField";
	/**
	 * Field XML_TAG_PRIMARY_KEY. (value is ""isPrimaryKey"")
	 */
	private static final String XML_TAG_PRIMARY_KEY = "isPrimaryKey";
	/**
	 * Field XML_TAG_JOINED_KEY. (value is ""isJoinedKey"")
	 */
	private static final String XML_TAG_JOINED_KEY = "isJoinedKey";
	/**
	 * Field rootNode.
	 */
	private Node rootNode;

	/**
	 * @param fileName
	 */
	public TableConfigXMLReader(String fileName) {
		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating( false );
			Document doc = factory.newDocumentBuilder().parse( new File( fileName ) );
			NodeList list = doc.getChildNodes();
			for ( int i = 0; i < list.getLength(); i++ )
			{
				if ( list.item( i ) instanceof Element )
				{
					rootNode = list.item( i );
					break;
				}
			}
		}
		catch ( Exception ex )
		{
			Trace.logError( Trace.COMPONENT_MAPPING, "读入配置文件失败", ex );
		}
	}

	/**
	 * Method getConfigs.
	 * 
	 * @return HashMap
	 * @throws TranFailException
	 */
	public Map getConfigs() throws TranFailException
	{
		Map configs = new HashMap();
		NodeList configNodeList = rootNode.getChildNodes().item( 1 ).getChildNodes();
		int size = configNodeList.getLength();
		for ( int i = 0; i < size; i++ )
		{
			Node node = configNodeList.item( i );
			if ( node.getNodeType() != Node.ELEMENT_NODE )
				continue;
			Node itemIdNode = node.getAttributes().getNamedItem( XML_TAG_ITEM_ID );
			Node subBusinessNode = node.getAttributes().getNamedItem( XML_TAG_SUB_BSNCODE );
			Node showBusinessNode = node.getAttributes().getNamedItem( XML_TAG_SHOW_BSNCODE );
			Node tableNameNode = node.getAttributes().getNamedItem( XML_TAG_TABLE_NAME );
			if ( itemIdNode != null && tableNameNode != null )
			{
				String businessCode = itemIdNode.getNodeValue().trim();
				String subBusinessCode = subBusinessNode == null ? null : subBusinessNode.getNodeValue().trim();
				String showBusinessCode = showBusinessNode == null ? null : showBusinessNode.getNodeValue().trim();
				String tableName = tableNameNode.getNodeValue();
				TableConfig newConfig = new TableConfig( businessCode, tableName, subBusinessCode, showBusinessCode );
				NodeList fieldNodeList = node.getChildNodes();
				int languageNodeSize = fieldNodeList.getLength();
				for ( int j = 0; j < languageNodeSize; j++ )
				{
					Node child = fieldNodeList.item( j );
					if ( child.getNodeType() != Node.ELEMENT_NODE )
						continue;
					if ( child.getNodeName().equals( XML_TAG_CONFIG_FIELD ) )
					{
						addConfigField( newConfig, child );
					}
				}
				String[] codes = businessCode.split( "," );
				for ( int j = 0; j < codes.length; j++ )
				{
					configs.put( codes[j], newConfig );
				}
			}
		}
		return configs;
	}

	/**
	 * 解析XML节点
	 * 
	 * @param newConfig
	 * @param configFieldNode
	 * @throws TranFailException
	 */
	public void addConfigField( TableConfig newConfig, Node configFieldNode ) throws TranFailException
	{
		Node dataFieldNode = configFieldNode.getAttributes().getNamedItem( XML_TAG_DATA_FIELD );
		Node tableFieldNode = configFieldNode.getAttributes().getNamedItem( XML_TAG_TABLE_FIELD );
		Node isPrimaryKeyNode = configFieldNode.getAttributes().getNamedItem( XML_TAG_PRIMARY_KEY );
		Node isJoinedKeyNode = configFieldNode.getAttributes().getNamedItem( XML_TAG_JOINED_KEY );
		String dataFieldName = dataFieldNode == null ? null : dataFieldNode.getNodeValue();
		String tableFieldName = tableFieldNode == null ? null : tableFieldNode.getNodeValue();
		String isPrimaryKey = isPrimaryKeyNode == null ? null : isPrimaryKeyNode.getNodeValue();
		String isJoinedKey = isJoinedKeyNode == null ? null : isJoinedKeyNode.getNodeValue();
		if ( dataFieldName == null || tableFieldName == null )
		{
			throw new TranFailException( LianaConstants.DEFAULT_ERROR_CODE, "数据字段名或者表字段名为空：dataField = "
					+ dataFieldName + ", tableField = " + tableFieldName );
		}
		newConfig.addDataTableField( dataFieldName, tableFieldName, isPrimaryKey, isJoinedKey );
	}
}
