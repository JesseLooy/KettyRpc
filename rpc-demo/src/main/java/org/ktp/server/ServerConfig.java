package org.ktp.server;

import org.nettyrpc.annotation.server.EnableKettyRpcService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Scanner;

@EnableKettyRpcService
public class ServerConfig {
    public static void main(String[] args) {
        new AnnotationConfigApplicationContext(ServerConfig.class);
        new Scanner(System.in).nextLine();
    }
}
