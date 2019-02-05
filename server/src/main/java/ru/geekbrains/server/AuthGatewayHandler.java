package ru.geekbrains.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import ru.geekbrains.common.AuthRequest;
import ru.geekbrains.common.FilesListMessage;
import ru.geekbrains.common.StatusMessage;
import ru.geekbrains.server.auth.AuthException;
import ru.geekbrains.server.auth.Authentication;

public class AuthGatewayHandler extends ChannelInboundHandlerAdapter {
    private boolean authorized = false;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!authorized) {
            if (msg instanceof AuthRequest) {
                AuthRequest authRequest = (AuthRequest) msg;
                try {
                    Authentication.login(authRequest.getLogin(), authRequest.getPassword());
                    authorized = true;
                    ctx.pipeline().addLast(new MainHandler());
                    ctx.write(new StatusMessage("Успешное подключение к серверу"));
                    ctx.writeAndFlush(new FilesListMessage());
                }
                catch (AuthException e) {
                    ctx.writeAndFlush(new StatusMessage(e.getMessage()));
                }
            }
        }
        else {
            ctx.fireChannelRead(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
