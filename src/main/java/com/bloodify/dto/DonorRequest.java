package com.bloodify.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Payload staff submit from the add-donor form. */
public record DonorRequest(

        @NotBlank(message = "Full name is required")
        @Size(max = 120, message = "Full name must be at most 120 characters")
        String fullName,

        @NotNull(message = "Date of birth is required")
        @Past(message = "Date of birth must be in the past")
        LocalDate dateOfBirth,

        @Pattern(regexp = "^(Male|Female|Other)$", message = "Gender must be Male, Female or Other")
        String gender,

        @NotBlank(message = "Blood group is required")
        @Pattern(regexp = "^(A|B|AB|O)[+-]$", message = "Blood group must be one of A+, A-, B+, B-, AB+, AB-, O+, O-")
        String bloodGroup,

        @NotBlank(message = "Phone is required")
        @Pattern(regexp = "^\\+?[0-9]{7,14}$", message = "Phone must be 7-14 digits, optionally starting with +")
        String phone,

        @Email(message = "Email is not valid")
        @Size(max = 150, message = "Email must be at most 150 characters")
        String email,

        @NotBlank(message = "City is required")
        @Size(max = 80, message = "City must be at most 80 characters")
        String city,

        @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
        @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
        @Digits(integer = 3, fraction = 6, message = "Latitude has too many digits")
        BigDecimal latitude,

        @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
        @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
        @Digits(integer = 3, fraction = 6, message = "Longitude has too many digits")
        BigDecimal longitude,

        @NotNull(message = "Weight is required")
        @DecimalMin(value = "45.0", message = "Weight must be at least 45 kg")
        @Digits(integer = 3, fraction = 2, message = "Weight must look like 62.50")
        BigDecimal weightKg,

        Boolean hasHealthIssues,

        @Size(max = 2000, message = "Health notes must be at most 2000 characters")
        String healthNotes,

        @PastOrPresent(message = "Last donation date cannot be in the future")
        LocalDate lastDonationDate,

        @Size(max = 20, message = "Prediction source must be at most 20 characters")
        String predictionSource,

        @DecimalMin(value = "0.0", message = "Prediction confidence must be between 0 and 1")
        @DecimalMax(value = "1.0", message = "Prediction confidence must be between 0 and 1")
        @Digits(integer = 1, fraction = 3, message = "Prediction confidence must look like 0.953")
        BigDecimal predictionConfidence,

        Boolean available,

        @AssertTrue(message = "Donor consent is required")
        boolean consent) {

    /** Donors must be at least 18. A missing date is reported by @NotNull instead. */
    @JsonIgnore
    @AssertTrue(message = "Donor must be at least 18 years old")
    public boolean isAdult() {
        return dateOfBirth == null || !dateOfBirth.isAfter(DonorView.today().minusYears(18));
    }
}