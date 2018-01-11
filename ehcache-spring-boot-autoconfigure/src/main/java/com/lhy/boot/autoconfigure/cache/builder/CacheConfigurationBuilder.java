package com.lhy.boot.autoconfigure.cache.builder;

import org.springframework.beans.BeanUtils;

import com.lhy.boot.autoconfigure.cache.EhCacheHelper;
import com.lhy.boot.autoconfigure.cache.EhCacheProperties;
import com.lhy.boot.autoconfigure.cache.EhCacheProperties.Cache;

import lombok.AllArgsConstructor;
import net.sf.ehcache.config.CacheConfiguration;

/**
 * 
* @ClassName: CacheConfigurationBuilder
* @Description: 缓存配置建造者
* @author  hyluan
* @date 2018年1月10日 下午4:30:09
* @Copyright: Copyright (c) 2017 wisedu
 */
@AllArgsConstructor
public class CacheConfigurationBuilder implements Builder<CacheConfiguration>{
	
	EhCacheProperties ehCacheProperties;
	String cacheName;
	
	
	/**
	 * 	cacheConfiguration.setMemoryStoreEvictionPolicy(cache.getMemoryStoreEvictionPolicy());
		<br>cacheConfiguration.setEternal(cache.getEternal());
		<br>cacheConfiguration.setTimeToIdleSeconds(cache.getTimeToIdleSeconds());
		<br>cacheConfiguration.setTimeToLiveSeconds(cache.getTimeToLiveSeconds());
		<br>cacheConfiguration.setOverflowToDisk(cache.getOverflowToDisk());
		<br>cacheConfiguration.setDiskPersistent(cache.getDiskPersistent());
		<br>cacheConfiguration.setMaxElementsOnDisk(cache.getMaxElementsOnDisk());
		<br>cacheConfiguration.setDiskExpiryThreadIntervalSeconds(cache.getDiskExpiryThreadIntervalSeconds());
		<br>cacheConfiguration.setDiskSpoolBufferSizeMB(cache.getDiskSpoolBufferSizeMB());
	 */
	@Override
	public CacheConfiguration build() {
		CacheConfiguration cacheConfiguration = new CacheConfiguration().name(cacheName);
		EhCacheHelper helper = new EhCacheHelper();
		Cache cache = helper.getCache(ehCacheProperties, cacheName);
		BeanUtils.copyProperties(cache, cacheConfiguration);
		cacheConfiguration.setMaxEntriesLocalHeap(cache.getMaxElementsInMemory());
		
		if (ehCacheProperties.getClusterEnabled()) {
			buildCacheEventListenerFactory(cacheConfiguration, cache);
		}
		return cacheConfiguration;
	}


	/**
	* @Title: buildCacheEventListenerFactory
	* @Description: 
	* @param cacheConfiguration
	* @param cache  
	*/
	private void buildCacheEventListenerFactory(CacheConfiguration cacheConfiguration, Cache cache) {
		CacheEventListenerFactoryBuilder cacheEventListenerFactoryBuilder = new CacheEventListenerFactoryBuilder(cache);
		cacheConfiguration.addCacheEventListenerFactory(cacheEventListenerFactoryBuilder.build());
	}
	
	
	
}
