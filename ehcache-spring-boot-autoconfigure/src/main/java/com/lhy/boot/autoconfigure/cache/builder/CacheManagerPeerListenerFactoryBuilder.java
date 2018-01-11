package com.lhy.boot.autoconfigure.cache.builder;

import com.lhy.boot.autoconfigure.cache.EhCacheProperties;

import lombok.AllArgsConstructor;
import net.sf.ehcache.config.FactoryConfiguration;
import net.sf.ehcache.distribution.RMICacheManagerPeerListenerFactory;

/**
 * 
 * @ClassName: CacheManagerPeerListenerFactoryBuilder
 * @Description:缓存管理监听工厂建造者
 * @author hyluan
 * @date 2018年1月10日 下午4:35:59
 * @Copyright: Copyright (c) 2017 wisedu
 */
@AllArgsConstructor
public class CacheManagerPeerListenerFactoryBuilder implements Builder<FactoryConfiguration<?>> {

	private static final String CLASS_NAME = RMICacheManagerPeerListenerFactory.class.getName();

	EhCacheProperties ehCacheProperties;

	@Override
	public FactoryConfiguration<?> build() {
		return new FactoryConfiguration<FactoryConfiguration<?>>().className(CLASS_NAME)
				.properties("hostName=" + ehCacheProperties.getCluster().getListener().getHostName() + ",port="
						+ ehCacheProperties.getCluster().getListener().getPort() + ",socketTimeoutMillis="
						+ ehCacheProperties.getCluster().getListener().getSocketTimeoutMillis());
	}

}
