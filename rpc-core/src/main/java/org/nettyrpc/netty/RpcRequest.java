package org.nettyrpc.netty;


public class RpcRequest {
    private String requestId;
    private String serviceName;   // 接口名
    private String methodName;
    private Class<?>[] parameterTypes;
    private Object[] args;
    private String group;
    private String version;
    public RpcRequest(){}
    public RpcRequest(String serviceName, String methodName, Class<?>[] parameterTypes, Object[] args, String group, String version) {
        this.serviceName = serviceName;
        this.methodName = methodName;
        this.parameterTypes = parameterTypes;
        this.args = args;
        this.group = group;
        this.version = version;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(Class<?>[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

    @Override
    public String toString() {
        String name = serviceName + '#' + methodName + "(";
        for (int i = 0; args != null && i < args.length; i++) {
            if(i != args.length - 1){
                name = name + args[i] + ",";
            }
        }
        return  name + ")";
    }
}