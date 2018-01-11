package com.lhy.boot.autoconfigure.cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ResourceCondition;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.Cache;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import com.lhy.boot.autoconfigure.cache.builder.EhCacheConfigurationBuilder;
import com.lhy.boot.autoconfigure.cache.marger.DefaultEhCacheConfigurationMarger;
import com.lhy.boot.autoconfigure.cache.marger.EhCacheConfigurationMarger;

import net.sf.ehcache.CacheManager;

/**
 * 
 * @ClassName: EhCacheCacheConfiguration
 * @Description: ehcache缓存集群自动配置类
 * @author luanhy
 * @date 2018年1月8日 下午9:49:57
 * @Copyright: Copyright (c) 2017 wisedu
 */
@Configuration
@ConditionalOnClass({ Cache.class, EhCacheCacheManager.class })
@EnableConfigurationProperties(EhCacheProperties.class)
@AutoConfigureBefore(CacheAutoConfiguration.class)
public class EhCacheCacheConfiguration {

	private final EhCacheProperties ehCacheProperties;

	private final CacheProperties cacheProperties;

	EhCacheCacheConfiguration(CacheProperties cacheProperties,
			EhCacheProperties ehCacheProperties) {
		this.cacheProperties = cacheProperties;
		this.ehCacheProperties = ehCacheProperties;
	}

	@Bean
	@ConditionalOnMissingBean
	public CacheManager ehCacheCacheManager(@Autowired(required=false) EhCacheConfigurationMarger ehCacheConfigurationMarger) {
		Resource location = this.cacheProperties.resolveConfigLocation(this.cacheProperties.getEhcache().getConfig());
		EhCacheConfigurationBuilder ehCacheConfigurationBuilder = new EhCacheConfigurationBuilder(
				ehCacheProperties, location, ehCacheConfigurationMarger);
		return CacheManager.create(ehCacheConfigurationBuilder.build());
	}

	@Bean
	@ConditionalOnMissingBean
	@Conditional(EhCacheCacheConfiguration.ConfigAvailableCondition.class)
	public EhCacheConfigurationMarger ehCacheConfigurationMarger() {
		return new DefaultEhCacheConfigurationMarger(ehCacheProperties);
	}
	
	static class ConfigAvailableCondition extends ResourceCondition {

		ConfigAvailableCondition() {
			super("EhCache", "spring.cache.ehcache", "config", "classpath:/ehcache.xml");
		}

	}

}
