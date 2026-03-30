package com.blooddonation.controller;

import com.blooddonation.dto.ApiResponse;
import com.blooddonation.dto.BloodRequestDTO;
import com.blooddonation.model.BloodRequest;
import com.blooddonation.service.BloodRequestService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/requests")
public class BloodRequestController {

    @Autowired
    private BloodRequestService requestService;

    // ─────────────────────────────
    // Public Endpoints
    // ─────────────────────────────

    /**
     * Submit a new blood request (no auth required — emergency use).
     * POST /api/requests/create
     */
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<BloodRequestDTO.Response>> create(
            @Valid @RequestBody BloodRequestDTO.CreateRequest req) {
        BloodRequestDTO.Response response = requestService.createRequest(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response,
                        "Blood request submitted. Nearby donors are being notified via SMS."));
    }

    /**
     * Get a single request by ID (public, for status tracking).
     * GET /api/requests/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BloodRequestDTO.Response>> getById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(requestService.getById(id)));
    }

    // ─────────────────────────────
    // Authenticated Endpoints
    // ─────────────────────────────

    /**
     * Donor accepts a blood request.
     * POST /api/requests/{id}/accept
     */
    @PostMapping("/{id}/accept")
    @PreAuthorize("hasRole('DONOR')")
    public ResponseEntity<ApiResponse<BloodRequestDTO.Response>> accept(
            @PathVariable String id,
            @RequestParam String donorId,
            Authentication auth) {
        BloodRequestDTO.Response response = requestService.acceptRequest(id, donorId);
        return ResponseEntity.ok(ApiResponse.success(response, "Request accepted! Both parties have been notified via SMS."));
    }

    /**
     * Update request status.
     * PATCH /api/requests/{id}/status
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BloodRequestDTO.Response>> updateStatus(
            @PathVariable String id,
            @RequestParam BloodRequest.RequestStatus status) {
        return ResponseEntity.ok(ApiResponse.success(
                requestService.updateStatus(id, status), "Status updated"));
    }

    // ─────────────────────────────
    // Admin Endpoints
    // ─────────────────────────────

    /**
     * Get all blood requests.
     * GET /api/requests
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<BloodRequestDTO.Response>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(requestService.getAll()));
    }

    /**
     * Filter requests by status.
     * GET /api/requests?status=PENDING
     */
    @GetMapping("/filter")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<BloodRequestDTO.Response>>> getByStatus(
            @RequestParam BloodRequest.RequestStatus status) {
        return ResponseEntity.ok(ApiResponse.success(requestService.getByStatus(status)));
    }

    /**
     * Dashboard stats.
     * GET /api/requests/stats
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getStats() {
        return ResponseEntity.ok(ApiResponse.success(requestService.getStats(), "Stats fetched"));
    }

    /**
     * Delete a request (admin only).
     * DELETE /api/requests/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        requestService.deleteRequest(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Request deleted"));
    }
}
