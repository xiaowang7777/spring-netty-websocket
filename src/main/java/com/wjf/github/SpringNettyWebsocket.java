package com.wjf.github;

import com.wjf.github.config.NettyServerProperties;
import com.wjf.github.netty.NettyServer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SpringNettyWebsocket {
	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(SpringNettyWebsocket.class);
		NettyServer nettyServer = context.getBean(NettyServer.class);
		nettyServer.start();
	}

	@Autowired
	private NettyServerProperties properties;

	@Bean(name = "bossEventLoopGroup")
	public EventLoopGroup bossEventLoopGroup() {
		return new NioEventLoopGroup(properties.getNettyServerBossThreadCount());
	}

	@Bean(name = "workEventLoopGroup")
	public EventLoopGroup workEventLoopGroup() {
		return new NioEventLoopGroup(properties.getNettyServerWorkThreadCount());
	}

}
