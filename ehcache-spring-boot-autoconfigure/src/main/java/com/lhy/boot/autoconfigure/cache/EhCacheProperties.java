package com.lhy.boot.autoconfigure.cache;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * @ClassName: EhCacheProperties
 * @Description: ehcache集群配置属性
 * @author luanhy
 * @date 2017年8月19日 下午3:24:55
 * @Copyright: Copyright (c) 2017 wisedu
 */
@ConfigurationProperties(prefix = EhCacheProperties.EHCACHE_CLUSTER_PREFIX)
@Data
@ToString
@Slf4j
public class EhCacheProperties {

	public static final String EHCACHE_CLUSTER_PREFIX = "ehcache";
	public static final String ENABLED = "enabled";

	private static final String RESERVED_ADDRESS = "127.0.0.1";

	private static final String split = ":";

	/**
	 * 是否启用集群 默认不启用
	 */
	private boolean clusterEnabled = false;

	/**
	 * 无需同步的缓存名称 |分隔 TODO:
	 */
	private String unSyncCacheNames;

	private Cluster cluster = new Cluster();

	private List<Cache> caches = new ArrayList<Cache>();

	@Data
	public static class Cluster {
		private Provider provider = new Provider();

		private Listener listener = new Listener();

		@Data
		public static class Provider {

			public static final String PEERDISCOVERY_AUTOMATIC = "automatic";
			public static final String PEERDISCOVERY_MANUAL = "manual";
			/**
			 * 缓存提供者rmi通信方式 自动和手动 默认自动
			 */
			private String peerDiscovery = PEERDISCOVERY_AUTOMATIC;

			private Automatic automatic = new Automatic();

			private Manual manual = new Manual();

			@Data
			public static class Automatic {

				private static final String DEFAULT_MULTICAST_GROUP_IP = "224.1.1.1";
				private static final int DEFAULT_MULTICAST_GROUP_PORT = 41000;

				/**
				 * 存活时间 默认32
				 */
				private int timeToLive = 32;

				/**
				 * 缓存提供者广播组地址 D 类 IP 地址，范围从 224.0.1.0 到 238.255.255.255 默认：224.1.1.1
				 */
				private String multicastGroupAddress = DEFAULT_MULTICAST_GROUP_IP;

				/**
				 * 缓存提供者广播组端口 默认：41000
				 */
				private int multicastGroupPort = DEFAULT_MULTICAST_GROUP_PORT;
			}

			@Data
			public static class Manual {

				private static final int DEFAULT_RMI_NOTIFY_PORT = 40001;

				/**
				 * 缓存提供者需要通知的rmi地址 多个 | 分隔 例如：127.0.0.1:40001
				 */
				private String rmiUrls = RESERVED_ADDRESS + split + DEFAULT_RMI_NOTIFY_PORT;

			}
		}

		@Data
		public static class Listener {

			private static final int DEFAULT_LISTENER_PORT = 40002;

			private static final int DEFAULT_MAX_RANDOM_PORT = 40999;

			private static final int DEFAULT_MIN_RANDOM_PORT = 40000;

			/**
			 * 缓存监听者端口是否随机 （仅自动配置时生效）
			 */
			private boolean randomPortEnabled = true;

			/**
			 * 缓存监听者端口地址 默认:127.0.0.1
			 */
			private String hostName = RESERVED_ADDRESS;

			/**
			 * 缓存监听者默认端口 默认：40002
			 */
			private int port = DEFAULT_LISTENER_PORT;

			/**
			 * socket连接超时时间 默认2000毫秒
			 */
			private long socketTimeoutMillis = 2000L;

			/**
			 * 随机端口范围上限 默认 40999
			 */
			private int maxRandomPort = DEFAULT_MAX_RANDOM_PORT;

			/**
			 * 随机端口范围下限 默认 40000
			 */
			private int minRandomPort = DEFAULT_MIN_RANDOM_PORT;

		}

	}

	@Data
	public static class Cache {

