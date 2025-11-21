package com.supernovapos.finalproject.booking.listener;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.supernovapos.finalproject.booking.dto.DiscordMessageDto;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

@Component
public class DiscordListener extends ListenerAdapter {
	 private final SimpMessagingTemplate messagingTemplate;
	    private final String watchChannelId = System.getenv("DISCORD_CHANNEL_ID");

	    public DiscordListener(SimpMessagingTemplate messagingTemplate) {
	        this.messagingTemplate = messagingTemplate;
	    }

	    @Override
	    public void onMessageReceived(MessageReceivedEvent event) {
	        if (event.getAuthor().isBot()) return;
	        if (!event.getChannel().getId().equals(watchChannelId)) return;

	        DiscordMessageDto dto = new DiscordMessageDto(
	            event.getAuthor().getName(),
	            event.getMessage().getContentDisplay(),
	            event.getMessage().getTimeCreated().toString()
	        );

	        // 將訊息推到 WebSocket topic（/topic/discord）
	        messagingTemplate.convertAndSend("/topic/discord", dto);
	    }

}

