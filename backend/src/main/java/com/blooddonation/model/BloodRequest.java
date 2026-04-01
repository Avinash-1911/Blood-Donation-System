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
@Document(collection = "blood_requests")
public class BloodRequest {

    @Id
    private String id;

    private String requesterName;

    private String requesterPhone;

    private String requesterEmail;

    @Indexed
    private Donor.BloodGroup bloodGroupNeeded;

    private int unitsNeeded;

    private Urgency urgency;

    private String patientName;

    private String hospital;

    private String hospitalAddress;

    private String city;

    private String state;

    /** GeoJSON point: [longitude, latitude] */
    @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE)
    private GeoJsonPoint location;

    /** Audit: FRONTEND_COORDINATES or OSM_GEOCODE */
    private String geocodedBy;

    /** Audit: source address/query used for geocoding */
    private String geocodedAddress;

    @Indexed
    private RequestStatus status;

    private String notes;

    /** Donors who were notified */
    private List<String> notifiedDonorIds;

    /** Donor who accepted */
    private String acceptedDonorId;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private LocalDateTime requiredByDate;

    public enum Urgency {
        CRITICAL, HIGH, MEDIUM, LOW
    }

    public enum RequestStatus {
        PENDING, NOTIFIED, MATCHED, FULFILLED, CANCELLED
    }
}
