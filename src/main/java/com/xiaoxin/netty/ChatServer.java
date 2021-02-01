package com.xiaoxin.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.springframework.stereotype.Component;

@Component
public class ChatServer {

    public static final ChatServer INSTANCE = new ChatServer();

    private NioEventLoopGroup bossGroup;
    private NioEventLoopGroup workerGroup;
    private ServerBootstrap boot;
    private Channel channel;

    private ChatServer(){
        initObjects();
    }

    private void initObjects() {
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        boot = new ServerBootstrap();
        boot.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY,true)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel sc) throws Exception {
                        sc.pipeline().addLast(new HttpServerCodec())
                                .addLast(new ChunkedWriteHandler())
                                .addLast(new HttpObjectAggregator(1024*64))
                                .addLast(new WebSocketServerProtocolHandler("/cs"))
                                // 针对客户端空闲状态设置
                                .addLast(new IdleStateHandler(30,30,60))
                                .addLast(new HeartBeatHandler())  // 自定义心跳空闲检测
                                .addLast(new ChatHandler());    // 自定义数据处理类
                    }
                });

    }

    public void start(int port) {
        this.channel = boot.bind(port).channel();
        System.err.println("---netty server start---");
    }
}
