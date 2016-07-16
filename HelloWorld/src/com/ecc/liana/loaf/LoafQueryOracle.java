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
import java.sql.SQLException;
import java.util.List;

import com.ecc.emp.core.Context;
import com.ecc.emp.data.KeyedCollection;
import com.ecc.liana.exception.TranFailException;

/**
 * LOAF 查询Oracle实现类<br>
 * 实现了Oracle的分页查询接口getQueryByPageStatement
 * 
 * @version 1.0 2008-02-04
 * @author zhanghao@yuchengtech.com
 */
public class LoafQueryOracle extends LoafQueryCommon {
	/**
	 * Constructor for LoafQueryOracle.
	 * 
	 * @param context
	 *            Context
	 * @param tableConfig
	 *            TableConfig
	 * @param resultKColl
	 *            KeyedCollection
	 * @param sortParam
	 *            String
	 * @throws TranFailException
	 */
	public LoafQueryOracle(Context context, TableConfig tableConfig, KeyedCollection resultKColl, String sortParam)
			throws TranFailException {
		super( context, tableConfig, resultKColl, sortParam );
	}

	/**
	 * Method getQueryByPageStatement.
	 * 
	 * @param connection
	 *            Connection
	 * @param input List
	 * @param beginPos
	 *            int
	 * @param showNum
	 *            int
	 * @return PreparedStatement
	 * @throws SQLException
	 * @see com.ecc.liana.loaf.LoafQueryInterface#getQueryByPageStatement(Connection, int, int)
	 */
	public PreparedStatement getQueryByPageStatement( Connection connection, List input, int beginPos, int showNum )
			throws SQLException
	{
		StringBuffer resultSql = new StringBuffer( itsQueryFields );
		resultSql.append( " FROM ( SELECT TEMP_TABLE.*, ROWNUM ROWNUM_ FROM ( " );
		resultSql.append( itsQueryFields ).append( " FROM " ).append( itsQueryTable );
		resultSql.append( " WHERE " ).append( itsQueryCondition );
		if ( itsQueryOrder != null )
		{
			resultSql.append( ' ' ).append( itsQueryOrder );
		}
		resultSql.append( ")  TEMP_TABLE WHERE ROWNUM < ? ) WHERE ROWNUM_ >= ?" );
		PreparedStatement statement = connection.prepareStatement( resultSql.toString() );
		// 填入输入数据
		int inputSize = input.size();
		int i = 1;
		for ( ; i <= inputSize; i++ )
		{
			statement.setString( i, (String) input.get( i - 1 ) );
		}
		statement.setInt( i++, beginPos + showNum );
		statement.setInt( i++, beginPos );
		return statement;
	}
}
