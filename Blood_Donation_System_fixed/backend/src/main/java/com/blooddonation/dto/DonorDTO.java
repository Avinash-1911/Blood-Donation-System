package com.blooddonation.dto;

import com.blooddonation.model.Donor;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

public class DonorDTO {

    @Data
    public static class RegisterRequest {
        @NotBlank
        private String name;

        @NotBlank @Email
        private String email;

        @NotBlank @Size(min = 6)
        private String password;

        @NotBlank @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone number")
        private String phone;

        @NotNull
        private Donor.BloodGroup bloodGroup;

        @Min(18) @Max(65)
        private int age;

        @NotBlank
        private String gender;

        private String address;

        @NotBlank
        private String city;

        private String state;

        private String pincode;

        /** Longitude */
        private Double longitude;

        /** Latitude */
        private Double latitude;

        private List<String> medicalConditions;
    }

    @Data
    public static class UpdateRequest {
        private String name;
        private String phone;
        private String address;
        private String city;
        private String state;
        private String pincode;
        private Double longitude;
        private Double latitude;
        private Boolean available;
        private List<String> medicalConditions;
        private LocalDateTime lastDonationDate;
    }

    @Data
    public static class Response {
        private String id;
        private String name;
        private String email;
        private String phone;
        private Donor.BloodGroup bloodGroup;
        private int age;
        private String gender;
        private String address;
        private String city;
        private String state;
        private String pincode;
        private Double longitude;
        private Double latitude;
        private boolean available;
        private boolean active;
        private LocalDateTime lastDonationDate;
        private int totalDonations;
        private LocalDateTime createdAt;
        private double distanceKm; // populated in geo searches

        public static Response from(Donor donor) {
            Response r = new Response();
            r.id = donor.getId();
            r.name = donor.getName();
            r.email = donor.getEmail();
            r.phone = donor.getPhone();
            r.bloodGroup = donor.getBloodGroup();
            r.age = donor.getAge();
            r.gender = donor.getGender();
            r.address = donor.getAddress();
            r.city = donor.getCity();
            r.state = donor.getState();
            r.pincode = donor.getPincode();
            if (donor.getLocation() != null) {
                r.longitude = donor.getLocation().getX();
                r.latitude = donor.getLocation().getY();
            }
            r.available = donor.isAvailable();
            r.active = donor.isActive();
            r.lastDonationDate = donor.getLastDonationDate();
            r.totalDonations = donor.getTotalDonations();
            r.createdAt = donor.getCreatedAt();
            return r;
        }
    }
}
