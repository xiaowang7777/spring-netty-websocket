package com.wjf.github.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class WebSocketHandler extends SimpleChannelInboundHandler<Object> {

	private static final Logger log = LoggerFactory.getLogger(WebSocketHandler.class);

	private WebSocketServerHandshaker handshaker;

	private final String lastPath;

	public WebSocketHandler(String lastPath) {
		this.lastPath = lastPath;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		log.info("there's a client connect! and id is {}", ctx.channel().id().asLongText());
		ctx.channel().writeAndFlush("Welcome!This is a message from server!Your id is " + ctx.channel().id().asLongText());
		super.channelActive(ctx);
	}

	@Override
	protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {

		if (o instanceof FullHttpRequest) {
			handHttpRequest(channelHandlerContext, (FullHttpRequest) o);
		}

		if (o instanceof WebSocketFrame) {
			handWebSocketRequest(channelHandlerContext, (WebSocketFrame) o);
		}

	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		if(evt instanceof IdleStateEvent){
			ctx.channel().writeAndFlush(new TextWebSocketFrame(new Date().toString() + ". Hi! This is a check msg."));
		}else {
			super.userEventTriggered(ctx, evt);
		}
	}

	private void handWebSocketRequest(ChannelHandlerContext ctx, WebSocketFrame frame) {
		if (frame instanceof CloseWebSocketFrame) {
			handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
			return;
		}
		if (frame instanceof PingWebSocketFrame) {
			ctx.channel().writeAndFlush(new PongWebSocketFrame(frame.content().retain()));
			log.info("get a ping message from {}",ctx.channel().id().asLongText());
			return;
		}
		if (frame instanceof TextWebSocketFrame) {
			TextWebSocketFrame textWebSocketFrame = (TextWebSocketFrame) frame;
			log.info("get message {} from {}", textWebSocketFrame.text(), ctx.channel().id().asLongText());
			TextWebSocketFrame res = new TextWebSocketFrame(new Date().toString() + ". Hi! This is a response msg to response " + textWebSocketFrame.text());
			ctx.channel().writeAndFlush(res);
		}
	}

	/**
	 * 处理http请求
	 *
	 * @param ctx
	 * @param request
	 */
	private void handHttpRequest(ChannelHandlerContext ctx, FullHttpRequest request) {
		if (!request.decoderResult().isSuccess() || !("websocket".equals(request.headers().get("Upgrade")))) {
			sendHttpResponse(ctx, request, new DefaultFullHttpResponse(HttpVersion.HTTP_1_0, HttpResponseStatus.BAD_REQUEST));
			return;
		}

		WebSocketServerHandshakerFactory handshakerFactory = new WebSocketServerHandshakerFactory(getLocation(request), null, false);

		handshaker = handshakerFactory.newHandshaker(request);
		if (handshaker == null) {
			WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
		} else {
			handshaker.handshake(ctx.channel(), request);
		}
	}

	/**
	 * 发送http信息
	 *
	 * @param ctx
	 * @param req
	 * @param res
	 */
	private void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse res) {
		//当响应状态码不为200时，将响应状态写入到相应内容中
		if (res.status().code() != 200) {
			ByteBuf buf = Unpooled.copiedBuffer(res.status().toString(), CharsetUtil.UTF_8);
			res.content().writeBytes(buf);
			buf.release();
		}
		ChannelFuture channelFuture = ctx.channel().writeAndFlush(res);
		if (!HttpUtil.isKeepAlive(req) || res.status().code() != 200) {
			channelFuture.addListener(ChannelFutureListener.CLOSE);
		}
	}

	private String getLocation(FullHttpRequest req) {
		String path = req.headers().get(HttpHeaderNames.HOST) + lastPath;
		return "ws://" + path;
	}
}
