package com.bloodify.controller;

import com.bloodify.model.Donor;
import com.bloodify.repository.DonorRepository;
import com.bloodify.service.ModelUtilsService;
import com.bloodify.service.PdfReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class ApiController {

    @Autowired
    private DonorRepository donorRepository;

    @Autowired
    private ModelUtilsService modelUtils;

    @Autowired
    private PdfReportService pdfService;

    @PostMapping("/predict")
    public ResponseEntity<?> predict(@RequestBody Map<String, String> payload) {
        String base64Image = payload.get("image");
        Map<String, Object> prediction = modelUtils.predictBloodGroup(base64Image);
        return ResponseEntity.ok(prediction);
    }

    @PostMapping("/donor")
    public ResponseEntity<?> addDonor(@RequestBody Donor donor) {
        if(donor.getAge() < 16 || donor.getAge() > 80) {
            return ResponseEntity.badRequest().body(Map.of("error", "Age must be between 16 and 80"));
        }
        Donor saved = donorRepository.save(donor);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/donors")
    public List<Donor> getAllDonors() {
        return donorRepository.findAllByOrderByTimestampDesc();
    }

    @GetMapping("/donors/compatible")
    public List<Donor> getCompatibleDonors(@RequestParam String receiverBg,
                                           @RequestParam(required = false, defaultValue = "false") boolean emergency,
                                           @RequestParam(required = false) String city) {
        if(emergency) {
            // Emergency: Closest compatible donors (matching city) + Universal Donors (O-)
            List<String> compatibleGroups = modelUtils.getCompatibleDonors(receiverBg);
            List<Donor> allMatches = new ArrayList<>();
            if (city != null && !city.isBlank()) {
                allMatches.addAll(donorRepository.findByBloodGroupInAndCityOrderByTimestampDesc(compatibleGroups, city));
            }
            List<Donor> universal = donorRepository.findByBloodGroupInOrderByTimestampDesc(Collections.singletonList("O-"));
            
            for(Donor u : universal) {
                if(allMatches.stream().noneMatch(d -> d.getId().equals(u.getId()))) {
                    allMatches.add(u);
                }
            }
            return allMatches;
        } else {
            List<String> compatibleGroups = modelUtils.getCompatibleDonors(receiverBg);
            if (city != null && !city.isBlank()) {
                return donorRepository.findByBloodGroupInAndCityOrderByTimestampDesc(compatibleGroups, city);
            }
            return donorRepository.findByBloodGroupInOrderByTimestampDesc(compatibleGroups);
        }
    }

    @GetMapping("/donor/{id}/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id) {
        Optional<Donor> opt = donorRepository.findById(id);
        if(opt.isPresent()) {
            byte[] pdfBytes = pdfService.generateDonorReport(opt.get());
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "donor_report_" + id + ".pdf");
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
