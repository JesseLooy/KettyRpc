package org.nettyrpc.netty.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import org.nettyrpc.netty.RpcRequest;
import org.nettyrpc.netty.RpcResponse;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RpcSendRequest extends SimpleChannelInboundHandler<RpcResponse> {

    private static ChannelHandlerContext ctx;

    public static final Map<String, RpcResponse> responseMap = new ConcurrentHashMap<>();

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.ctx = ctx;
        super.channelActive(ctx);
    }
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse rpcResponse) throws Exception {
        responseMap.put(rpcResponse.getRequestId(), rpcResponse);
        synchronized (RpcSendRequest.class) {
            RpcSendRequest.class.notifyAll();
        }
    }

    public static void sendRequest(RpcRequest rpcRequest){
        if (ctx == null) {
            System.out.println("还未激活");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if(ctx == null){
                throw new RuntimeException("ChannelHandlerContext is not ready.Please wait netty client to open");
            }
        }
        ctx.writeAndFlush(rpcRequest);
    }
}
