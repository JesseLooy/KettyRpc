package org.nettyrpc.netty.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.common.utils.KryoSerializer;
import org.common.utils.RpcResponse;

public class RpcResponseEncoder extends MessageToByteEncoder<RpcResponse> {

    @Override
    protected void encode(ChannelHandlerContext ctx, RpcResponse msg, ByteBuf out) throws Exception {
        byte[] body = KryoSerializer.serialize(msg);
        out.writeInt(body.length);
        out.writeBytes(body);
    }
}