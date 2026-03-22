package org.nettyrpc.scan.client;

import org.common.constant.Constant;
import org.jspecify.annotations.Nullable;
import org.common.utils.RpcRequest;
import org.common.utils.RpcResponse;
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
    public @Nullable Object getObject() {
        return Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class[] { interfaceClass },
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        // TODO: 请求Netty 拿到返回结果
                        // System.out.println("Netty 发送请求代理，并且等待结果的返回");



                        if(RpcClientScannerRegistrar.channel == null) {
                            synchronized (RpcClientScannerRegistrar.class) {}
                        }
                        rpcRequest.setRequestId(String.valueOf(Constant.requestID.getAndIncrement()));
                        rpcRequest.setArgs(args);
                        rpcRequest.setMethodName(method.getName());
                        rpcRequest.setServiceName(interfaceClass.getName());
                        RpcClientScannerRegistrar.channel.writeAndFlush(rpcRequest);
                        RpcResponse response = null;
                        while(response == null){

                            synchronized (RpcSendRequest.class) {
                                response = RpcSendRequest.responseMap.get(rpcRequest.getRequestId());

                                if(response == null) RpcSendRequest.class.wait();
                            }


                            // rpcRequest.getRequestId()
                        }
                        return response.getData();
                    }
                }
        );
    }

    @Override
    public @Nullable Class<?> getObjectType() {
        return interfaceClass;
    }
}
