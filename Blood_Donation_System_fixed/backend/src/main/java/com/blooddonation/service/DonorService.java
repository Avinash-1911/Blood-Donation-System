package com.blooddonation.service;

import com.blooddonation.dto.DonorDTO;
import com.blooddonation.exception.ResourceNotFoundException;
import com.blooddonation.model.Donor;
import com.blooddonation.repository.DonorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.NearQuery;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DonorService {

    @Autowired
    private DonorRepository donorRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private GeocodingService geocodingService;

    // ─────────────────────────────
    // Registration
    // ─────────────────────────────

    public DonorDTO.Response register(DonorDTO.RegisterRequest req) {
        if (donorRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }
        if (donorRepository.existsByPhone(req.getPhone())) {
            throw new IllegalArgumentException("Phone number already registered");
        }

        Donor.DonorBuilder builder = Donor.builder()
                .name(req.getName())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .phone(req.getPhone())
                .bloodGroup(req.getBloodGroup())
                .age(req.getAge())
                .gender(req.getGender())
                .address(req.getAddress())
                .city(req.getCity())
                .state(req.getState())
                .pincode(req.getPincode())
                .available(true)
                .active(true)
                .totalDonations(0)
                .medicalConditions(req.getMedicalConditions() != null ? req.getMedicalConditions() : new ArrayList<>());

        if (req.getLongitude() != null && req.getLatitude() != null) {
            builder.location(new GeoJsonPoint(req.getLongitude(), req.getLatitude()));
        } else {
            String query = String.join(", ",
                    req.getAddress() != null ? req.getAddress() : "",
                    req.getCity() != null ? req.getCity() : "",
                    req.getState() != null ? req.getState() : "",
                    req.getPincode() != null ? req.getPincode() : "",
                    "India");

            geocodingService.geocode(query)
                    .ifPresent(builder::location);
        }

        Donor saved = donorRepository.save(builder.build());
        return DonorDTO.Response.from(saved);
    }

    // ─────────────────────────────
    // Profile Management
    // ─────────────────────────────

    public DonorDTO.Response getById(String id) {
        Donor donor = donorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Donor not found: " + id));
        return DonorDTO.Response.from(donor);
    }

    public DonorDTO.Response getByEmail(String email) {
        Donor donor = donorRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Donor not found: " + email));
        return DonorDTO.Response.from(donor);
    }

    public DonorDTO.Response update(String id, DonorDTO.UpdateRequest req) {
        Donor donor = donorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Donor not found: " + id));

        if (req.getName() != null) donor.setName(req.getName());
        if (req.getPhone() != null) donor.setPhone(req.getPhone());
        if (req.getAddress() != null) donor.setAddress(req.getAddress());
        if (req.getCity() != null) donor.setCity(req.getCity());
        if (req.getState() != null) donor.setState(req.getState());
        if (req.getPincode() != null) donor.setPincode(req.getPincode());
        if (req.getAvailable() != null) donor.setAvailable(req.getAvailable());
        if (req.getMedicalConditions() != null) donor.setMedicalConditions(req.getMedicalConditions());
        if (req.getLastDonationDate() != null) donor.setLastDonationDate(req.getLastDonationDate());

        if (req.getLongitude() != null && req.getLatitude() != null) {
            donor.setLocation(new GeoJsonPoint(req.getLongitude(), req.getLatitude()));
        }

        return DonorDTO.Response.from(donorRepository.save(donor));
    }

    public void setAvailability(String id, boolean available) {
        Donor donor = donorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Donor not found: " + id));
        donor.setAvailable(available);
        donorRepository.save(donor);
    }

    // ─────────────────────────────
    // Geo-Spatial Donor Search
    // ─────────────────────────────

    public List<DonorDTO.Response> findNearby(double longitude,
                                              double latitude,
                                              double radiusKm,
                                              Donor.BloodGroup bloodGroup,
                                              boolean compatible) {

        GeoJsonPoint centre = new GeoJsonPoint(longitude, latitude);
        double radiusMetres = radiusKm * 1000;

        Criteria criteria = Criteria.where("available").is(true)
                .and("active").is(true);

        if (bloodGroup != null) {
            if (compatible) {
                List<Donor.BloodGroup> compatibleGroups =
                        Donor.BloodGroup.compatibleDonors(bloodGroup);
                criteria.and("bloodGroup").in(compatibleGroups);
            } else {
                criteria.and("bloodGroup").is(bloodGroup);
            }
        }

        NearQuery nearQuery = NearQuery.near(centre)
                .maxDistance(radiusMetres)   // distance in meters
                .spherical(true)
                .query(Query.query(criteria))
                .limit(100);   // ✅ FIXED (was .num(100))

        var geoResults = mongoTemplate.geoNear(nearQuery, Donor.class);

        return geoResults.getContent().stream().map(geoResult -> {
            DonorDTO.Response r = DonorDTO.Response.from(geoResult.getContent());
            r.setDistanceKm(geoResult.getDistance().getValue() / 1000); // meters → km
            return r;
        }).collect(Collectors.toList());
    }

    // ─────────────────────────────
    // City-based search
    // ─────────────────────────────

    public List<DonorDTO.Response> findByCity(String city,
                                              Donor.BloodGroup bloodGroup) {

        List<Donor> donors;

        if (bloodGroup != null) {
            donors = donorRepository
                    .findByCityAndBloodGroupAndAvailableTrue(city, bloodGroup);
        } else {
            donors = donorRepository.findByCity(city);
        }

        return donors.stream()
                .map(DonorDTO.Response::from)
                .collect(Collectors.toList());
    }

    public List<DonorDTO.Response> findAll() {
        return donorRepository.findAll().stream()
                .map(DonorDTO.Response::from)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────
    // Donation Count
    // ─────────────────────────────

    public void incrementDonationCount(String donorId) {
        Donor donor = donorRepository.findById(donorId)
                .orElseThrow(() -> new ResourceNotFoundException("Donor not found: " + donorId));

        donor.setTotalDonations(donor.getTotalDonations() + 1);
        donorRepository.save(donor);
    }
}
