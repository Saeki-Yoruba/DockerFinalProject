package com.supernovapos.finalproject.auth.service;

public interface EmailService {

	void sendVerificationEmail(String to, String token);

}