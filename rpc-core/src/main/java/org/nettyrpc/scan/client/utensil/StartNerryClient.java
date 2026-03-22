package org.nettyrpc.scan.client.utensil;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import static org.nettyrpc.scan.client.RpcClientScannerRegistrar.startClient;


public class StartNerryClient implements SmartInitializingSingleton, ApplicationContextAware {
    private ApplicationContext context;
    @Override
    public void afterSingletonsInstantiated() {
        startClient();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }
}
