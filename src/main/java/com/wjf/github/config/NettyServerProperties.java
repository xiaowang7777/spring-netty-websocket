package com.wjf.github.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "project")
public class NettyServerProperties {

	private Integer nettyPort;
	private Integer nettyServerBossThreadCount;
	private Integer nettyServerWorkThreadCount;
	private String nettyServerLastPath;

	public Integer getNettyPort() {
		return nettyPort;
	}

	public void setNettyPort(Integer nettyPort) {
		this.nettyPort = nettyPort;
	}

	public Integer getNettyServerBossThreadCount() {
		return nettyServerBossThreadCount;
	}

	public void setNettyServerBossThreadCount(Integer nettyServerBossThreadCount) {
		this.nettyServerBossThreadCount = nettyServerBossThreadCount;
	}

	public Integer getNettyServerWorkThreadCount() {
		return nettyServerWorkThreadCount;
	}

	public void setNettyServerWorkThreadCount(Integer nettyServerWorkThreadCount) {
		this.nettyServerWorkThreadCount = nettyServerWorkThreadCount;
	}

	public String getNettyServerLastPath() {
		return nettyServerLastPath;
	}

	public void setNettyServerLastPath(String nettyServerLastPath) {
		this.nettyServerLastPath = nettyServerLastPath;
	}
}
