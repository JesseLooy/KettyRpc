package org.nettyrpc;

import org.nettyrpc.annotation.client.EnableKettyRpcCli;
import org.nettyrpc.annotation.server.EnableKettyRpcService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableKettyRpcCli
@EnableKettyRpcService
@ComponentScan("org.nettyrpc")
public class App {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(App.class);

    }
}