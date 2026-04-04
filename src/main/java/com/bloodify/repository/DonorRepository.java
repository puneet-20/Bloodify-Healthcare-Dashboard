package com.bloodify.repository;

import com.bloodify.model.Donor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DonorRepository extends JpaRepository<Donor, Long> {
    List<Donor> findByBloodGroupInOrderByTimestampDesc(List<String> bloodGroups);
    List<Donor> findByBloodGroupInAndCityOrderByTimestampDesc(List<String> bloodGroups, String city);
    List<Donor> findAllByOrderByTimestampDesc();
}
