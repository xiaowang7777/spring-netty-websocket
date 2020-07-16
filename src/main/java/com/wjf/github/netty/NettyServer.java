package com.wjf.github.netty;

import com.wjf.github.config.NettyServerProperties;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.concurrent.TimeUnit;

@Component
public class NettyServer {

	private static final Logger log = LoggerFactory.getLogger(NettyServer.class);

	@Autowired
	private NettyServerProperties properties;

	@Autowired
	@Qualifier("bossEventLoopGroup")
	private EventLoopGroup bossGroup;

	@Autowired
	@Qualifier("workEventLoopGroup")
	private EventLoopGroup workGroup;

	private Channel channel = null;

	public void start() {
		log.info("start to init netty websocket server.");

		try {
			ServerBootstrap bootstrap = new ServerBootstrap();
			bootstrap.group(bossGroup, workGroup)
					.channel(NioServerSocketChannel.class)
					.childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						protected void initChannel(SocketChannel channel) throws Exception {
							channel.pipeline()
									.addLast(new IdleStateHandler(3,3,3, TimeUnit.SECONDS))
									.addLast(new HttpServerCodec())
									.addLast(new HttpObjectAggregator(1024 * 64))
									.addLast(new ChunkedWriteHandler())
									.addLast(new WebSocketHandler(properties.getNettyServerLastPath()));
						}
					});
			ChannelFuture channelFuture = bootstrap.bind(properties.getNettyPort()).sync();
			log.info("server bind port {} successfully! ",properties.getNettyPort());
			channelFuture.channel().closeFuture().sync();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	@PreDestroy
	public void stop() {
		try {
			log.info("It's time to close netty server.");
			if (channel != null) {
				channel.close().sync();
				channel.parent().close().sync();
			}
			workGroup.shutdownGracefully().sync();
			bossGroup.shutdownGracefully().sync();
			log.info("close netty server success!");
		} catch (InterruptedException e) {
			e.printStackTrace();
			log.info("fail to close netty server and throw some exception");
		}
	}
}
