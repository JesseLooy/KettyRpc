package org.nettyrpc.test;

import io.netty.channel.Channel;
import org.nettyrpc.annotation.client.KettyRpcClass;
import org.nettyrpc.annotation.client.KettyRpcRef;
import org.nettyrpc.scan.client.RpcClientScannerRegistrar;

@KettyRpcClass
public class B{

    @KettyRpcRef
    private A a;


    public String test() {
        System.out.println(a.a(1, 1));
        System.out.println(a.b());
        return "test";
    }

    private static Channel channel1;
    public static void main(String[] args) {
        if(RpcClientScannerRegistrar.channel == null) {
            synchronized (RpcClientScannerRegistrar.class) {
                System.out.println("等待创建完毕");
            }
        }
        System.out.println("123");
        System.out.println();
    }
}
