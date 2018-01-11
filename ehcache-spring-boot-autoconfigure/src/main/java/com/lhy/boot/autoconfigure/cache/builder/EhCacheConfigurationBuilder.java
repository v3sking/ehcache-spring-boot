package com.lhy.boot.autoconfigure.cache.builder;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.cache.ehcache.EhCacheManagerUtils;
import org.springframework.core.io.Resource;

import com.lhy.boot.autoconfigure.cache.EhCacheProperties;
import com.lhy.boot.autoconfigure.cache.EhCacheProperties.Cache;
import com.lhy.boot.autoconfigure.cache.marger.EhCacheConfigurationMarger;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.CacheConfiguration.CacheEventListenerFactoryConfiguration;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.ConfigurationFactory;
import net.sf.ehcache.config.FactoryConfiguration;

/**
 * 
* @ClassName: EhCacheConfigurationBuilder
* @Description: EhCache Configuration构造类
* @author  luanhy
* @param <T>
* @date 2018年1月8日 下午9:51:20
* @Copyright: Copyright (c) 2017 wisedu
 */
@Slf4j
@AllArgsConstructor
public class EhCacheConfigurationBuilder implements Builder<Configuration>{

	private final EhCacheProperties ehCacheProperties;
	private Resource location; 
	private EhCacheConfigurationMarger ehCacheConfigurationMarger;

	@Override
	public Configuration build() {
		
		Configuration defaultConfiguration = new Configuration();
		
		buildCaches(defaultConfiguration);
		
		if (ehCacheProperties.getClusterEnabled()) {
			buildCacheManagerPeerProviderFactory(defaultConfiguration);
			
			buildCacheManagerPeerListenerFactory(defaultConfiguration);
		}
		
		Configuration finalConfiguration = getFinalConfiguration(defaultConfiguration);
		
		logConfiguration(finalConfiguration);
		
		return finalConfiguration;
	}

	private Configuration parseConfiguration(){
		Configuration configuration = (location != null ? EhCacheManagerUtils.parseConfiguration(location)
				: ConfigurationFactory.parseConfiguration());
		return configuration;
	}
	
	private void buildCaches(Configuration configuration) {
		List<Cache> caches = ehCacheProperties.getCaches();
		for (Cache cache : caches) {
			CacheConfiguration cacheConfiguration = new CacheConfigurationBuilder(ehCacheProperties,cache.getName()).build();
			configuration.addCache(cacheConfiguration);
		}
	}
	
	private void buildCacheManagerPeerProviderFactory(Configuration configuration) {
		CacheManagerPeerProviderFactoryBuilder cacheManagerPeerProviderFactoryBuilder = new CacheManagerPeerProviderFactoryBuilder(ehCacheProperties, configuration);
		configuration.addCacheManagerPeerProviderFactory(cacheManagerPeerProviderFactoryBuilder.build());
	}

	private void buildCacheManagerPeerListenerFactory(Configuration configuration) {
		CacheManagerPeerListenerFactoryBuilder cacheManagerPeerListenerFactoryBuilder = new CacheManagerPeerListenerFactoryBuilder(ehCacheProperties);
		configuration.addCacheManagerPeerListenerFactory(cacheManagerPeerListenerFactoryBuilder.build());
	}
	
	private Configuration getFinalConfiguration(Configuration defaultConfiguration) {
		if (ehCacheConfigurationMarger!= null) {
			Configuration parseConfiguration = parseConfiguration();
			ehCacheConfigurationMarger.marge(defaultConfiguration, parseConfiguration);
			return parseConfiguration;
		}
		return defaultConfiguration;
	}
	
	private void logConfiguration(Configuration configuration){
		if (!log.isInfoEnabled()) {
			return;
		}
		Map<String, CacheConfiguration> cacheConfigurations = configuration.getCacheConfigurations();
		Set<Entry<String,CacheConfiguration>> entrySet = cacheConfigurations.entrySet();
		log.info("EhCache final caches:");
		for (Entry<String, CacheConfiguration> entry : entrySet) {
			StringBuilder sb = new StringBuilder();
			sb.append(entry.getKey()).append(": ").append(entry.getValue());
			log.info(sb.toString());
			List<CacheEventListenerFactoryConfiguration> cacheEventListenerConfigurations = entry.getValue().getCacheEventListenerConfigurations();
			for (CacheEventListenerFactoryConfiguration factoryConfiguration : cacheEventListenerConfigurations) {
				log.info("	cacheEventListenerFactory: classPath:{}",factoryConfiguration.getFullyQualifiedClassPath());
				log.info("	properties:{},listenFor:{}",factoryConfiguration.getProperties(),factoryConfiguration.getListenFor());
			}
		}
		
		List<FactoryConfiguration> cacheManagerPeerProviderFactoryConfiguration = configuration.getCacheManagerPeerProviderFactoryConfiguration();
		for (FactoryConfiguration factoryConfiguration : cacheManagerPeerProviderFactoryConfiguration) {
			log.info("cacheManagerPeerProviderFactory: classPath:{}",factoryConfiguration.getFullyQualifiedClassPath());
			log.info("properties:{}",factoryConfiguration.getProperties());
		}
		
		List<FactoryConfiguration> cacheManagerPeerListenerFactoryConfigurations = configuration.getCacheManagerPeerListenerFactoryConfigurations();
		for (FactoryConfiguration factoryConfiguration : cacheManagerPeerListenerFactoryConfigurations) {
			log.info("cacheManagerPeerListenerFactory: classPath:{}",factoryConfiguration.getFullyQualifiedClassPath());
			log.info("properties:{}",factoryConfiguration.getProperties());
		}
		
	}

}
