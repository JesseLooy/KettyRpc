package org.ktp.client;

import org.nettyrpc.annotation.client.EnableKettyRpcCli;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

@EnableKettyRpcCli()
public class CliConfig {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(CliConfig.class);
        System.out.println(context.getType("clientTemp"));
        ClientTemp bean = context.getBean(ClientTemp.class);
        System.out.println("bean: " + bean.test());
    }
}