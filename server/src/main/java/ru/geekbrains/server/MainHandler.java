package ru.geekbrains.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import ru.geekbrains.common.*;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class MainHandler extends ChannelInboundHandlerAdapter {
    private static final String SERVER_FOLDER = "server_storage/";
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (msg == null) {
                return;
            }
            if (msg instanceof FileRequest) {
                FileRequest fr = (FileRequest) msg;
                if (Files.exists(Paths.get(SERVER_FOLDER + fr.getFilename()))) {
                    FileMessage fm = new FileMessage(Paths.get(SERVER_FOLDER + fr.getFilename()));
                    ctx.writeAndFlush(fm);
                }
            }
            else if (msg instanceof FileMessage) {
                FileMessage fm = (FileMessage) msg;
                Files.write(Paths.get(SERVER_FOLDER + fm.getFilename()), fm.getData(), StandardOpenOption.CREATE);
                ctx.writeAndFlush(new FilesListMessage());
            }
            else if (msg instanceof FilesListRequest) {
                ctx.writeAndFlush(new FilesListMessage());
            }
            else if (msg instanceof DeleteFileRequest) {
                DeleteFileRequest df = (DeleteFileRequest) msg;
                Files.delete(Paths.get(SERVER_FOLDER +df.getFilename()));
                ctx.writeAndFlush(new DeleteFileMessage());
            }
            else if (msg instanceof RenameFileRequest) {
                RenameFileRequest rf = (RenameFileRequest) msg;
                System.out.println(((RenameFileRequest) msg).getNewFileName());
                Files.move(Paths.get(SERVER_FOLDER + rf.getOldFileName()),
                        Paths.get(SERVER_FOLDER + rf.getNewFileName()));
                ctx.writeAndFlush(new FilesListMessage());
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
