package com.yushan.user_service.util;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.MailParseException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.UnsupportedEncodingException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * MailUtil unit tests
 */
public class MailUtilTest {

    private MailUtil mailUtil;

    @Mock
    private JavaMailSender javaMailSender;

    @Mock
    private MimeMessage mimeMessage;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mailUtil = new MailUtil();
        ReflectionTestUtils.setField(mailUtil, "javaMailSender", javaMailSender);
        ReflectionTestUtils.setField(mailUtil, "EMAIL_FROM", "test@example.com");
    }

    /**
     * normal case
     */
    @Test
    void testSendEmail_Success() throws MessagingException, UnsupportedEncodingException {
        // Given
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        // When
        assertDoesNotThrow(() -> mailUtil.sendEmail("recipient@example.com", "Test Subject", "<p>Hello</p>"));

        // Then
        verify(javaMailSender).createMimeMessage();
        verify(javaMailSender).send(any(MimeMessage.class));
    }

    /**
     * throw exception when createMimeMessage
     */
    @Test
    void testSendEmail_CreateMimeMessageThrowsException_ShouldThrowMessagingException() throws MessagingException {
        // Given
        when(javaMailSender.createMimeMessage()).thenThrow(new MailParseException("Create failed"));

        // When & Then
        MessagingException exception = assertThrows(MessagingException.class, () ->
                mailUtil.sendEmail("recipient@example.com", "Test Subject", "<p>Hello</p>")
        );

        assertTrue(exception.getMessage().contains("Failed to send email"));
        verify(javaMailSender).createMimeMessage();
        verify(javaMailSender, never()).send(any(MimeMessage.class));
    }

    /**
     * throw exception when send
     */
    @Test
    void testSendEmail_SendThrowsException_ShouldThrowMessagingException() throws MessagingException, UnsupportedEncodingException {
        // Given
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new RuntimeException("Send failed")).when(javaMailSender).send(any(MimeMessage.class));

        // When & Then
        MessagingException exception = assertThrows(MessagingException.class, () ->
                mailUtil.sendEmail("recipient@example.com", "Test Subject", "<p>Hello</p>")
        );

        assertTrue(exception.getMessage().contains("Failed to send email"));
        verify(javaMailSender).createMimeMessage();
        verify(javaMailSender).send(any(MimeMessage.class));
    }
}
