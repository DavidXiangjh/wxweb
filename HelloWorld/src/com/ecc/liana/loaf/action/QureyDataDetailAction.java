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
 * <b>����������</b><br>
 * ���ݽ��״����ѯ����������ϸ<br>
 * <b>����˵��:</b><br>
 * businessCodeField:���״����Field����<br>
 * businessCode:���û��ָ��businessCodeField��ֵ����ʹ��businessCode�������ֵ
 * 
 * @version 2.1 2008-01-16
 * @author zhanghao@yuchengtech.com
 */
public class QureyDataDetailAction extends LianaAction {
	/**
	 * ���潻�״����Field������
	 */
	private String businessCodeField;
	/**
	 * ���״���
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
			Trace.logDebug( Trace.COMPONENT_MAPPING, currentBusinessCode + "��ѯ��ʼ" );
			// ��ѯ��ҵ���¼��Ϣ;
			DataMappingProvider.fillTableDataIntoContext( connection, currentBusinessCode, context );
			// ��ѯ������ҵ���¼��Ϣ
			TableConfig tableConfig = DataMappingProvider.getTableConfig( currentBusinessCode );
			List subBusinessList = tableConfig.getSubBusinessList();
			for ( int i = 0; i < subBusinessList.size(); i++ )
			{
				String subBusinessCode = (String) subBusinessList.get( i );
				Trace.logDebug( Trace.COMPONENT_MAPPING, currentBusinessCode + "�Ĺ�����ҵ��" + subBusinessCode + "��ѯ��ʼ" );
				DataMappingProvider.fillTableDataIntoContext( connection, subBusinessCode, context );
			}
			// ȡ�ý��״����������ʾ�ø������ݣ�������context��
			List showBusinessList = tableConfig.getShowBusinessList();
			for ( int i = 0; i < showBusinessList.size(); i++ )
			{
				String subBusinessCode = (String) showBusinessList.get( i );
				Trace.logDebug( Trace.COMPONENT_MAPPING, currentBusinessCode + "����ʾ�ø�������" + subBusinessCode + "��ѯ��ʼ" );
				DataMappingProvider.fillTableDataIntoContext( connection, subBusinessCode, context );
			}
			Trace.logDebug( Trace.COMPONENT_MAPPING, currentBusinessCode + "��ѯ����" );
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
