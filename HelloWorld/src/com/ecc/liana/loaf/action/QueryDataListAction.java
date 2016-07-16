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

import com.ecc.emp.core.Context;
import com.ecc.emp.core.EMPException;
import com.ecc.emp.data.IndexedCollection;
import com.ecc.liana.base.LianaAction;
import com.ecc.liana.base.LianaDBAccess;
import com.ecc.liana.base.Trace;
import com.ecc.liana.loaf.DataMappingProvider;
import com.ecc.liana.loaf.TableConfig;

/**
 * <b>功能描述：</b><br>
 * 根据交易代码查询多笔数据，将结果存入到iColl中<br>
 * <b>参数说明:</b><br>
 * businessCodeField:交易代码的Field名称<br>
 * businessCode:如果没有指定businessCodeField的值，则使用businessCode内输入的值<br>
 * iCollName:作为输入参考，并保存结果的IndexedCollection名称
 * 
 * @version 1.1 2008-01-16
 * @author zhanghao@yuchengtech.com
 */
public class QueryDataListAction extends LianaAction {
	/**
	 * 保存交易代码的Field的名称
	 */
	private String businessCodeField;
	/**
	 * 交易代码
	 */
	private String businessCode;
	/**
	 * 保存查询结果的Icoll的名称
	 */
	private String iCollName;

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
			TableConfig tableConfig = DataMappingProvider.getTableConfig( currentBusinessCode );
			connection = LianaDBAccess.getConnection();
			IndexedCollection input = (IndexedCollection) context.getDataElement( iCollName );
			DataMappingProvider.queryResultSet( connection, tableConfig, context, input );
			Trace.logDebug( Trace.COMPONENT_MAPPING, "查询结果--->" + input );
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

	public void setBusinessCode( String businessCode )
	{
		this.businessCode = businessCode;
	}
}
