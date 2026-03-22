package org.ktp.client;


import org.ktp.server.Method1;
import org.ktp.server.Method2;
import org.nettyrpc.annotation.client.KettyRpcClass;
import org.nettyrpc.annotation.client.KettyRpcRef;

@KettyRpcClass
public class ClientTemp {
    @KettyRpcRef
    Method1 method1;

    @KettyRpcRef
    Method2 method2;

    public String test(){
        System.out.println(method1.insert());
        System.out.println(method2.method2());
        return "OK";
    }
}
