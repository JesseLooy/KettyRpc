package org.nettyrpc.netty.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.nettyrpc.netty.RpcRequest;
import org.nettyrpc.netty.RpcResponse;
import org.nettyrpc.scan.server.RpcServerScannerRegistrar;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

public class RequestHandler extends SimpleChannelInboundHandler<RpcRequest> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest rpcRequest) throws Exception {
        String reqUrl = rpcRequest.toString();
        RpcResponse resp = new RpcResponse();
        resp.setRequestId(rpcRequest.getRequestId());
        Map<String, Map<String, Set<String>>> serviceMap = RpcServerScannerRegistrar.serviceMap;
        Map<String, Object> service = RpcServerScannerRegistrar.service;
        if (serviceMap.get(rpcRequest.getGroup()).get(rpcRequest.getVersion()).contains(reqUrl)) {
            Object bean = service.get(reqUrl);
            if(bean != null) {
                Class<?> clazz = bean.getClass();
                Method method = clazz.getMethod(
                        rpcRequest.getMethodName(),
                        rpcRequest.getParameterTypes()
                );

                Object result = method.invoke(bean, rpcRequest.getArgs());

                ctx.writeAndFlush(RpcResponse.success(rpcRequest.getRequestId(), result));
            }
        }
        ctx.writeAndFlush(RpcResponse.fail(rpcRequest.getRequestId(), "Method Not Found"));

    }
}
