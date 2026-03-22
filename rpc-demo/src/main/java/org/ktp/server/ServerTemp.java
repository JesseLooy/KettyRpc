package org.ktp.server;

import org.nettyrpc.annotation.server.KettyRpcService;

@KettyRpcService
public class ServerTemp implements Method1 {
    @Override
    public String insert() {
        return "ServerTemp:Method1:insert()";
    }
}
