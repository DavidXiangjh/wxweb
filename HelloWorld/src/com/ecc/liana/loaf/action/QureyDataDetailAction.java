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
package com.ecc.liana.loaf.action;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.ecc.emp.core.Context;
import com.ecc.emp.core.EMPException;
import com.ecc.liana.base.LianaAction;
import com.ecc.liana.base.LianaConstants;
import com.ecc.liana.base.LianaDBAccess;
import com.ecc.liana.base.Trace;
import com.ecc.liana.exception.TranFailException;
import com.ecc.liana.loaf.DataMappingProvider;
import com.ecc.liana.loaf.TableConfig;

/**
 * <b>功能描述：</b><br>
 * 根据交易代码查询单笔数据明细<br>
 * <b>参数说明:</b><br>
 * businessCodeField:交易代码的Field名称<br>
 * businessCode:如果没有指定businessCodeField的值，则使用businessCode内输入的值
 * 
 * @version 2.1 2008-01-16
 * @author zhanghao@yuchengtech.com
 */
public class QureyDataDetailAction extends LianaAction {
	/**
	 * 保存交易代码的Field的名称
	 */
	private String businessCodeField;
	/**
	 * 交易代码
	 */
	private String businessCode;
	/**
	 * Method execute.
	 * 
	 * @param context
	 *            Context
	 * @return String
	 * @throws EMPException
	 * @see com.ecc.emp.flow.Action#execute(Context)
	 */
	public String execute( Context context ) throws EMPException
	{
		Connection connection = null;
		try
		{
			String currentBusinessCode = null;
			if ( businessCodeField != null )
			{
				currentBusinessCode = (String) context.getDataValue( businessCodeField );
			}
			else
			{
				currentBusinessCode = businessCode;
			}
			connection = LianaDBAccess.getConnection();
			Trace.logDebug( Trace.COMPONENT_MAPPING, currentBusinessCode + "查询开始" );
			// 查询主业务记录信息;
			DataMappingProvider.fillTableDataIntoContext( connection, currentBusinessCode, context );
			// 查询关联子业务记录信息
			TableConfig tableConfig = DataMappingProvider.getTableConfig( currentBusinessCode );
			List subBusinessList = tableConfig.getSubBusinessList();
			for ( int i = 0; i < subBusinessList.size(); i++ )
			{
				String subBusinessCode = (String) subBusinessList.get( i );
				Trace.logDebug( Trace.COMPONENT_MAPPING, currentBusinessCode + "的关联子业务" + subBusinessCode + "查询开始" );
				DataMappingProvider.fillTableDataIntoContext( connection, subBusinessCode, context );
			}
			// 取得交易代码关联的显示用辅助数据，并填入context中
			List showBusinessList = tableConfig.getShowBusinessList();
			for ( int i = 0; i < showBusinessList.size(); i++ )
			{
				String subBusinessCode = (String) showBusinessList.get( i );
				Trace.logDebug( Trace.COMPONENT_MAPPING, currentBusinessCode + "的显示用辅助数据" + subBusinessCode + "查询开始" );
				DataMappingProvider.fillTableDataIntoContext( connection, subBusinessCode, context );
			}
			Trace.logDebug( Trace.COMPONENT_MAPPING, currentBusinessCode + "查询结束" );
		}
		catch ( SQLException ex )
		{
			throw new TranFailException( LianaConstants.DEFAULT_ERROR_CODE, ex );
		}
		finally
		{
			LianaDBAccess.releaseConnection( connection );
		}
		return DEFAULT_RETURN_VALUE;
	}

	/**
	 * Method setBusinessCodeField.
	 * 
	 * @param businessCodeField
	 *            String
	 */
	public void setBusinessCodeField( String businessCodeField )
	{
		this.businessCodeField = businessCodeField;
	}
	
	public void setBusinessCode( String businessCode )
	{
		this.businessCode = businessCode;
	}
}
