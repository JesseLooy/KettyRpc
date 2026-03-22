package org.nettyrpc.scan.server;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.nettyrpc.annotation.client.EnableKettyRpcCli;
import org.nettyrpc.annotation.server.EnableKettyRpcService;
import org.nettyrpc.annotation.server.KettyRpcService;
import org.nettyrpc.netty.server.RpcRequestDecoder;
import org.nettyrpc.netty.server.RpcResponseEncoder;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.beans.factory.config.BeanDefinition;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class RpcServerScannerRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware,
                                                  SmartInitializingSingleton,
                                                  ApplicationContextAware {

    private ResourceLoader resourceLoader;
    private ApplicationContext context;
    public static final Map<String, Map<String, Set<String>>> serviceMap = new ConcurrentHashMap<>();
    public static final Map<String, Object> service = new ConcurrentHashMap<>();

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata,
                                        BeanDefinitionRegistry registry) {

        Map<String, Object> attributes =
                importingClassMetadata.getAnnotationAttributes(EnableKettyRpcService.class.getName());

        String[] basePackages = null;
        if (attributes != null) {
            basePackages = (String[]) attributes.get("basePackages");
        }

        if (basePackages == null || basePackages.length == 0) {
            String className = importingClassMetadata.getClassName();
            String basePackage = className.substring(0, className.lastIndexOf("."));
            basePackages = new String[]{basePackage};
        }

        scanClassAnnotation(basePackages, registry);
    }

    private void scanClassAnnotation(String[] basePackages, BeanDefinitionRegistry registry) {
        // 1. 创建 Spring 提供的类路径扫描器.false 表示不走默认规则
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        // 2. 告诉扫描器：只关心 @RpcService
        scanner.addIncludeFilter(new AnnotationTypeFilter(KettyRpcService.class));

        if (resourceLoader != null) {
            scanner.setResourceLoader(resourceLoader);
        }

        // 3. 扫描指定包
        for (String basePackage : basePackages) {
            Set<BeanDefinition> beanDefinitions = scanner.findCandidateComponents(basePackage);

            for (BeanDefinition beanDefinition : beanDefinitions) {
                String name = beanDefinition.getBeanClassName();
                String group = null, version = null;
                Class<?>[] interfaces = null;
                try {
                    Class<?> aClass = Class.forName(name);
                    KettyRpcService annotation = aClass.getAnnotation(KettyRpcService.class);
                    // 获取group和version
                    group = annotation.group();
                    version = annotation.version();
                    interfaces = aClass.getInterfaces();
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
                // 扫描所有的接口，注册接口里面的方法
                for (Class<?> anInterface : interfaces) {
                    Method[] methods = anInterface.getDeclaredMethods();
                    for (Method method : methods) {
                        name = anInterface.getName();
                        Class<?>[] parameterTypes = method.getParameterTypes();
                        name = name + "#" + method.getName() + "(";
                        for (int i = 0; i < parameterTypes.length; i++) {
                            name +=  parameterTypes[i].getName();
                            if(i != parameterTypes.length - 1) {
                                name += ",";
                            }
                        }
                        name += ")";
                        // TODO: 把服务注册到注册中心
                        // 如果不存在创建并返回在添加进入
                        // System.out.println("TODO: 把后面的服务注册到注册中心，应该是等服务全了在注册进去。Spring的初始化完成回调" + name);
                        boolean add = serviceMap
                                .computeIfAbsent(group, g -> new HashMap<>())
                                .computeIfAbsent(version, v -> new HashSet<>())
                                .add(name);
                        if(add){
                            // 注册进 注册中心，
                            System.out.println("TODO: 把服务注册到注册中心");
                        }

                    }

                    registry.registerBeanDefinition(anInterface.getName(), beanDefinition);
                }

            }
        }
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void afterSingletonsInstantiated() {
        // TODO: 其实在这里也可以进行Bean注册到注册中心，这里是统一注册
        System.out.println("TODO: 其实在这里也可以进行Bean注册到注册中心，这里是统一注册");
        serviceMap.forEach((s, map) -> {
            map.forEach((k, v) -> {
                v.forEach( method ->{
                    if(service.get(method) == null){
                        service.put(method, context.getBean(method));
                    }
                });
            });
        });
        startServer();

    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }

    private void startServer(){
        new Thread(()->{
            EventLoopGroup bossGroup = new MultiThreadIoEventLoopGroup(1, NioIoHandler.newFactory());
            EventLoopGroup workerGroup = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());
            ServerBootstrap bootstrap = new ServerBootstrap();
            try{
                bootstrap.group(bossGroup,workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .childOption(ChannelOption.SO_KEEPALIVE,true)
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel socketChannel) throws Exception {
                                ChannelPipeline pl = socketChannel.pipeline();
                                pl.addLast(new RpcResponseEncoder());
                                pl.addLast(new RpcRequestDecoder());
                            }
                        });
                bootstrap.bind(8090).sync();
            }catch (Exception e){
                throw new RuntimeException("Unknown Exception -> " + e);
            }

        }).start();
    }
}