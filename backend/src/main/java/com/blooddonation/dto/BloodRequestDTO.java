package com.blooddonation.dto;

import com.blooddonation.model.BloodRequest;
import com.blooddonation.model.Donor;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

public class BloodRequestDTO {

    @Data
    public static class CreateRequest {
        @NotBlank
        private String requesterName;

        @NotBlank @Pattern(regexp = "^\\+?[0-9]{10,15}$")
        private String requesterPhone;

        @Email
        private String requesterEmail;

        @NotNull
        private Donor.BloodGroup bloodGroupNeeded;

        @Min(1) @Max(10)
        private int unitsNeeded;

        @NotNull
        private BloodRequest.Urgency urgency;

        private String patientName;

        @NotBlank
        private String hospital;

        private String hospitalAddress;

        @NotBlank
        private String city;

        private String state;

        /** Longitude */
        @NotNull
        private Double longitude;

        /** Latitude */
        @NotNull
        private Double latitude;

        private String notes;

        private LocalDateTime requiredByDate;

        /** Radius in km for finding donors (default 50) */
        private Double searchRadiusKm;
    }

    @Data
    public static class Response {
        private String id;
        private String requesterName;
        private String requesterPhone;
        private String requesterEmail;
        private Donor.BloodGroup bloodGroupNeeded;
        private int unitsNeeded;
        private BloodRequest.Urgency urgency;
        private String patientName;
        private String hospital;
        private String hospitalAddress;
        private String city;
        private String state;
        private Double longitude;
        private Double latitude;
        private BloodRequest.RequestStatus status;
        private String notes;
        private int notifiedDonorsCount;
        private String acceptedDonorId;
        private LocalDateTime createdAt;
        private LocalDateTime requiredByDate;

        public static Response from(BloodRequest req) {
            Response r = new Response();
            r.id = req.getId();
            r.requesterName = req.getRequesterName();
            r.requesterPhone = req.getRequesterPhone();
            r.requesterEmail = req.getRequesterEmail();
            r.bloodGroupNeeded = req.getBloodGroupNeeded();
            r.unitsNeeded = req.getUnitsNeeded();
            r.urgency = req.getUrgency();
            r.patientName = req.getPatientName();
            r.hospital = req.getHospital();
            r.hospitalAddress = req.getHospitalAddress();
            r.city = req.getCity();
            r.state = req.getState();
            if (req.getLocation() != null) {
                r.longitude = req.getLocation().getX();
                r.latitude = req.getLocation().getY();
            }
            r.status = req.getStatus();
            r.notes = req.getNotes();
            r.notifiedDonorsCount = req.getNotifiedDonorIds() != null ? req.getNotifiedDonorIds().size() : 0;
            r.acceptedDonorId = req.getAcceptedDonorId();
            r.createdAt = req.getCreatedAt();
            r.requiredByDate = req.getRequiredByDate();
            return r;
        }
    }
}
