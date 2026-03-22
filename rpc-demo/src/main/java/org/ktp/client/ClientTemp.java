package org.ktp.client;


import org.ktp.server.Method1;
import org.nettyrpc.annotation.client.KettyRpcClass;
import org.nettyrpc.annotation.client.KettyRpcRef;

@KettyRpcClass
public class ClientTemp {
    @KettyRpcRef
    Method1 method1;

    public String test(){
        System.out.println(method1.insert());
        return method1.insert();
    }
}
