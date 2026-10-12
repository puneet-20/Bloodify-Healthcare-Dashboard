package com.bloodify.controller;

import com.bloodify.dto.DonorRequest;
import com.bloodify.dto.DonorView;
import com.bloodify.model.Donor;
import com.bloodify.repository.DonorRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/** Staff API (ADMIN or STAFF). Protected by SecurityConfig: /api/donors/** */
@RestController
@RequestMapping("/api/donors")
public class DonorApiController {

    private final DonorRepository donors;

    public DonorApiController(DonorRepository donors) {
        this.donors = donors;
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody DonorRequest req, Authentication auth) {
        String email = (req.email() == null || req.email().isBlank()) ? null : req.email().trim();
        if (email != null && donors.existsByEmail(email)) {
            return ResponseEntity.status(409).body(Map.of("error", "A donor with this email already exists"));
        }

        Donor d = new Donor();
        d.setFullName(req.fullName().trim());
        d.setDateOfBirth(req.dateOfBirth());
        d.setGender(blankToNull(req.gender()));
        d.setBloodGroup(req.bloodGroup());
        d.setPhone(req.phone().trim());
        d.setEmail(email);
        d.setCity(req.city().trim());
        d.setLatitude(req.latitude());
        d.setLongitude(req.longitude());
        d.setWeightKg(req.weightKg());
        d.setHasHealthIssues(Boolean.TRUE.equals(req.hasHealthIssues()));
        d.setHealthNotes(blankToNull(req.healthNotes()));
        d.setLastDonationDate(req.lastDonationDate());
        String source = blankToNull(req.predictionSource());
        d.setPredictionSource(source != null ? source : "MANUAL");
        d.setPredictionConfidence(req.predictionConfidence());
        d.setAvailable(req.available() == null || req.available());
        d.setCreatedBy(auth.getName());

        Donor saved = donors.save(d);
        return ResponseEntity.status(201).body(DonorView.from(saved));
    }

    @GetMapping
    public Map<String, Object> list(@RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "20") int size,
                                    @RequestParam(defaultValue = "") String bloodGroup,
                                    @RequestParam(defaultValue = "") String city) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        Page<Donor> result = donors.searchForStaff(
                bloodGroup.trim(), city.trim(),
                PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt")));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("items", result.getContent().stream().map(DonorView::from).toList());
        body.put("page", result.getNumber());
        body.put("size", result.getSize());
        body.put("totalItems", result.getTotalElements());
        body.put("totalPages", result.getTotalPages());
        return body;
    }

    @GetMapping("/stats")
    public Map<String, Object> stats() {
        Map<String, Long> byGroup = new LinkedHashMap<>();
        for (DonorRepository.BloodGroupCount c : donors.countAllByBloodGroup()) {
            byGroup.put(c.getBloodGroup(), c.getTotal());
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("total", donors.count());
        body.put("available", donors.countByAvailableTrue());
        body.put("eligible", donors.countEligible(DonorView.eligibleCutoff()));
        body.put("byBloodGroup", byGroup);
        return body;
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}