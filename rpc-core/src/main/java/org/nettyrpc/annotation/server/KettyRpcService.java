package org.nettyrpc.annotation.server;

import java.lang.annotation.*;

@Documented
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value={ElementType.TYPE})
@Inherited
public @interface KettyRpcService {
    String group() default "";
    String version() default "";
}