		public static final String DEFAULT_RMI_CACHE_REPLICATOR_FACTORY_PROPERTIES_NAME = "replicatePuts=true,replicateUpdates=true,replicateRemovals=true,replicateAsynchronously=true,replicatePutsViaCopy=true,replicateUpdatesViaCopy=true,asynchronousReplicationIntervalMillis=true";
		public static final String DEFAULT_MEMORY_STORE_EVICTION_POLICY = "LRU";

		/**
		 * 缓存名称
		 */
		String name;
		/**
		 * cache 中最多可以存放的元素的数量。如果放入cache中的元素超过这个数值，有两种情况： <br>
		 * 1、若overflowToDisk的属性值为true，会将cache中多出的元素放入磁盘文件中。 <br>
		 * 2、若overflowToDisk的属性值为false，
		 * 会根据memoryStoreEvictionPolicy的策略替换cache中原有的元素。
		 */
		long maxElementsInMemory = 100L;
		/**
		 * 缓存的3 种清空策略 ： <br>
		 * FIFO ，first in first out (先进先出). <br>
		 * LFU ， Less Frequently Used (最少使用).意思是一直以来最少被使用的。缓存的元素有一个hit 属性，hit
		 * 值最小的将会被清出缓存。 <br>
		 * LRU ，Least Recently Used(最近最少使用). (ehcache
		 * 默认值).缓存的元素有一个时间戳，当缓存容量满了，而又需要腾出地方来缓存新的元素的时候，
		 * 那么现有缓存元素中时间戳离当前时间最远的元素将被清出缓存。
		 */
		String memoryStoreEvictionPolicy = DEFAULT_MEMORY_STORE_EVICTION_POLICY;
		/**
		 * 设定缓存的elements是否永远不过期。如果为true，则缓存的数据始终有效，
		 * 如果为false那么还要根据timeToIdleSeconds，timeToLiveSeconds判断。
		 */
		boolean eternal;
		/**
		 * 对象空闲时间，指对象在多长时间没有被访问就会失效。只对eternal为false的有效。默认值0，表示一直可以访问。以秒为单位。
		 */
		long timeToIdleSeconds = 0L;
		/**
		 * 对象存活时间，指对象从创建到失效所需要的时间。只对eternal为false的有效。默认值0，表示一直可以访问。以秒为单位。
		 */
		long timeToLiveSeconds = 0L;
		/**
		 * 如果内存中数据超过内存限制，是否要缓存到磁盘上。默认false
		 */
		boolean overflowToDisk;
		/**
		 * 是否在磁盘上持久化。指重启jvm后，数据是否有效。默认为false。
		 */
		boolean diskPersistent;

		/**
		 * 在磁盘上缓存的element的最大数目，默认值为0，表示不限制。
		 */
		int maxElementsOnDisk;

		/**
		 * 对象检测线程运行时间间隔。标识对象状态的线程多长时间运行一次。以秒为单位。默认120 秒
		 */
		long diskExpiryThreadIntervalSeconds = 120L;

		/**
		 * DiskStore使用的磁盘大小，默认值30MB。每个cache使用各自的DiskStore。
		 */
		int diskSpoolBufferSizeMB = 30;

		RMICacheReplicatorFactory rmiCacheReplicatorFactory = new RMICacheReplicatorFactory();

		@Data
		public static class RMICacheReplicatorFactory {
			String properties = DEFAULT_RMI_CACHE_REPLICATOR_FACTORY_PROPERTIES_NAME;
		}
	}

	@PostConstruct
	private void init() {
		if (clusterEnabled) {
			Cluster cluster = this.getCluster();
			// 自动方式并且配置随机端口
			if (cluster.listener.getRandomPortEnabled()
					&& Cluster.Provider.PEERDISCOVERY_AUTOMATIC.equals(cluster.provider.getPeerDiscovery())) {
				// 41000 默认广播随机端口号 不能随机
				EhCacheHelper.PortUtil portUtil = new EhCacheHelper.PortUtil();
				// 40000 -40999监听随机端口号
				cluster.listener.setPort(portUtil.getUnAvailableRandomPort(cluster.listener.getMinRandomPort(),
						cluster.listener.getMaxRandomPort()));
			}
			log.info("ehcache rmi listener port:{}", cluster.listener.getPort());
		}
	}

}
