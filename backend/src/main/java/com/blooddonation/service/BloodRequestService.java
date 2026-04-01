package com.blooddonation.service;

import com.blooddonation.dto.BloodRequestDTO;
import com.blooddonation.dto.DonorDTO;
import com.blooddonation.exception.ResourceNotFoundException;
import com.blooddonation.model.BloodRequest;
import com.blooddonation.model.Donor;
import com.blooddonation.repository.BloodRequestRepository;
import com.blooddonation.repository.DonorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BloodRequestService {

    private static final Logger logger = LoggerFactory.getLogger(BloodRequestService.class);

    @Autowired
    private BloodRequestRepository requestRepository;

    @Autowired
    private DonorRepository donorRepository;

    @Autowired
    private DonorService donorService;

    @Autowired
    private SmsService smsService;

    @Autowired
    private GeocodingService geocodingService;

    @Value("${geo.search.default.radius}")
    private double defaultRadiusKm;

    // ─────────────────────────────
    // Create & Auto-Match
    // ─────────────────────────────

    public BloodRequestDTO.Response createRequest(BloodRequestDTO.CreateRequest req) {
        GeoJsonPoint location = null;
        String geocodedBy;
        String geocodedAddress;
        if (req.getLongitude() != null && req.getLatitude() != null) {
            location = new GeoJsonPoint(req.getLongitude(), req.getLatitude());
            geocodedBy = "FRONTEND_COORDINATES";
            geocodedAddress = String.format("lon=%s,lat=%s", req.getLongitude(), req.getLatitude());
        } else {
            String query = String.join(", ",
                req.getHospitalAddress() != null ? req.getHospitalAddress() : "",
                req.getHospital() != null ? req.getHospital() : "",
                req.getCity() != null ? req.getCity() : "",
                req.getState() != null ? req.getState() : "",
                "India");

            location = geocodingService.geocode(query)
                .orElseThrow(() -> new IllegalArgumentException(
                    "Unable to resolve location. Please provide valid city/state or coordinates."));
            geocodedBy = "OSM_GEOCODE";
            geocodedAddress = query;
        }

        BloodRequest bloodRequest = BloodRequest.builder()
                .requesterName(req.getRequesterName())
                .requesterPhone(req.getRequesterPhone())
                .requesterEmail(req.getRequesterEmail())
                .bloodGroupNeeded(req.getBloodGroupNeeded())
                .unitsNeeded(req.getUnitsNeeded())
                .urgency(req.getUrgency())
                .patientName(req.getPatientName())
                .hospital(req.getHospital())
                .hospitalAddress(req.getHospitalAddress())
                .city(req.getCity())
                .state(req.getState())
                .location(location)
                .geocodedBy(geocodedBy)
                .geocodedAddress(geocodedAddress)
                .status(BloodRequest.RequestStatus.PENDING)
                .notes(req.getNotes())
                .requiredByDate(req.getRequiredByDate())
                .notifiedDonorIds(new ArrayList<>())
                .build();

        BloodRequest saved = requestRepository.save(bloodRequest);

        // Trigger async donor matching and SMS
        double radius = req.getSearchRadiusKm() != null ? req.getSearchRadiusKm() : defaultRadiusKm;
        notifyNearbyDonors(saved, radius);

        return BloodRequestDTO.Response.from(requestRepository.findById(saved.getId()).orElse(saved));
    }

    @Async
    public void notifyNearbyDonors(BloodRequest request, double radiusKm) {
        try {
            // Find compatible donors within radius
            List<DonorDTO.Response> nearbyDonors = donorService.findNearby(
                    request.getLocation().getX(),
                    request.getLocation().getY(),
                    radiusKm,
                    request.getBloodGroupNeeded(),
                    true // include compatible blood groups
            );

            if (nearbyDonors.isEmpty()) {
                logger.info("No nearby donors found for request {}. Expanding search to city.", request.getId());
                // Fallback: city-based search
                List<DonorDTO.Response> cityDonors = donorService.findByCity(
                        request.getCity(), request.getBloodGroupNeeded());
                nearbyDonors = cityDonors;
            }

            List<String> notifiedIds = new ArrayList<>();
            for (DonorDTO.Response donorResp : nearbyDonors) {
                Donor donor = donorRepository.findById(donorResp.getId()).orElse(null);
                if (donor == null) continue;
                boolean sent = smsService.sendDonorAlert(donor, request);
                if (sent) {
                    notifiedIds.add(donor.getId());
                }
            }

            // Update request with notified donors
            BloodRequest updated = requestRepository.findById(request.getId()).orElse(request);
            updated.setNotifiedDonorIds(notifiedIds);
            updated.setStatus(notifiedIds.isEmpty()
                    ? BloodRequest.RequestStatus.PENDING
                    : BloodRequest.RequestStatus.NOTIFIED);
            requestRepository.save(updated);

            // Notify the requester
            smsService.sendRequesterConfirmation(updated, notifiedIds.size());

            logger.info("Notified {} donor(s) for blood request {}", notifiedIds.size(), request.getId());

        } catch (Exception e) {
            logger.error("Error notifying donors for request {}: {}", request.getId(), e.getMessage(), e);
        }
    }

    // ─────────────────────────────
    // Donor Accepts Request
    // ─────────────────────────────

    public BloodRequestDTO.Response acceptRequest(String requestId, String donorId) {
        BloodRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found: " + requestId));

        if (request.getStatus() == BloodRequest.RequestStatus.MATCHED
                || request.getStatus() == BloodRequest.RequestStatus.FULFILLED) {
            throw new IllegalStateException("Request already matched or fulfilled");
        }

        Donor donor = donorRepository.findById(donorId)
                .orElseThrow(() -> new ResourceNotFoundException("Donor not found: " + donorId));

        request.setAcceptedDonorId(donorId);
        request.setStatus(BloodRequest.RequestStatus.MATCHED);
        requestRepository.save(request);

        // SMS notifications
        smsService.sendMatchNotification(request, donor);

        return BloodRequestDTO.Response.from(request);
    }

    // ─────────────────────────────
    // Status & Queries
    // ─────────────────────────────

    public BloodRequestDTO.Response getById(String id) {
        return BloodRequestDTO.Response.from(
                requestRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Request not found: " + id)));
    }

    public List<BloodRequestDTO.Response> getAll() {
        return requestRepository.findAll().stream()
                .map(BloodRequestDTO.Response::from)
                .collect(Collectors.toList());
    }

    public List<BloodRequestDTO.Response> getByStatus(BloodRequest.RequestStatus status) {
        return requestRepository.findByStatus(status).stream()
                .map(BloodRequestDTO.Response::from)
                .collect(Collectors.toList());
    }

    public BloodRequestDTO.Response updateStatus(String id, BloodRequest.RequestStatus newStatus) {
        BloodRequest request = requestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found: " + id));
        request.setStatus(newStatus);
        return BloodRequestDTO.Response.from(requestRepository.save(request));
    }

    public void deleteRequest(String id) {
        if (!requestRepository.existsById(id)) {
            throw new ResourceNotFoundException("Request not found: " + id);
        }
        requestRepository.deleteById(id);
    }

    // ─────────────────────────────
    // Dashboard Stats
    // ─────────────────────────────

    public java.util.Map<String, Long> getStats() {
        return java.util.Map.of(
                "pending", requestRepository.countByStatus(BloodRequest.RequestStatus.PENDING),
                "notified", requestRepository.countByStatus(BloodRequest.RequestStatus.NOTIFIED),
                "matched", requestRepository.countByStatus(BloodRequest.RequestStatus.MATCHED),
                "fulfilled", requestRepository.countByStatus(BloodRequest.RequestStatus.FULFILLED),
                "totalDonors", donorRepository.count(),
                "availableDonors", donorRepository.countByAvailableTrue()
        );
    }
}
