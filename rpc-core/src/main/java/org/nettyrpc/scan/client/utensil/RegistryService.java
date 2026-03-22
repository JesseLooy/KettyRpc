package org.nettyrpc.scan.client.utensil;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import static org.nettyrpc.scan.client.RpcClientScannerRegistrar.startClient;
import static org.nettyrpc.scan.server.RpcServerScannerRegistrar.*;

public class RegistryService implements SmartInitializingSingleton, ApplicationContextAware {
    private ApplicationContext context;
    @Override
    public void afterSingletonsInstantiated() {
        startServer();
        serviceMap.forEach((s, map) -> {
            map.forEach((k, v) -> {
                v.forEach( method ->{
                    if(service.get(method) == null){
                        service.put(method, context.getBean(method.split("#")[0]));
                    }
                });
            });
        });
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }
}
