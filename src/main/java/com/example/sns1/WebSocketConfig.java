package com.example.sns1;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import com.example.sns1.jwt.StompHandler;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.config.ChannelRegistration;

@RequiredArgsConstructor
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    private final StompHandler stompHandler;
    
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 안드로이드 앱용
        registry.addEndpoint("/ws-stomp")
                .setAllowedOriginPatterns("*");

        // 웹 브라우저용 (SockJS 사용)
        registry.addEndpoint("/ws-stomp-web") 
                .setAllowedOriginPatterns("*")
                .withSockJS(); 
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/sub");
        //registry.setApplicationDestinationPrefixes("/pub");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompHandler);
    }
}
