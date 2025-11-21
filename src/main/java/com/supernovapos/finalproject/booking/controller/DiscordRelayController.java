package com.supernovapos.finalproject.booking.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

@RestController
@RequestMapping("/api/discord")
public class DiscordRelayController {

    private final JDA jda;
    private final String channelId = System.getenv("DISCORD_CHANNEL_ID");

    public DiscordRelayController(JDA jda) {
        this.jda = jda;
    }
    

    @PreAuthorize("hasAuthority('DISCORD_SEND')")
    @PostMapping("/send")
    public ResponseEntity<?> sendMessageToDiscord(@RequestBody MessageRequest req) {
        TextChannel channel = jda.getTextChannelById(channelId);
        if (channel == null) {
            return ResponseEntity.badRequest().body("Discord channel not found");
        }
        channel.sendMessage(req.getContent()).queue();
        return ResponseEntity.ok().build();
    }

    public static class MessageRequest {
        private String content;
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }
}


