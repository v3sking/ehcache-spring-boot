package com.lhy.boot.autoconfigure.cache;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.springframework.util.StringUtils;

import com.lhy.boot.autoconfigure.cache.EhCacheProperties.Cache;

import lombok.extern.slf4j.Slf4j;

/**
 * 
* @ClassName: EhCacheHelper
* @Description:帮助类
* @author  luanhy
* @date 2018年1月8日 下午9:41:11
* @Copyright: Copyright (c) 2017 wisedu
 */
@Slf4j
public class EhCacheHelper {
	
	/**
	 *
	* @Title: getCache
	* @Description:  获取缓存配置
	* @return Cache    返回类型
	* @param ehCacheProperties
	* @param cacheName
	* @return
	 */
	public Cache getCache(EhCacheProperties ehCacheProperties, String cacheName){
		List<Cache> caches = ehCacheProperties.getCaches();
		for (Cache cache : caches) {
			if (cache.getName().equals(cacheName)) {
				return cache;
			}
		}
		return new Cache();
	}
	
	/**
	 * 
	* @Title: makeRmiUrls
	* @Description: 拼装rmiUrl 原始只有 uri,拼装后增加cacheKey
	* @return String    返回类型 
	* <br>//127.0.0.1:10001/user|//127.0.0.1:10001/role|//127.0.0.1:10002/user|//127.0.0.1:10002/role
	* @param oldRmiUrls 127.0.0.1:10001|127.0.0.1:10002|...
	* @param cacheNames Set<String>   user,role,....
	 */
	public String makeRmiUrls(String oldRmiUrls, Set<String> cacheNames){
		StringTokenizer stringTokenizer = new StringTokenizer(oldRmiUrls, "|");
		StringBuilder rmiUrlsStr = new StringBuilder();
		while (stringTokenizer.hasMoreTokens()) {
			String rmiUrl = stringTokenizer.nextToken();
			rmiUrl = rmiUrl.trim();
			for (String key : cacheNames) {
				rmiUrlsStr.append("//").append(rmiUrl).append("/").append(key).append("|");
			}
		}
		rmiUrlsStr = rmiUrlsStr.deleteCharAt(rmiUrlsStr.length() - 1);
		log.debug("last rmiUrls：" + rmiUrlsStr.toString());
		return rmiUrlsStr.toString();
	}
	
	/**
	 * 
	* @ClassName: PortUtil
	* @Description: 随机端口工具类
	* @author  luanhy
	* @date 2018年1月8日 下午9:41:11
	* @Copyright: Copyright (c) 2017 wisedu
	 */
	@Slf4j
	static class PortUtil {

		private int maxRandomCount = 1000;

		private int randomCount = 0;

		public void resetCount() {
			randomCount = 0;
		}

		/**
		 * 
		 * @Title: isPortAvailable
		 * @Description: 端口是否未被用
		 * @param port
		 * @return
		 */
		public boolean isPortAvailable(int port) {
			try {

				String[] commond = new String[2];
				commond[0] = "netstat";
				String encoding = "gbk";
				SystemCommandUtil systemCommandUtil = new SystemCommandUtil();
				if (systemCommandUtil.isWindows()) {
					commond[1] = "-aon";
				} else {
					commond[1] = "-anp";
					encoding = "utf-8";
				}
				String ret = systemCommandUtil.excuteCmdMultiThread(commond, encoding);
				boolean matches = Pattern.compile("(.+)(" + port + "\\s+)(.*)").matcher(ret).find();
				return !matches;
			} catch (Exception e) {
				log.error("", e);
				return false;
			}
		}

		/**
		 * 
		 * @Title: getRandomPort
		 * @Description: 获取随机端口号
		 * @param minPort
		 * @param maxPort
		 * @return
		 */
		private int getRandomPort(int minPort, int maxPort) {
			Random random = new Random();
			int s = random.nextInt(maxPort) % (maxPort - minPort + 1) + minPort;
			return s;
		}

