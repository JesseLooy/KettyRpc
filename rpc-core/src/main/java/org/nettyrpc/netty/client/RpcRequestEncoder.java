package org.nettyrpc.netty.client;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.nettyrpc.netty.KryoSerializer;
import org.nettyrpc.netty.RpcRequest;

public class RpcRequestEncoder extends MessageToByteEncoder<RpcRequest> {

    @Override
    protected void encode(ChannelHandlerContext ctx, RpcRequest msg, ByteBuf out) throws Exception {
        byte[] body = KryoSerializer.serialize(msg);
        out.writeInt(body.length);
        out.writeBytes(body);
    }
}