package com.blooddonation.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "donors")
public class Donor {

    @Id
    private String id;

    private String name;

    @Indexed(unique = true)
    private String email;

    @Indexed
    private String phone;

    private String password;

    @Indexed
    private BloodGroup bloodGroup;

    private int age;

    private String gender;

    private String address;

    private String city;

    private String state;

    private String pincode;

    /** GeoJSON point: [longitude, latitude] */
    @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE)
    private GeoJsonPoint location;

    private boolean available;

    private boolean active;

    private LocalDateTime lastDonationDate;

    private List<String> medicalConditions;

    private String profileImage;

    private int totalDonations;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public enum BloodGroup {
        A_POSITIVE("A+"), A_NEGATIVE("A-"),
        B_POSITIVE("B+"), B_NEGATIVE("B-"),
        AB_POSITIVE("AB+"), AB_NEGATIVE("AB-"),
        O_POSITIVE("O+"), O_NEGATIVE("O-");

        private final String label;

        BloodGroup(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }

        public static BloodGroup fromLabel(String label) {
            for (BloodGroup bg : values()) {
                if (bg.label.equalsIgnoreCase(label)) return bg;
            }
            throw new IllegalArgumentException("Unknown blood group: " + label);
        }

        /**
         * Returns blood groups compatible for donation to the given recipient group.
         * (who can RECEIVE from whom)
         */
        public static List<BloodGroup> compatibleDonors(BloodGroup recipient) {
            return switch (recipient) {
                case A_POSITIVE  -> List.of(A_POSITIVE, A_NEGATIVE, O_POSITIVE, O_NEGATIVE);
                case A_NEGATIVE  -> List.of(A_NEGATIVE, O_NEGATIVE);
                case B_POSITIVE  -> List.of(B_POSITIVE, B_NEGATIVE, O_POSITIVE, O_NEGATIVE);
                case B_NEGATIVE  -> List.of(B_NEGATIVE, O_NEGATIVE);
                case AB_POSITIVE -> List.of(A_POSITIVE, A_NEGATIVE, B_POSITIVE, B_NEGATIVE,
                                            AB_POSITIVE, AB_NEGATIVE, O_POSITIVE, O_NEGATIVE);
                case AB_NEGATIVE -> List.of(A_NEGATIVE, B_NEGATIVE, AB_NEGATIVE, O_NEGATIVE);
                case O_POSITIVE  -> List.of(O_POSITIVE, O_NEGATIVE);
                case O_NEGATIVE  -> List.of(O_NEGATIVE);
            };
        }
    }
}
