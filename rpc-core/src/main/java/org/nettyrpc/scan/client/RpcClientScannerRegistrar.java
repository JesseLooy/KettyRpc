package org.nettyrpc.scan.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.nettyrpc.annotation.client.EnableKettyRpcCli;
import org.nettyrpc.annotation.client.KettyRpcClass;
import org.nettyrpc.annotation.client.KettyRpcRef;
import org.nettyrpc.netty.RpcRequest;
import org.nettyrpc.netty.client.RpcRequestEncoder;
import org.nettyrpc.netty.client.RpcResponseDecoder;
import org.nettyrpc.netty.client.RpcSendRequest;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@EnableKettyRpcCli
public class RpcClientScannerRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware {

    private ResourceLoader resourceLoader;
    public static Channel channel;
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata,
                                        BeanDefinitionRegistry registry) {

        Map<String, Object> attributes =
                importingClassMetadata.getAnnotationAttributes(EnableKettyRpcCli.class.getName());

        String[] basePackages = null;
        if (attributes != null) {
            basePackages = (String[]) attributes.get("basePackages");
        }

        if (basePackages == null || basePackages.length == 0) {
            String className = importingClassMetadata.getClassName();
            String basePackage = className.substring(0, className.lastIndexOf("."));
            basePackages = new String[]{basePackage};
        }

        // 1. 创建 Spring 提供的类路径扫描器
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);

        // 2. 告诉扫描器：只关心 @RpcService
        scanner.addIncludeFilter(new AnnotationTypeFilter(KettyRpcClass.class));

        if (resourceLoader != null) {
            scanner.setResourceLoader(resourceLoader);
        }


        Set<String> interfaceName = new HashSet<String>();
        // 3. 扫描指定包
        for (String basePackage : basePackages) {
            Set<BeanDefinition> beanDefinitions = scanner.findCandidateComponents(basePackage);

            for (BeanDefinition beanDefinition : beanDefinitions) {
                String className = beanDefinition.getBeanClassName();

                String group = null, version = null;
                Class<?> clazz = null;
                try {
                    clazz = Class.forName(className);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
                Field[] fields = clazz.getDeclaredFields();

                for (Field field : fields) {
                    field.setAccessible(true);
                    if(field.isAnnotationPresent(KettyRpcRef.class)){
                        KettyRpcRef annotation = field.getAnnotation(KettyRpcRef.class);
                        // 获取group和version
                        group = annotation.group();
                        version = annotation.version();
                        // 必须是接口类型的，并且没有被添加进入过的 直接通过代理 注册到Spring 容器里面
                        if (field.getType().isInterface() && interfaceName.add(field.getType().getName())) {
                            RpcRequest rpcRequest = new RpcRequest("","",null,null,group,version);

                            beanDefinition.getConstructorArgumentValues().addGenericArgumentValue(field.getType());
                            beanDefinition.getConstructorArgumentValues().addGenericArgumentValue(rpcRequest);

                            beanDefinition.setBeanClassName(RpcFactoryBean.class.getName());
                            registry.registerBeanDefinition(field.getType().getName(), beanDefinition);
                        }
                    }
                }
            }
        }
        // 这里先开启一次服务，免得后面没有开启
        startClient();
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public synchronized static void startClient(){
        if(channel != null) return;
        EventLoopGroup group = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());
        Bootstrap bootstrap = new Bootstrap();
        try{
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE,true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pl = socketChannel.pipeline();
                            pl.addLast(new RpcRequestEncoder());
                            pl.addLast(new RpcResponseDecoder());
                            pl.addLast(new RpcSendRequest());
                        }
                    });
            // TODO: 这里端口和 服务器地址要从注册中心获取
            // System.out.println("TODO: 这里端口和 服务器地址要从注册中心获取");
            ChannelFuture future = bootstrap.connect("127.0.0.1", 8090).sync();
            channel = future.channel();
        }catch (Exception e){
            throw new RuntimeException("NettyRpc Server is not running. Please start server firstly -> ");
        }
    }

}