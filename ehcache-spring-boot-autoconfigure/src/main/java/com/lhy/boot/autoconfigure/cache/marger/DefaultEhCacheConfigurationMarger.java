package com.lhy.boot.autoconfigure.cache.marger;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.lhy.boot.autoconfigure.cache.EhCacheHelper;
import com.lhy.boot.autoconfigure.cache.EhCacheProperties;
import com.lhy.boot.autoconfigure.cache.EhCacheProperties.Cache;
import com.lhy.boot.autoconfigure.cache.builder.CacheEventListenerFactoryBuilder;
import com.lhy.boot.autoconfigure.cache.builder.CacheManagerPeerProviderFactoryBuilder;

import lombok.AllArgsConstructor;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.CacheConfiguration.CacheEventListenerFactoryConfiguration;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.FactoryConfiguration;

/**
 * 
* @ClassName: DefaultEhCacheConfigurationMarger
* @Description: 默认合并器 ehcache.xml解析出的Configuration和 {@link EhCacheProperties} Configuration解析出的 配置 如果重复,以ehcache为准
* @author  hyluan
* @date 2018年1月11日 下午3:56:16
* @Copyright: Copyright (c) 2017 wisedu
 */
@AllArgsConstructor
public class DefaultEhCacheConfigurationMarger implements EhCacheConfigurationMarger{
	
	EhCacheProperties ehCacheProperties;
	
	/**
	 * 
	* @Title: marge
	* @Description:合并配置 如果存在ehcache.xml文件则合并配置 以ehcache.xml为准
	* @return void    返回类型
	* @param defaultConfiguration
	* @param customConfiguration
	 */
	@Override
	public void marge(Configuration sourceConfiguration, Configuration targetConfiguration) {
		
		margeCacheConfiguration(sourceConfiguration.getCacheConfigurations(), targetConfiguration.getCacheConfigurations());
		
		if (ehCacheProperties.getClusterEnabled()) {
			margeCacheManagerPeerProviderFactoryConfiguration(sourceConfiguration.getCacheManagerPeerProviderFactoryConfiguration(), targetConfiguration.getCacheManagerPeerProviderFactoryConfiguration(), targetConfiguration.getCacheConfigurationsKeySet());
			
			margeCacheManagerPeerListenerFactoryConfigurations(sourceConfiguration.getCacheManagerPeerListenerFactoryConfigurations(), targetConfiguration.getCacheManagerPeerListenerFactoryConfigurations());
		}
	}
	
	/**
	 * 
	* @Title: margeCacheConfiguration
	* @Description: 合并margeCacheConfiguration
	* @return void    返回类型
	* @param defaultCacheConfigurations
	* @param customCacheConfigurations
	 */
	protected void margeCacheConfiguration(Map<String, CacheConfiguration> defaultCacheConfigurations,Map<String, CacheConfiguration> customCacheConfigurations){
		EhCacheHelper ehCacheHelper = new EhCacheHelper();
		if (defaultCacheConfigurations.isEmpty()) {
			return;
		}
		Set<Entry<String,CacheConfiguration>> customEntrySet = customCacheConfigurations.entrySet();
		Set<Entry<String,CacheConfiguration>> defaultEntrySet = defaultCacheConfigurations.entrySet();
		if (customEntrySet.isEmpty()) {
			customCacheConfigurations.putAll(defaultCacheConfigurations);
		}else{
			//设置自定义配置没有的属性  不putall防止覆盖
			for (Entry<String, CacheConfiguration> entry : defaultEntrySet) {
				if (!customCacheConfigurations.containsKey(entry.getKey())) {
					customCacheConfigurations.put(entry.getKey(), entry.getValue());
				}
			}
			if (ehCacheProperties.getClusterEnabled()) {
				margeCacheEventListenerFactory(ehCacheHelper, customEntrySet);
			}
		}
	}

	/**
	* @Title: margeCacheEventListenerFactory
	* @Description:
	* @param ehCacheHelper
	* @param customEntrySet  
	*/
	private void margeCacheEventListenerFactory(EhCacheHelper ehCacheHelper, Set<Entry<String, CacheConfiguration>> customEntrySet) {
		//设置CacheEventListenerFactory
		for (Entry<String, CacheConfiguration> entry : customEntrySet) {
			List<CacheEventListenerFactoryConfiguration> cacheEventListenerConfigurations = entry.getValue().getCacheEventListenerConfigurations();
			//没有Listener的设置为默认
			if (cacheEventListenerConfigurations.isEmpty()) {
				Cache cache = ehCacheHelper.getCache(ehCacheProperties, entry.getValue().getName());
				CacheEventListenerFactoryBuilder cacheEventListenerFactoryBuilder = new CacheEventListenerFactoryBuilder(cache);
				entry.getValue().addCacheEventListenerFactory(cacheEventListenerFactoryBuilder.build());
			}
		}
	}
	
	protected void margeCacheManagerPeerProviderFactoryConfiguration(List<FactoryConfiguration> defaultProviderFactoryConfiguration, List<FactoryConfiguration> customProviderFactoryConfiguration, Set<String> cacheNames) {
		if (defaultProviderFactoryConfiguration.isEmpty()) {
			return;
		}
		if (customProviderFactoryConfiguration.isEmpty()) {
			CacheManagerPeerProviderFactoryBuilder cacheManagerPeerProviderFactoryBuilder = new CacheManagerPeerProviderFactoryBuilder(ehCacheProperties, cacheNames);
			customProviderFactoryConfiguration.add(cacheManagerPeerProviderFactoryBuilder.build());
		}
	}
	
	protected void margeCacheManagerPeerListenerFactoryConfigurations(List<FactoryConfiguration> defaultListenerFactoryConfiguration, List<FactoryConfiguration> customListenerFactoryConfiguration) {
		if (defaultListenerFactoryConfiguration.isEmpty()) {
			return;
		}
		if (customListenerFactoryConfiguration.isEmpty()) {
			customListenerFactoryConfiguration.addAll(defaultListenerFactoryConfiguration);
		}
	}
	


}
