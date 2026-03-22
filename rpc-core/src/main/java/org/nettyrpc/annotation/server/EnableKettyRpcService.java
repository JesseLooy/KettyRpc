package org.nettyrpc.annotation.server;

import org.nettyrpc.scan.server.RpcServerScannerRegistrar;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(RpcServerScannerRegistrar.class)
public @interface EnableKettyRpcService {
    @AliasFor("value")
    String[] basePackages() default {};

    @AliasFor("basePackages")
    String[] value() default {};;
}
