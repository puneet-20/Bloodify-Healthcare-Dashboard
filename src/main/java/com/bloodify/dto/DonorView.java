package com.bloodify.dto;

import com.bloodify.model.Donor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.Period;
import java.time.ZoneId;

/** Full donor record. Only ever returned to ADMIN/STAFF (contains phone and email). */
public record DonorView(
        Long id,
        String fullName,
        int age,
        String gender,
        String bloodGroup,
        String phone,
        String email,
        String city,
        BigDecimal weightKg,
        boolean hasHealthIssues,
        LocalDate lastDonationDate,
        boolean available,
        boolean eligible,
        OffsetDateTime createdAt,
        String createdBy) {

    public static final int WAIT_DAYS = 56;
    private static final ZoneId ZONE = ZoneId.of("Asia/Kolkata");

    public static LocalDate today() {
        return LocalDate.now(ZONE);
    }

    /** A donor whose last donation is on or before this date has waited long enough. */
    public static LocalDate eligibleCutoff() {
        return today().minusDays(WAIT_DAYS);
    }

    public static DonorView from(Donor d) {
        LocalDate today = today();
        LocalDate cutoff = today.minusDays(WAIT_DAYS);
        boolean waitedLongEnough = d.getLastDonationDate() == null
                || !d.getLastDonationDate().isAfter(cutoff);
        boolean eligible = d.isAvailable() && !d.isHasHealthIssues() && waitedLongEnough;

        return new DonorView(
                d.getId(),
                d.getFullName(),
                Period.between(d.getDateOfBirth(), today).getYears(),
                d.getGender(),
                d.getBloodGroup(),
                d.getPhone(),
                d.getEmail(),
                d.getCity(),
                d.getWeightKg(),
                d.isHasHealthIssues(),
                d.getLastDonationDate(),
                d.isAvailable(),
                eligible,
                d.getCreatedAt(),
                d.getCreatedBy());
    }
}