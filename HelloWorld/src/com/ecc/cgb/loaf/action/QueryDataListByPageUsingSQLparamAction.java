////////////////////////////////////////////////////////////////////////////
// 
// Copyright (C) 2009 e-Channels CORPORATION
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
// 
// $Log: QueryDataListByPageUsingSQLAction.java,v $
// Revision 1.7  2010/01/29 02:27:52  liangqiang
// adjust some
//
// Revision 1.6  2010/01/28 09:05:12  sunyawei
// getCount����Ҫʹ��connection�����Ҫ��ǰ��������
//
// Revision 1.5  2010/01/28 07:38:29  liangqiang
// init version
//
// 

package com.ecc.cgb.loaf.action;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import com.ecc.emp.core.Context;
import com.ecc.emp.core.EMPException;
import com.ecc.emp.data.IndexedCollection;
import com.ecc.emp.data.KeyedCollection;
import com.ecc.emp.data.ObjectNotFoundException;
import com.ecc.liana.action.LianaJDBCAction;
import com.ecc.liana.base.LianaConstants;
import com.ecc.liana.base.LianaDBAccess;
import com.ecc.liana.base.Trace;
import com.ecc.liana.exception.TranFailException;
import com.ecc.liana.loaf.DataMappingProvider;
import com.ecc.liana.loaf.LoafTools;
import com.ecc.liana.loaf.TableConfig;
import com.ecc.liana.loaf.jdbc.LoafQueryFactory;
import com.ecc.liana.loaf.jdbc.LoafQueryInterface;

/**
 * 将sql以入参的方式传入，进行分页查询
 * 
 * @version 1.1 2014-03-25
 * @author tangxuyao
 */
public class QueryDataListByPageUsingSQLparamAction extends QueryDataListByPageUsingSQLAction {
	/**
	 * 字段分隔符
	 */
	private static final String FIELD_SEPERATOR = "||";
	/**
	 * 出现在查询结果中的列
	 */
	private String queryFields;
	
	/**
	 * sql入参
	 */
	private String sqlParamField;
	
	/**
	 * 输出参数列表
	 */
	private List outputFields;
	
	/**
	 * 排序参数
	 */
	private String sortParam;
	
	/**
	 * 保存查询结果的Icoll的名称
	 */
	private String iCollName;
	
	/**
	 * 存放起始条数字段名称
	 */
	private String beginPosFieldName;
	
	/**
	 * 存放每页显示条数字段名称
	 */
	private String showNumFieldName;
	
	/**
	 * 存放记录总条数字段名称
	 */
	private String totalNumFieldName;

	private List dbFields;
	
	private String datafieldToTablefield(String datafield) {
		String tablefield = null;
		
		if (dbFields == null) {
			//去掉起始select
			String dbfieldsSt = queryFields.substring(7);
			
			List result = new ArrayList();
			StringTokenizer toke = new StringTokenizer( dbfieldsSt, "," );
			while ( toke.hasMoreElements() )
			{
				String input = ((String) toke.nextElement()).trim();
				result.add( input );
			}
			this.dbFields = result;
		}
		
		tablefield = 
			(String) dbFields.get(outputFields.indexOf(datafield));
		return tablefield;
	}
	
	private String getOrderString(Context context) throws EMPException {
		StringBuffer orderString = new StringBuffer();
		
		Object newOrder = null;
		String newOrderSt = null;
		Object orderDirection = null;
		String orderDirectionSt = null;
		
		try {
			newOrder = context.getDataValue("sortKey");
		} catch (ObjectNotFoundException e) {
		}
		
		if (newOrder == null) {
			return sortParam;
		}
		
		//没有定义businessCode使用queryFields做名字转换
		newOrderSt = datafieldToTablefield(newOrder.toString());
		
		String sortDirection = "ASC";
		try {
			sortDirection = (String) context.getDataValue("sortDirection");
			
			if (sortDirection != null) {
				sortDirection = sortDirection.toUpperCase();
				if (!"DESC".equals(sortDirection))
					sortDirection = "ASC";
			}
		} catch (ObjectNotFoundException e) {
		}

		
		orderString.append("ORDER BY ").append(newOrderSt).append(" ").append(sortDirection);
		if (sortParam != null) {
			orderString.append(", ").append(sortParam.substring(sortParam.toUpperCase().indexOf("BY") + 3));
		}
		
		return orderString.toString();
	}
	
