package eu.lauwenmark.amanogawa.server.network

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInitializer
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.DelimiterBasedFrameDecoder
import io.netty.handler.codec.Delimiters
import io.netty.handler.codec.string.StringDecoder
import io.netty.handler.codec.string.StringEncoder
import io.netty.handler.logging.LogLevel
import io.netty.handler.logging.LoggingHandler
import io.netty.handler.ssl.SslContext
import io.netty.handler.ssl.SslContextBuilder
import io.netty.handler.ssl.util.SelfSignedCertificate
import mu.KLogging

fun startNetwork(port: Int = 2044, ssl : Boolean = false) {
    val bossGroup = NioEventLoopGroup(1)
    val workerGroup = NioEventLoopGroup(1)
    try {
        val bootstrap = ServerBootstrap()
        val certificate = SelfSignedCertificate()
        val sslContext : SslContext? = if (ssl) SslContextBuilder.forServer(certificate.certificate(), certificate.privateKey()).build() else null

        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel::class.java)
                .handler(LoggingHandler(LogLevel.INFO))
                .childHandler(NetworkServerInitializer(sslContext))
        bootstrap.bind(port).sync().channel().closeFuture().sync()
    } finally {
        bossGroup.shutdownGracefully()
        workerGroup.shutdownGracefully()
    }
}

class NetworkServerInitializer(val sslContext: SslContext?) : ChannelInitializer<SocketChannel?>() {

    private val ENCODER = StringEncoder()
    private val DECODER = StringDecoder()
    private val SERVER_HANDLER = NetworkServerHandler()

    override fun initChannel(ch: SocketChannel?) {
        val pipeline = ch?.pipeline()
        if (sslContext != null) {
            pipeline?.addLast(sslContext.newHandler(ch.alloc()))
        }
        pipeline?.addLast(DelimiterBasedFrameDecoder(8192, *Delimiters.lineDelimiter()))
        pipeline?.addLast(DECODER)
        pipeline?.addLast(ENCODER)
        pipeline?.addLast(SERVER_HANDLER)
    }

}

class NetworkServerHandler() : SimpleChannelInboundHandler<String>() {

    companion object : KLogging()

    override fun channelActive(ctx: ChannelHandlerContext?) {
        ctx?.write("This is Amanogawa 10.1.\r\n")
        ctx?.flush()
    }

    override fun channelRead0(ctx: ChannelHandlerContext?, msg: String?) {
        logger.debug { " Read $msg"}
    }

    override fun channelReadComplete(ctx: ChannelHandlerContext?) {
        ctx?.flush()
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext?, cause: Throwable?) {
        cause?.printStackTrace()
        ctx?.close()
    }
}