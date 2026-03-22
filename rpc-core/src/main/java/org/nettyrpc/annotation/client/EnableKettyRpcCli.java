package org.nettyrpc.annotation.client;
import org.nettyrpc.scan.client.RpcClientScannerRegistrar;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(RpcClientScannerRegistrar.class)
public @interface EnableKettyRpcCli {
    @AliasFor("value")
    String[] basePackages() default {};

    @AliasFor("basePackages")
    String[] value() default {};;
}