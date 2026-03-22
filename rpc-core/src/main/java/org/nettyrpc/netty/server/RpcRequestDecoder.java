package org.nettyrpc.netty.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.common.utils.KryoSerializer;
import org.common.utils.RpcRequest;

import java.util.List;

public class RpcRequestDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // 长度字段都还没到，先不读
        if (in.readableBytes() < 4) {
            return;
        }

        // 标记当前读位置，防止半包时能回退
        in.markReaderIndex();

        // 先读消息体长度
        int dataLength = in.readInt();

        // 基本校验
        if (dataLength <= 0) {
            ctx.close();
            throw new RuntimeException("illegal data length: " + dataLength);
        }

        // 半包：消息体还没收完整，回退读指针，等待下次数据到达
        if (in.readableBytes() < dataLength) {
            in.resetReaderIndex();
            return;
        }

        // 读取消息体
        byte[] body = new byte[dataLength];
        in.readBytes(body);

        // 反序列化
        RpcRequest request = KryoSerializer.deserialize(body, RpcRequest.class);
        // 传给下一个 handler
        out.add(request);
    }
}