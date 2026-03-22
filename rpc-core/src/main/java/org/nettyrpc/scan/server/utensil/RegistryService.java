package org.nettyrpc.scan.server.utensil;

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
                        Class clazz = null;
                        try {
                            clazz = Class.forName(method.split("#")[0]);
                        } catch (ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                        service.put(method, context.getBean(clazz));
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
