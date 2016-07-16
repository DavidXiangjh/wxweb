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
// $Log: QueryDataListByPageUsingSpecialSQLAction.java,v $
// Revision 1.3  2010/01/28 07:38:22  liangqiang
// init version
//
// 

package com.ecc.cgb.loaf.action;


/**
 * <b>功能描述：</b><br>
 * 根据输入的定制SQL进行分页查询，将结果存入到iColl中<br>
 * 定制SQL中通过 '?变量名' 的方式定义输入参数<br>
 * 例： SELECT WFL_CSTNO FROM CB_WAGE_FLOW WHERE WFL_CSTNO=?custormId <br>
 * 
 * @see QueryDataListByPageUsingSQLAction
 * @version 1.0 2008-07-11
 * @author zhanghao@yuchengtech.com
 */
public class QueryDataListByPageUsingSpecialSQLAction extends QueryDataListByPageUsingSQLAction {
	/**
	 * Method setQuerySql.
	 * 
	 * @param querySql
	 *            String
	 */
	public void setQuerySql( String querySql )
	{
		super.querySql = querySql;
	}
}
