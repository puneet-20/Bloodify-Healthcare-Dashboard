package com.bloodify.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bloodify.model.Donor;
import com.bloodify.repository.DonorRepository;

/** Public, read-only. Exposes limited fields only: no phone, email or health data. */
@RestController
@RequestMapping("/api/public")
public class PublicDonorController {

    private static final Set<String> GROUPS = Set.of("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-");
    private static final int MAX_PAGE_SIZE = 20;

    public record PublicDonorView(String displayName, String bloodGroup, String city, boolean available) {
        static PublicDonorView from(Donor d) {
            return new PublicDonorView(shortName(d.getFullName()), d.getBloodGroup(), d.getCity(), d.isAvailable());
        }
    }

    private final DonorRepository donors;

    public PublicDonorController(DonorRepository donors) {
        this.donors = donors;
    }

    @GetMapping("/donors")
    public Map<String, Object> list(@RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "20") int size,
                                    @RequestParam(defaultValue = "") String bloodGroup,
                                    @RequestParam(defaultValue = "") String city) {
        String bg = bloodGroup.trim();
        if (!bg.isEmpty() && !GROUPS.contains(bg)) {
            bg = "?";   // matches nothing instead of erroring
        }
        String c = city.trim();
        if (c.length() > 80) {
            c = c.substring(0, 80);
        }
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);

        Page<Donor> result = donors.searchPublic(bg, c,
                PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt")));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("items", result.getContent().stream().map(PublicDonorView::from).toList());
        body.put("page", result.getNumber());
        body.put("size", result.getSize());
        body.put("totalItems", result.getTotalElements());
        body.put("totalPages", result.getTotalPages());
        return body;
    }

    @GetMapping("/stats")
    public Map<String, Object> stats() {
        Map<String, Long> byGroup = new LinkedHashMap<>();
        for (String g : List.of("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")) {
            byGroup.put(g, 0L);
        }
        long total = 0;
        for (DonorRepository.BloodGroupCount row : donors.countAvailableByBloodGroup()) {
            byGroup.put(row.getBloodGroup(), row.getTotal());
            total += row.getTotal();
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("availableTotal", total);
        body.put("byBloodGroup", byGroup);
        return body;
    }

    /** "Priya Sharma" -> "Priya S." ; single names stay as they are. */
    static String shortName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return "Donor";
        }
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0];
        }
        String last = parts[parts.length - 1];
        return parts[0] + " " + last.substring(0, 1).toUpperCase() + ".";
    }
}