package org.nettyrpc.netty.client;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.common.utils.KryoSerializer;
import org.common.utils.RpcRequest;

public class RpcRequestEncoder extends MessageToByteEncoder<RpcRequest> {

    @Override
    protected void encode(ChannelHandlerContext ctx, RpcRequest msg, ByteBuf out) throws Exception {
        byte[] body = KryoSerializer.serialize(msg);
        out.writeInt(body.length);
        out.writeBytes(body);
    }
}