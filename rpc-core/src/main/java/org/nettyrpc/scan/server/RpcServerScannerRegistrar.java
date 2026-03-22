package org.nettyrpc.scan.server;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.nettyrpc.annotation.server.EnableKettyRpcService;
import org.nettyrpc.annotation.server.KettyRpcService;
import org.nettyrpc.netty.server.RequestHandler;
import org.nettyrpc.netty.server.RpcRequestDecoder;
import org.nettyrpc.netty.server.RpcResponseEncoder;
import org.nettyrpc.scan.server.utensil.RegistryService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
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


/**
 * Server 端里面的注册是 找目标类里面的所有接口的不同方法，注册成服务的
 * 把每一个不同的接口都要注册一个本类的实现，只是名字要按照接口自己的名字。
 * 因为调用服务时候是按照接口名字来进行的查找
 *
 *
 * 最新的可以改成根据类型查找，这样的话只有一个实现类是需要放到一个进入Ioc
 * 容器的。所以只需要放入一次就行了
 *
  */
public class RpcServerScannerRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware,
                                                  ApplicationContextAware {

    private ResourceLoader resourceLoader;
    private ApplicationContext context;
    public static final Map<String, Map<String, Set<String>>> serviceMap = new ConcurrentHashMap<>();
    public static final Map<String, Object> service = new ConcurrentHashMap<>();
    private static final AnnotationBeanNameGenerator GENERATOR =
            new AnnotationBeanNameGenerator();
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata,
                                        BeanDefinitionRegistry registry) {

        // 率先注册了 单例都完成之后的回调
        BeanDefinitionBuilder builder =
                BeanDefinitionBuilder.genericBeanDefinition(RegistryService.class);

        registry.registerBeanDefinition("rpcServerInit", builder.getBeanDefinition());


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
                }
                registry.registerBeanDefinition(GENERATOR.generateBeanName(beanDefinition, registry), beanDefinition);
            }
        }
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }

    public synchronized static void startServer(){
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
                                pl.addLast(new RequestHandler());
                            }
                        });
                bootstrap.bind(8090).sync();
            }catch (Exception e){
                throw new RuntimeException("Unknown Exception -> " + e);
            }

        }).start();
    }
}