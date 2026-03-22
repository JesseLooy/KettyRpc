package org.nettyrpc.test;

import org.nettyrpc.annotation.server.KettyRpcService;

@KettyRpcService
public class C implements A{
    @Override
    public String a(int a, int b) {
        return "C server端提供的服务 调用a方法";
    }

    @Override
    public String b() {
        return "C server端提供的服务 调用b方法";
    }
}
