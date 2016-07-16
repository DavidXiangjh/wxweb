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
import java.util.List;

import com.ecc.emp.core.Context;
import com.ecc.emp.core.EMPException;
import com.ecc.emp.transaction.EMPTransactionDef;
import com.ecc.liana.base.LianaAction;
import com.ecc.liana.base.LianaDBAccess;
import com.ecc.liana.base.Trace;
import com.ecc.liana.loaf.DataMappingProvider;
import com.ecc.liana.loaf.TableConfig;

/**
 * <b>����������</b><br>
 * ��context�е����ݱ����ʱ��ʵ�����ݿ�Ĳ���,����������/�޸�/ɾ��<br>
 * <b>����˵��:</b><br>
 * opType:1.���� 2.ɾ�� 3.�޸�<br>
 * businessCodeField:���״����Field����<br>
 * businessCode:���û��ָ��businessCodeField��ֵ����ʹ��businessCode�������ֵ
 * 
 * @version 2.1 2008-01-16
 * @author zhanghao@yuchengtech.com
 */
public class UpdateChangedDataAction extends LianaAction {
	/**
	 * ָ������:1.���� 2.�޸� 3.ɾ��
	 */
	private int opType;
	/**
	 * ���潻�״����Field������
	 */
	private String businessCodeField;
	/**
	 * ���״���
	 */
	private String businessCode;
	/**
	 * ȫ������/��������
	 */
	private int trxType = 0;
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
			// ��������в���
			String operationSql = DataMappingProvider.getOperationSql( opType, currentBusinessCode, context );
			connection = LianaDBAccess.getConnection();
			if ( operationSql.length() > 0 )
			{
				Trace.logDebug( Trace.COMPONENT_MAPPING, "��ʼ��������---->" + operationSql );
				LianaDBAccess.executeSQLUpdate( connection, operationSql );
			}
			// �Թ����ӱ���в���
			List subBusinessList = tableConfig.getSubBusinessList();
			for ( int i = 0; i < subBusinessList.size(); i++ )
			{
				operationSql = DataMappingProvider.getOperationSql( opType, (String) subBusinessList.get( i ), context );
				if ( operationSql.length() > 0 )
				{
					Trace.logDebug( Trace.COMPONENT_MAPPING, "��ʼ��������---->" + operationSql );
					LianaDBAccess.executeSQLUpdate( connection, operationSql );
				}
			}
		}
		finally
		{
			LianaDBAccess.releaseConnection( connection );
		}
		return DEFAULT_RETURN_VALUE;
	}

	/**
	 * Method setOpType.
	 * 
	 * @param opType
	 *            String
	 */
	public void setOpType( String opType )
	{
		this.opType = DataMappingProvider.getOpType( opType );
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
	public void setTransactionType( String value )
	{
		if ( "TRX_REQUIRED".equals( value ) )
			trxType = 0;
		else if ( "TRX_REQUIRE_NEW".equals( value ) )
			trxType = 1;
	}

	public EMPTransactionDef getTransactionDef()
	{
		return new EMPTransactionDef( trxType );
	}
}
