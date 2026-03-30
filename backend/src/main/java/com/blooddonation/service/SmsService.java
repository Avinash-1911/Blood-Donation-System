package com.blooddonation.service;

import com.blooddonation.model.BloodRequest;
import com.blooddonation.model.Donor;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SmsService {

    private static final Logger logger = LoggerFactory.getLogger(SmsService.class);

    @Value("${twilio.account.sid}")
    private String accountSid;

    @Value("${twilio.auth.token}")
    private String authToken;

    @Value("${twilio.phone.number}")
    private String fromPhone;

    private boolean twilioEnabled = false;

    @PostConstruct
    public void init() {
        if (!accountSid.startsWith("YOUR_") && !authToken.startsWith("YOUR_")) {
            try {
                Twilio.init(accountSid, authToken);
                twilioEnabled = true;
                logger.info("Twilio SMS service initialized successfully");
            } catch (Exception e) {
                logger.warn("Twilio initialization failed: {}. SMS will be logged only.", e.getMessage());
            }
        } else {
            logger.warn("Twilio credentials not configured. SMS will be logged only.");
        }
    }

    /**
     * Sends SMS alert to a donor about a blood request nearby.
     */
    public boolean sendDonorAlert(Donor donor, BloodRequest request) {
        String message = buildDonorAlertMessage(donor, request);
        return sendSms(donor.getPhone(), message);
    }

    /**
     * Sends a confirmation SMS to the requester.
     */
    public boolean sendRequesterConfirmation(BloodRequest request, int notifiedCount) {
        String message = buildRequesterConfirmationMessage(request, notifiedCount);
        return sendSms(request.getRequesterPhone(), message);
    }

    /**
     * Sends an SMS when a donor accepts a request.
     */
    public boolean sendMatchNotification(BloodRequest request, Donor donor) {
        // Notify requester
        String reqMsg = String.format(
            "🩸 BLOOD DONOR FOUND!\n" +
            "Donor: %s\nBlood Group: %s\nContact: %s\n" +
            "Request ID: %s\nHospital: %s",
            donor.getName(), donor.getBloodGroup().getLabel(),
            donor.getPhone(), request.getId(), request.getHospital()
        );
        sendSms(request.getRequesterPhone(), reqMsg);

        // Notify donor
        String donorMsg = String.format(
            "🩸 DONATION CONFIRMED!\n" +
            "You've been matched with a recipient.\n" +
            "Hospital: %s\nContact: %s\nBlood Group: %s",
            request.getHospital(), request.getRequesterPhone(),
            request.getBloodGroupNeeded().getLabel()
        );
        return sendSms(donor.getPhone(), donorMsg);
    }

    private boolean sendSms(String toPhone, String messageBody) {
        if (!twilioEnabled) {
            logger.info("[SMS LOG] To: {} | Message: {}", toPhone, messageBody);
            return true; // Simulate success in dev mode
        }
        try {
            Message message = Message.creator(
                    new PhoneNumber(toPhone),
                    new PhoneNumber(fromPhone),
                    messageBody
            ).create();
            logger.info("SMS sent to {} - SID: {}", toPhone, message.getSid());
            return true;
        } catch (Exception e) {
            logger.error("Failed to send SMS to {}: {}", toPhone, e.getMessage());
            return false;
        }
    }

    private String buildDonorAlertMessage(Donor donor, BloodRequest request) {
        return String.format(
            "🚨 URGENT BLOOD REQUEST!\n" +
            "Blood Group Needed: %s\n" +
            "Hospital: %s, %s\n" +
            "Urgency: %s\n" +
            "Contact: %s\n" +
            "Request ID: %s\n\n" +
            "Reply ACCEPT to donate. Every second counts!",
            request.getBloodGroupNeeded().getLabel(),
            request.getHospital(), request.getCity(),
            request.getUrgency(),
            request.getRequesterPhone(),
            request.getId()
        );
    }

    private String buildRequesterConfirmationMessage(BloodRequest request, int notifiedCount) {
        return String.format(
            "✅ Blood request received!\n" +
            "Blood Group: %s\n" +
            "Hospital: %s\n" +
            "%d donor(s) notified nearby.\n" +
            "Request ID: %s\n" +
            "We'll notify you when a donor responds.",
            request.getBloodGroupNeeded().getLabel(),
            request.getHospital(),
            notifiedCount,
            request.getId()
        );
    }
}
