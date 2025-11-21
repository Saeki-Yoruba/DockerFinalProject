package com.supernovapos.finalproject.booking.config;

import java.util.EnumSet;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.supernovapos.finalproject.booking.listener.DiscordListener;

import jakarta.annotation.PreDestroy;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;

@Configuration
public class JdaConfig {

    @Value("${DISCORD_BOT_TOKEN}")
    private String token;

    @Bean
    public JDA jda(DiscordListener discordListener) throws Exception {
        JDABuilder builder = JDABuilder.createDefault(token,
                EnumSet.of(
                    GatewayIntent.GUILD_MESSAGES,
                    GatewayIntent.DIRECT_MESSAGES,
                    GatewayIntent.MESSAGE_CONTENT
                )
        );

        builder.setActivity(Activity.playing("Listening to channel"));
        builder.addEventListeners(discordListener);

        JDA jda = builder.build();
        // 等待 JDA 完成啟動（可選）
        jda.awaitReady();

        return jda;
    }

    @PreDestroy
    public void shutdown() {
        try {
            // Spring 關閉時 JDA 會自動關閉（如果需要可 inject JDA 進此類別）
        } catch (Exception ignored) {}
    }
}

