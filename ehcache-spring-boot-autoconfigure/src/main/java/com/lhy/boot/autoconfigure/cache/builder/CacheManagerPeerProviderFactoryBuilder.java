package com.lhy.boot.autoconfigure.cache.builder;

import java.util.Set;

import org.springframework.util.StringUtils;

import com.lhy.boot.autoconfigure.cache.EhCacheHelper;
import com.lhy.boot.autoconfigure.cache.EhCacheProperties;

import lombok.extern.slf4j.Slf4j;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.FactoryConfiguration;
import net.sf.ehcache.distribution.RMICacheManagerPeerProviderFactory;

/**
 * 
* @ClassName: CacheManagerPeerProviderFactoryBuilder
* @Description: 缓存管理提供者工厂建造者
* @author  hyluan
* @date 2018年1月10日 下午4:36:56
* @Copyright: Copyright (c) 2017 wisedu
 */
@Slf4j
public class CacheManagerPeerProviderFactoryBuilder implements Builder<FactoryConfiguration<?>>{

	/**
	 * CacheManagerPeerProviderFactory类名
	 */
	private static final String CLASS_NAME = RMICacheManagerPeerProviderFactory.class.getName();
	
	EhCacheProperties ehCacheProperties;
	String peerDiscovery;
	Set<String> cacheNames;
	
	public CacheManagerPeerProviderFactoryBuilder(EhCacheProperties ehCacheProperties) {
		this.ehCacheProperties = ehCacheProperties;
		this.peerDiscovery = ehCacheProperties.getCluster().getProvider().getPeerDiscovery();
	}
	
	public CacheManagerPeerProviderFactoryBuilder(EhCacheProperties ehCacheProperties, Set<String> cacheNames) {
		this(ehCacheProperties);
		this.cacheNames = cacheNames;
	}

	public CacheManagerPeerProviderFactoryBuilder(EhCacheProperties ehCacheProperties,Configuration configuration) {
		this(ehCacheProperties,configuration.getCacheConfigurationsKeySet());
	}
	
	@Override
	public FactoryConfiguration<?> build() {
		if (EhCacheProperties.Cluster.Provider.PEERDISCOVERY_AUTOMATIC.equals(peerDiscovery)) {
			return automaticFactoryConfiguration();
		} else if (EhCacheProperties.Cluster.Provider.PEERDISCOVERY_MANUAL.equals(peerDiscovery)) {
			return manualFactoryConfiguration();
		} else {
			throw new IllegalArgumentException(peerDiscovery);
		}
	}

	private FactoryConfiguration<FactoryConfiguration<?>> automaticFactoryConfiguration() {
		return new FactoryConfiguration<FactoryConfiguration<?>>()
				.className(CLASS_NAME).properties(
						"peerDiscovery=" + EhCacheProperties.Cluster.Provider.PEERDISCOVERY_AUTOMATIC + ",multicastGroupAddress="
								+ ehCacheProperties.getCluster().getProvider().getAutomatic().getMulticastGroupAddress()
								+ ",multicastGroupPort="
								+ ehCacheProperties.getCluster().getProvider().getAutomatic().getMulticastGroupPort()
								+ ",timeToLive="+ehCacheProperties.getCluster().getProvider().getAutomatic().getTimeToLive());
	}

	private FactoryConfiguration<FactoryConfiguration<?>> manualFactoryConfiguration() {
		String rmiUrls = ehCacheProperties.getCluster().getProvider().getManual().getRmiUrls();
		if (StringUtils.hasText(rmiUrls)) {
			String makeRmiUrls = new EhCacheHelper().makeRmiUrls(rmiUrls, cacheNames);
			return new FactoryConfiguration<FactoryConfiguration<?>>()
					.className(CLASS_NAME)
					.properties("peerDiscovery=" + EhCacheProperties.Cluster.Provider.PEERDISCOVERY_MANUAL + ",rmiUrls="
							+ makeRmiUrls);
		}
		throw new NullPointerException("rmiUrls can not be null");
	}
	
}
