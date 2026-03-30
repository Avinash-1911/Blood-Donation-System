package com.blooddonation.controller;

import com.blooddonation.dto.ApiResponse;
import com.blooddonation.dto.DonorDTO;
import com.blooddonation.model.Donor;
import com.blooddonation.service.DonorService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/donors")
public class DonorController {

    @Autowired
    private DonorService donorService;

    // ─────────────────────────────
    // Public Endpoints
    // ─────────────────────────────

    /**
     * Register as a blood donor.
     * POST /api/donors/register
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<DonorDTO.Response>> register(
            @Valid @RequestBody DonorDTO.RegisterRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(donorService.register(req), "Donor registered successfully"));
    }

    /**
     * Search donors by location and/or blood group.
     * GET /api/donors/nearby?lat=&lng=&radius=&bloodGroup=&compatible=true
     */
    @GetMapping("/nearby")
    public ResponseEntity<ApiResponse<List<DonorDTO.Response>>> findNearby(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "50") double radius,
            @RequestParam(required = false) Donor.BloodGroup bloodGroup,
            @RequestParam(defaultValue = "true") boolean compatible) {
        List<DonorDTO.Response> donors = donorService.findNearby(lng, lat, radius, bloodGroup, compatible);
        return ResponseEntity.ok(ApiResponse.success(donors,
                donors.size() + " donor(s) found within " + radius + " km"));
    }

    /**
     * Search donors by city.
     * GET /api/donors/search?city=&bloodGroup=
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<DonorDTO.Response>>> search(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Donor.BloodGroup bloodGroup) {
        List<DonorDTO.Response> donors;
        if (city != null) {
            donors = donorService.findByCity(city, bloodGroup);
        } else if (bloodGroup != null) {
            donors = donorService.findNearby(0, 0, 9999, bloodGroup, false);
        } else {
            donors = donorService.findAll();
        }
        return ResponseEntity.ok(ApiResponse.success(donors));
    }

    /**
     * Get donor profile by ID.
     * GET /api/donors/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DONOR') or authentication.name == @donorService.getById(#id).email")
    public ResponseEntity<ApiResponse<DonorDTO.Response>> getById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(donorService.getById(id)));
    }

    /**
     * Get own profile.
     * GET /api/donors/me
     */
    @GetMapping("/me")
    @PreAuthorize("hasRole('DONOR')")
    public ResponseEntity<ApiResponse<DonorDTO.Response>> getMe(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(donorService.getByEmail(auth.getName())));
    }

    /**
     * Update donor profile.
     * PUT /api/donors/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or authentication.name == @donorService.getById(#id).email")
    public ResponseEntity<ApiResponse<DonorDTO.Response>> update(
            @PathVariable String id,
            @RequestBody DonorDTO.UpdateRequest req) {
        return ResponseEntity.ok(ApiResponse.success(donorService.update(id, req), "Profile updated"));
    }

    /**
     * Toggle donor availability.
     * PATCH /api/donors/{id}/availability
     */
    @PatchMapping("/{id}/availability")
    @PreAuthorize("hasRole('ADMIN') or authentication.name == @donorService.getById(#id).email")
    public ResponseEntity<ApiResponse<Void>> setAvailability(
            @PathVariable String id,
            @RequestParam boolean available) {
        donorService.setAvailability(id, available);
        return ResponseEntity.ok(ApiResponse.success(null,
                "Availability set to " + available));
    }

    // ─────────────────────────────
    // Admin Endpoints
    // ─────────────────────────────

    /**
     * Get all donors (admin only).
     * GET /api/donors
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<DonorDTO.Response>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(donorService.findAll()));
    }
}
