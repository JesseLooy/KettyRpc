package org.ktp.client;

import org.nettyrpc.annotation.client.EnableKettyRpcCli;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;

@EnableKettyRpcCli("org.ktp.client")
@ComponentScan("org.ktp.client")
public class CliConfig {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(CliConfig.class);
        ClientTemp bean = context.getBean(ClientTemp.class);
        System.out.println("bean: " + bean.test());
    }
}
