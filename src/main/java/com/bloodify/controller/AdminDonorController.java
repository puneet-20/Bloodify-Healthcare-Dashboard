package com.bloodify.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bloodify.repository.DonorRepository;

/** Admin only (SecurityConfig: /api/admin/** = ADMIN). Permanently deletes a donor record. */
@RestController
@RequestMapping("/api/admin/donors")
public class AdminDonorController {

    private final DonorRepository donors;

    public AdminDonorController(DonorRepository donors) {
        this.donors = donors;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        if (!donors.existsById(id)) {
            return ResponseEntity.status(404).body(Map.of("error", "Donor not found"));
        }
        donors.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}