package com.bloodify.repository;

import com.bloodify.model.Donor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DonorRepository extends JpaRepository<Donor, Long> {

    interface BloodGroupCount {
        String getBloodGroup();
        Long getTotal();
    }

    // NOTE: the filter parameters below must be '' (never null) when "no filter".
    // Hibernate + PostgreSQL cannot type a null parameter in "(:x = '' or ...)".

    /** Staff search: every donor, optional blood group / city filters. */
    @Query("""
            select d from Donor d
            where (:bloodGroup = '' or d.bloodGroup = :bloodGroup)
              and (:city = '' or lower(d.city) = lower(:city))
            """)
    Page<Donor> searchForStaff(@Param("bloodGroup") String bloodGroup,
                               @Param("city") String city,
                               Pageable pageable);

    /** Public search: only donors marked available. */
    @Query("""
            select d from Donor d
            where d.available = true
              and (:bloodGroup = '' or d.bloodGroup = :bloodGroup)
              and (:city = '' or lower(d.city) = lower(:city))
            """)
    Page<Donor> searchPublic(@Param("bloodGroup") String bloodGroup,
                             @Param("city") String city,
                             Pageable pageable);

    @Query("""
            select d.bloodGroup as bloodGroup, count(d) as total
            from Donor d where d.available = true
            group by d.bloodGroup
            """)
    List<BloodGroupCount> countAvailableByBloodGroup();

    @Query("""
            select d.bloodGroup as bloodGroup, count(d) as total
            from Donor d
            group by d.bloodGroup
            """)
    List<BloodGroupCount> countAllByBloodGroup();

    long countByAvailableTrue();

    @Query("""
            select count(d) from Donor d
            where d.available = true
              and d.hasHealthIssues = false
              and (d.lastDonationDate is null or d.lastDonationDate <= :cutoff)
            """)
    long countEligible(@Param("cutoff") LocalDate cutoff);

    boolean existsByEmail(String email);

    // Compatible-donor search (staff). Only donors marked available.
    List<Donor> findByBloodGroupInAndAvailableTrueOrderByCreatedAtDesc(List<String> bloodGroups);

    List<Donor> findByBloodGroupInAndCityIgnoreCaseAndAvailableTrueOrderByCreatedAtDesc(
            List<String> bloodGroups, String city);
}