	/**
	 * Method execute.
	 * 
	 * @param context
	 *            Context
	 * @return String
	 * @throws TranFailException
	 * @see com.ecc.emp.flow.Action#execute(Context)
	 */
	public String execute( Context context ) throws EMPException
	{	
		Connection connection = null;
		int totalNum = 0;
		try
		{
			IndexedCollection outputIColl = (IndexedCollection) context.get( iCollName );
			// 预处理
			List input = new LinkedList();
			
			String querySqlParam = (String)context.getDataValue(this.sqlParamField);
			//setQuerySql(querySqlParam);
			String finalSQL = LoafTools.parseQuerySql(context, querySqlParam, input);
			 
			// 构造查询实体
			String sortParam = getOrderString(context);
			LoafQueryInterface query = LoafQueryFactory.getNewQuery( queryFields, sortParam, finalSQL );
			connection = LianaDBAccess.getConnection( getDataSource() );
			totalNum = query.getCount( connection, input );
			Trace.logDebug( Trace.COMPONENT_MAPPING, "查询条件--->" + query );
			Trace.logDebug( Trace.COMPONENT_MAPPING, "输入参数--->" + input );
			if ( totalNum > 0 )
			{
				PreparedStatement ps = null;
				ResultSet resultSet = null;
				try
				{
					int beginPos = Integer.parseInt( (String) context.get( beginPosFieldName ) );
					int showNum = Integer.parseInt( (String) context.get( showNumFieldName ) );					
					ps = query.getQueryByPageStatement( connection, input, beginPos, showNum );
					resultSet = ps.executeQuery();
					fillResultIntoIcoll( resultSet, outputIColl );
				}
				catch ( SQLException ex )
				{
					throw new TranFailException( LianaConstants.DEFAULT_ERROR_CODE, ex );
				}
				finally
				{
					LianaDBAccess.cleanResources( ps, resultSet );
				}
			}
			context.put( totalNumFieldName, String.valueOf( totalNum ) );
			Trace.logDebug( Trace.COMPONENT_MAPPING, "查询结果--->\n" + outputIColl );
		}
		catch (EMPException empe)
		{
			Trace.logError(Trace.COMPONENT_MAPPING, empe.getMessage(), empe);
			throw empe;
		}
		finally
		{
			LianaDBAccess.releaseConnection( getDataSource(), connection );
		}
		return String.valueOf( totalNum );
	}

	/**
	 * 将结果集填入到IndexedCollection中
	 * 
	 * @param resultSet
	 * @param input
	 * @throws SQLException
	 */
	private void fillResultIntoIcoll( ResultSet resultSet, IndexedCollection input ) throws SQLException, EMPException
	{
		int dateSize = outputFields.size();
		while ( resultSet.next() )
		{
			KeyedCollection singleRecord = (KeyedCollection)input.getDataElement().clone();
			for ( int i = 1; i <= dateSize; i++ )
			{
				String dataFieldValue = resultSet.getString( i );
				singleRecord.setDataValue( (String)outputFields.get( i - 1 ), dataFieldValue );
			}
			input.addDataElement( singleRecord );
		}
	}

	/**
	 * Method setICollName.
	 * 
	 * @param collName
	 *            String
	 */
	public void setICollName( String collName )
	{
		iCollName = collName;
	}

	/**
	 * Method setBeginPosFieldName
	 * 
	 * @param beginPosFieldName
	 *            String
	 */
	public void setBeginPosFieldName( String beginPosFieldName )
	{
		this.beginPosFieldName = beginPosFieldName;
	}

	/**
	 * Method setShowNumFieldName
	 * 
	 * @param showNumFieldName
	 *            String
	 */
	public void setShowNumFieldName( String showNumFieldName )
	{
		this.showNumFieldName = showNumFieldName;
	}

	/**
	 * Method setTotalNumFieldName
	 * 
	 * @param totalNumFieldName
	 *            String
	 */
	public void setTotalNumFieldName( String totalNumFieldName )
	{
		this.totalNumFieldName = totalNumFieldName;
	}

	/**
	 * Method setQueryFields.
	 * 
	 * @param queryFields
	 *            String
	 */
	public void setQueryFields( String queryFields )
	{
		this.queryFields = queryFields;
	}

	/**
	 * Method setQuerySql.
	 * 
	 * @param querySql
	 *            String
	 */
	public void setQuerySql( String querySql )
	{
		//测试用转换
		if (LianaConstants.DB_TYPE == LianaConstants.DB_TYPE_DB2) {
			querySql = querySql.replaceAll("TO_NUMBER", "BIGINT");
		}
		this.querySql = querySql;
	}

	/**
	 * Method setSortParam.
	 * 
	 * @param sortParam
	 *            String
	 */
	public void setSortParam( String sortParam )
	{
		this.sortParam = sortParam;
	}

	/**
	 * Method setInputFields.
	 * 
	 * @param inputFields
	 *            String
	 */
	public void setInputFields( String inputFields )
	{
		List result = new ArrayList();
		StringTokenizer toke = new StringTokenizer( inputFields, FIELD_SEPERATOR );
		while ( toke.hasMoreElements() )
		{
			String input = ((String) toke.nextElement()).trim();
			result.add( input );
		}
		this.inputFields = result;
	}

	/**
	 * Method setOutputFields.
	 * 
	 * @param outputFields
	 *            String
	 */
	public void setOutputFields( String outputFields )
	{
		List result = new ArrayList();
		StringTokenizer toke = new StringTokenizer( outputFields, FIELD_SEPERATOR );
		while ( toke.hasMoreElements() )
		{
			String input = ((String) toke.nextElement()).trim();
			result.add( input );
		}
		this.outputFields = result;
	}

	public String getSqlParamField() {
		return sqlParamField;
	}

	public void setSqlParamField(String sqlParamField) {
		this.sqlParamField = sqlParamField;
	}
	
	
}
