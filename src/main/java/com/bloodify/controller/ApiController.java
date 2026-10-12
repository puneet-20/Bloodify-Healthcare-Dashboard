package com.bloodify.controller;

import com.bloodify.dto.DonorView;
import com.bloodify.model.Donor;
import com.bloodify.repository.DonorRepository;
import com.bloodify.service.ModelUtilsService;
import com.bloodify.service.PdfReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final DonorRepository donorRepository;
    private final ModelUtilsService modelUtils;
    private final PdfReportService pdfService;

    public ApiController(DonorRepository donorRepository,
                         ModelUtilsService modelUtils,
                         PdfReportService pdfService) {
        this.donorRepository = donorRepository;
        this.modelUtils = modelUtils;
        this.pdfService = pdfService;
    }

    /** Public: fingerprint -> blood group prediction. Touches no donor data. */
    @PostMapping("/predict")
    public ResponseEntity<?> predict(@RequestBody Map<String, String> payload) {
        String base64Image = payload.get("image");
        return ResponseEntity.ok(modelUtils.predictBloodGroup(base64Image));
    }

    /** Staff only (once Step 7 security is on): compatible donors for a receiver. */
    @GetMapping("/donors/compatible")
    public List<DonorView> getCompatibleDonors(@RequestParam String receiverBg,
                                               @RequestParam(defaultValue = "false") boolean emergency,
                                               @RequestParam(required = false) String city) {
        List<String> groups = modelUtils.getCompatibleDonors(receiverBg);
        if (groups.isEmpty()) {
            return List.of();
        }
        boolean hasCity = city != null && !city.isBlank();

        List<Donor> result = new ArrayList<>();
        if (hasCity) {
            result.addAll(donorRepository
                    .findByBloodGroupInAndCityIgnoreCaseAndAvailableTrueOrderByCreatedAtDesc(groups, city.trim()));
        } else if (!emergency) {
            result.addAll(donorRepository
                    .findByBloodGroupInAndAvailableTrueOrderByCreatedAtDesc(groups));
        }

        if (emergency) {
            // Emergency: add universal donors (O-) from anywhere.
            Set<Long> seen = new HashSet<>();
            result.forEach(d -> seen.add(d.getId()));
            for (Donor u : donorRepository
                    .findByBloodGroupInAndAvailableTrueOrderByCreatedAtDesc(List.of("O-"))) {
                if (seen.add(u.getId())) {
                    result.add(u);
                }
            }
        }
        return result.stream().map(DonorView::from).toList();
    }

    /** Staff only (once Step 7 security is on): donor PDF report. */
    @GetMapping("/donors/{id}/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id) {
        return donorRepository.findById(id)
                .map(donor -> {
                    byte[] pdfBytes = pdfService.generateDonorReport(donor);
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_PDF);
                    headers.setContentDispositionFormData("attachment", "donor_report_" + id + ".pdf");
                    return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
                })
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}