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

/**
 * LOAF ��ѯ�ӿ���<br>
 * �����������б��ѯ�ͷ�ҳ��ѯ�Ľӿ�
 * 
 * @version 1.0 2008-02-04
 * @author zhanghao@yuchengtech.com
 */
public interface LoafQueryInterface {
	/**
	 * ��ȡ��ѯ��PreparedStatement
	 * 
	 * @param connection
	 * @param input List
	 * @return PreparedStatement
	 * @throws SQLException
	 */
	public PreparedStatement getQueryStatement( Connection connection, List input ) throws SQLException;

	/**
	 * ��ȡ��ҳ��ѯ��PreparedStatement
	 * 
	 * @param connection
	 * @param input List
	 * @param beginPos
	 *            int
	 * @param showNum
	 *            int
	 * @return PreparedStatement
	 * @throws SQLException
	 */
	public PreparedStatement getQueryByPageStatement( Connection connection, List input, int beginPos, int showNum )
			throws SQLException;

	/**
	 * ��ȡ��ҳ��ѯʱ�ܼ�¼����
	 * 
	 * @param connection
	 * @param input List
	 * @return int
	 */
	public int getCount( Connection connection, List input );
}
