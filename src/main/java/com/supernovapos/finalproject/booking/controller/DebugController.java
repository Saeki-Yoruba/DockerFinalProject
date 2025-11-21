package com.supernovapos.finalproject.booking.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/discord")
public class DebugController {
  private final SimpMessagingTemplate simp;
  public DebugController(SimpMessagingTemplate simp) { this.simp = simp; }
  
  @PreAuthorize("hasAuthority('DISCORD_DEBUG')")
  @PostMapping("/debug/sendTest")
  public ResponseEntity<?> sendTest() {
    simp.convertAndSend("/topic/booking", Map.of("msg","hello from server"));
    return ResponseEntity.ok().build();
  }
}

