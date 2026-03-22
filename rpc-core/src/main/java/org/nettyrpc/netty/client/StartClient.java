package org.nettyrpc.netty.client;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.nettyrpc.netty.server.RpcRequestDecoder;
import org.nettyrpc.netty.server.RpcResponseEncoder;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class StartClient implements SmartInitializingSingleton {

    private final ApplicationContext applicationContext;

    public StartClient(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterSingletonsInstantiated() {
        new Thread(()->{
            EventLoopGroup bossGroup = new MultiThreadIoEventLoopGroup(1, NioIoHandler.newFactory());
            EventLoopGroup workerGroup = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());
            ServerBootstrap bootstrap = new ServerBootstrap();
            try{
                bootstrap.group(bossGroup,workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .childOption(ChannelOption.SO_KEEPALIVE,true)
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel socketChannel) throws Exception {
                                ChannelPipeline pl = socketChannel.pipeline();
                                pl.addLast(new RpcResponseEncoder());
                                pl.addLast(new RpcRequestDecoder());
                                pl.addLast(new RpcSendRequest());
                            }
                        });
                bootstrap.bind(8090).sync();
            }catch (Exception e){
                throw new RuntimeException("Unknown Exception -> " + e);
            }

        }).start();
    }
}