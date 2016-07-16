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

import java.util.HashMap;
import java.util.Map;

import com.ecc.emp.core.Context;
import com.ecc.emp.data.KeyedCollection;
import com.ecc.liana.base.LianaConstants;
import com.ecc.liana.base.LianaStandard;
import com.ecc.liana.exception.TranFailException;

/**
 * LOAF LianaQuery构造工厂类<br>
 * 用于构造实现了LoafQueryInterface接口的查询对象，并提供缓存机制
 * 
 * @version 1.0 2008-02-04
 * @author zhanghao@yuchengtech.com
 */
public class LoafQueryFactory {
	/**
	 * 网银异常:未定义的数据库类型
	 */
	public static final TranFailException EXCEPTION_UNDEFINED_DATABASE_TYPE = new TranFailException(
			LianaConstants.DEFAULT_ERROR_CODE,
			"未定义的数据库类型" );
	/**
	 * 是否启用缓存
	 */
	private static boolean QUERY_POOL_IS_ENABLE = true;
	/**
	 * 缓存用的查询池
	 */
	private static Map QUERY_POOL = null;

	/**
	 * 禁用构造函数
	 */
	private LoafQueryFactory() {
		// 禁用构造函数
	}

	/**
	 * @param context
	 * @param tableConfig
	 * @param resultKColl
	 * @return LoafQueryInterface
	 * @throws TranFailException
	 */
	public static LoafQueryInterface createQueryEntity( Context context, TableConfig tableConfig,
			KeyedCollection resultKColl ) throws TranFailException
	{
		return createQueryEntity( context, tableConfig, resultKColl, null );
	}

	/**
	 * @param context
	 * @param tableConfig
	 * @param resultKColl
	 *            KeyedCollection
	 * @param sortParam
	 * @return LoafQueryInterface
	 * @throws TranFailException
	 */
	public static LoafQueryInterface createQueryEntity( Context context, TableConfig tableConfig,
			KeyedCollection resultKColl, String sortParam ) throws TranFailException
	{
		if ( QUERY_POOL_IS_ENABLE )
		{
			// TODO 此处构造key的方式有局限性，不能保证不同的查询构造出的key不同
			String key = tableConfig.getBusinessCode() + context.getName();
			LoafQueryInterface pooledQuery = getPooledQuery( key );
			if ( pooledQuery == null )
			{
				pooledQuery = getNewQuery( context, tableConfig, resultKColl, sortParam );
				QUERY_POOL.put( key, pooledQuery );
			}
			return pooledQuery;
		}
		else
		{
			return getNewQuery( context, tableConfig, resultKColl, sortParam );
		}
	}

	/**
	 * 获取缓存中的查询对象
	 * 
	 * @param key
	 * @return LoafQueryInterface
	 */
	private static LoafQueryInterface getPooledQuery( String key )
	{
		if ( QUERY_POOL == null )
		{
			QUERY_POOL = new HashMap();
			return null;
		}
		else
		{
			LoafQueryInterface pooledQuery = (LoafQueryInterface) QUERY_POOL.get( key );
			return pooledQuery;
		}
	}

	/**
	 * 生成新的查询对象
	 * 
	 * @param context
	 * @param tableConfig
	 * @param resultKColl
	 * @param sortParam
	 * @return LoafQueryInterface
	 * @throws TranFailException
	 */
	private static LoafQueryInterface getNewQuery( Context context, TableConfig tableConfig,
			KeyedCollection resultKColl, String sortParam ) throws TranFailException
	{
		if ( LianaStandard.isUsingOracle() )
		{
			return new LoafQueryOracle( context, tableConfig, resultKColl, sortParam );
		}
		else if ( LianaStandard.isUsingDB2() )
		{
			return new LoafQueryDB2( context, tableConfig, resultKColl, sortParam );
		}
		else
		{
			throw EXCEPTION_UNDEFINED_DATABASE_TYPE;
		}
	}
}