package com.lhy.boot.autoconfigure.cache.builder;

/**
 * 
* @ClassName: Builder
* @Description: 建造者接口
* @author  hyluan
* @date 2018年1月10日 下午4:29:51
* @Copyright: Copyright (c) 2017 wisedu
* @param <T>
 */
public interface Builder<T> {
	
	/**
	 * 
	* @Title: build
	* @Description: 构建对象
	* @return T    返回类型
	* @return
	 */
	T build();
}
