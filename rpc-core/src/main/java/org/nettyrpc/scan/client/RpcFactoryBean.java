package org.nettyrpc.scan.client;

import org.jspecify.annotations.Nullable;
import org.nettyrpc.netty.RpcRequest;
import org.nettyrpc.netty.client.RpcSendRequest;
import org.springframework.beans.factory.FactoryBean;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class RpcFactoryBean implements FactoryBean{
    private Class interfaceClass;
    private RpcRequest rpcRequest;
    public RpcFactoryBean(RpcRequest rpcRequest, Class interfaceClass) {
        this.rpcRequest = rpcRequest;
        this.interfaceClass = interfaceClass;
    }

    @Override
    public @Nullable Object getObject() throws Exception {
        return Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class[] { interfaceClass },
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        // TODO: 请求Netty 拿到返回结果
                        System.out.println("Netty 发送请求代理，并且等待结果的返回");
                        if(RpcClientScannerRegistrar.channel == null) throw new RuntimeException("channel is not ready");
                        rpcRequest.setArgs(args);
                        rpcRequest.setMethodName(method.getName());
                        rpcRequest.setServiceName(interfaceClass.getName());
                        return RpcClientScannerRegistrar.channel.writeAndFlush(rpcRequest);
                    }
                }
        );
    }

    @Override
    public @Nullable Class<?> getObjectType() {
        return interfaceClass;
    }
}
