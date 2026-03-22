package org.ktp.server;

import org.nettyrpc.annotation.server.KettyRpcService;

@KettyRpcService
public class ServerTemp implements Method1,Method2 {
    @Override
    public String insert() {
        return "我想什么就是什么insert";
    }

    @Override
    public String method2() {
        return "我 method2";
    }
}
