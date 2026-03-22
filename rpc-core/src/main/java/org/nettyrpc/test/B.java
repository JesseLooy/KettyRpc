package org.nettyrpc.test;

import org.nettyrpc.annotation.client.KettyRpcClass;
import org.nettyrpc.annotation.client.KettyRpcRef;

@KettyRpcClass
public class B{

    @KettyRpcRef
    private A a;


    public String test() {
        System.out.println(a.a(1, 1));
        System.out.println(a.b());
        return "test";
    }
}