		/**
		 * 
		 * @Title: getUnAvailablePort
		 * @Description:获取未被占用的随机端口号
		 * @param minPort
		 * @param maxPort
		 * @return
		 */
		public int getUnAvailableRandomPort(int minPort, int maxPort) {
			if ((++randomCount) > maxRandomCount) {
				throw new RuntimeException("无法从" + minPort + "到" + maxPort + "绑定ehcache rmi同步端口号,请检查端口占用情况");
			}
			int randomPort = getRandomPort(minPort, maxPort);
			if (!isPortAvailable(randomPort)) {
				return getUnAvailableRandomPort(minPort, maxPort);
			}
			return randomPort;
		}
		
	}
	
	/**
	 * 
	* @ClassName: SystemCommandUtil
	* @Description: 命令工具类
	* @author  luanhy
	* @date 2018年1月8日 下午9:40:51
	* @Copyright: Copyright (c) 2017 wisedu
	 */
	@Slf4j
	static class SystemCommandUtil {
		
		private static final String DEFAULT_ENCODING = "GBK";//编码  
		private static final int PROTECTED_LENGTH = 51200;// 输入流保护 50KB  

		public boolean isWindows() {
			String osName = System.getProperty("os.name");
			return osName.indexOf("Windows") != -1;
		}

		/** 执行外部程序,并获取标准输出 */
		public String excuteCmdMultiThread(String[] cmd, String encoding) {
			Process p = null;
			try {
				p = Runtime.getRuntime().exec(cmd);
				/* 为"错误输出流"单独开一个线程读取之,否则会造成标准输出流的阻塞 */
				Thread t = new Thread(new InputStreamRunnable(p.getErrorStream(), "ErrorStream"));
				t.start();
				/* "标准输出流"就在当前方法中读取 */
				String encodingStr = StringUtils.isEmpty(encoding) ? DEFAULT_ENCODING : encoding;
				String readInfoStream = readInfoStream(p.getInputStream(), encodingStr);
				return readInfoStream;
			} catch (Exception e) {
				log.error("执行外部程序,并获取标准输出异常", e);
			} finally {
				p.destroy();
			}
			return null;
		}
		
		public String readInfoStream(InputStream input, String encoding) throws Exception {  
		    if (input == null) {  
		        throw new Exception("输入流为null");  
		    }  
		    //字节数组  
		    byte[] bcache = new byte[2048];  
		    int readSize = 0;//每次读取的字节长度  
		    int totalSize = 0;//总字节长度  
		    ByteArrayOutputStream infoStream = new ByteArrayOutputStream();  
		    try {  
		        //一次性读取2048字节  
		        while ((readSize = input.read(bcache)) > 0) {  
		            totalSize += readSize;  
		            if (totalSize > PROTECTED_LENGTH) {  
		                throw new Exception("输入流超出50K大小限制");  
		            }  
		            //将bcache中读取的input数据写入infoStream  
		            infoStream.write(bcache,0,readSize);  
		        }  
		    } catch (IOException e1) {  
		        throw new Exception("输入流读取异常");  
		    } finally {  
		        try {  
		            //输入流关闭  
		            input.close();  
		        } catch (IOException e) {  
		            throw new Exception("输入流关闭异常");  
		        }  
		    }  
		  
		    try {  
		        return infoStream.toString(encoding);  
		    } catch (UnsupportedEncodingException e) {  
		        throw new Exception("输出异常");  
		    }  
		}  

		/** 读取InputStream的线程 */
		class InputStreamRunnable implements Runnable {
			BufferedReader bReader = null;
			String type = null;

			public InputStreamRunnable(InputStream is, String typeStr) {
				try {
					bReader = new BufferedReader(new InputStreamReader(new BufferedInputStream(is), "UTF-8"));
					type = typeStr;
				} catch (Exception ex) {
					log.error("读取InputStream的线程异常", ex);
				}
			}

			public void run() {
				String line = null;
				try {
					while ((line = bReader.readLine()) != null) {
						log.error(line);
					}
					bReader.close();
				} catch (Exception ex) {
					log.error("", ex);
				}
			}
		}
	}
	

}
