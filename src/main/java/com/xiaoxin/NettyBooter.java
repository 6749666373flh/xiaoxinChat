package com.xiaoxin;

import com.xiaoxin.netty.ChatServer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class NettyBooter implements ApplicationListener<ContextRefreshedEvent> {
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (event.getApplicationContext().getParent() == null) {
            try {
                ChatServer.INSTANCE.start(8888);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
