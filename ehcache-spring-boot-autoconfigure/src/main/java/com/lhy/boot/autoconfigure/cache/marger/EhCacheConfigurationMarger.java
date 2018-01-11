package com.lhy.boot.autoconfigure.cache.marger;

import net.sf.ehcache.config.Configuration;

/**
 * 
* @ClassName: EhCacheConfigurationMarger
* @Description:* Configuration合并器 
* @author  hyluan
* @date 2018年1月11日 下午3:55:54
* @Copyright: Copyright (c) 2017 wisedu
 */
public interface EhCacheConfigurationMarger {
	
	/**
	 * 
	* @Title: marge
	* @Description: 合并配置
	* @return void    返回类型
	* @param sourceConfiguration
	* @param targetConfiguration
	 */
	void marge(Configuration sourceConfiguration,Configuration targetConfiguration);
}
