package com.lhy.boot.autoconfigure.cache.builder;

import com.lhy.boot.autoconfigure.cache.EhCacheProperties.Cache;

import lombok.AllArgsConstructor;
import net.sf.ehcache.config.CacheConfiguration.CacheEventListenerFactoryConfiguration;
import net.sf.ehcache.distribution.RMICacheReplicatorFactory;

/**
 * 
* @ClassName: CacheEventListenerFactoryBuilder
* @Description:缓存事件监听工厂建造者
* @author  hyluan
* @date 2018年1月10日 下午4:35:40
* @Copyright: Copyright (c) 2017 wisedu
 */
@AllArgsConstructor
public class CacheEventListenerFactoryBuilder implements Builder<CacheEventListenerFactoryConfiguration>{
	
	private static final String CLASS_NAME = RMICacheReplicatorFactory.class.getName();

	Cache cache;
	
	@Override
	public CacheEventListenerFactoryConfiguration build() {
		CacheEventListenerFactoryConfiguration cacheEventListenerFactoryConfiguration = new CacheEventListenerFactoryConfiguration()
				.className(CLASS_NAME);
		String properties = cache.getRmiCacheReplicatorFactory().getProperties();
		cacheEventListenerFactoryConfiguration.setProperties(properties);
		return cacheEventListenerFactoryConfiguration;
	}

}
