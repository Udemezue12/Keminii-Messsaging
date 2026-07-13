package com.astrotech.chat.config;

import com.astrotech.chat.configProperties.StompRabbitBrokerProperties;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import io.netty.resolver.dns.DnsServerAddressStreamProviders;
import io.netty.resolver.dns.RoundRobinDnsAddressResolverGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.stomp.StompReactorNettyCodec;
import org.springframework.messaging.tcp.TcpOperations;
import org.springframework.messaging.tcp.reactor.ReactorNettyTcpClient;
import reactor.netty.tcp.TcpClient;

import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ReactorNettyConfig {

    private final RabbitProperties rabbitProperties;
    private final StompRabbitBrokerProperties rabbitBrokerProperties;





    @Bean
    public TcpClient reactorNettyTcpClient() {
        return TcpClient.create()

                .host(rabbitProperties.getHost())
                .port(rabbitBrokerProperties.getRelayPort())

                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_RCVBUF, 1024 * 1024)
                .option(ChannelOption.SO_SNDBUF, 1024 * 1024)

                .doOnConnected(connection -> connection

                        .addHandlerLast(new ReadTimeoutHandler(30, TimeUnit.SECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(30, TimeUnit.SECONDS)))

                .doOnDisconnected(connection -> log.warn("RabbitMQ STOMP connection for TCP disconnected"))



//                .secure(spec -> {
//                    try {
//                        spec.sslContext(
//                                SslContextBuilder.forClient()
//                                        .trustManager(new File("ca.pem"))
//                                        .build());
//                    } catch (SSLException e) {
//                        throw new IllegalStateException("Failed to configure Reactor Netty SSL context", e);
//                    }
//                })
//
                .resolver(new RoundRobinDnsAddressResolverGroup(
                        NioDatagramChannel.class,
                        DnsServerAddressStreamProviders.platformDefault()));
    }
    @Bean
    public StompReactorNettyCodec stompReactorNettyCodec(){
        return new StompReactorNettyCodec();
    }
    @Bean
    public TcpOperations<byte[]> stompTcpClient() {
        return new ReactorNettyTcpClient<>(reactorNettyTcpClient(),stompReactorNettyCodec());
    }
}
