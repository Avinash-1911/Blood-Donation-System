package com.blooddonation.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "donation_events")
public class DonationEvent {

    @Id
    private String id;

    @Indexed
    private String donorId;

    @Indexed
    private String requestId;

    private String donorName;

    private String recipientName;

    private Donor.BloodGroup bloodGroup;

    private int unitsdonated;

    private String hospital;

    private LocalDateTime donationDate;

    private EventStatus status;

    private String notes;

    @CreatedDate
    private LocalDateTime createdAt;

    public enum EventStatus {
        SCHEDULED, COMPLETED, CANCELLED, NO_SHOW
    }
}
