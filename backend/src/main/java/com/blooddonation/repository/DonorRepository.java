package com.blooddonation.repository;

import com.blooddonation.model.Donor;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DonorRepository extends MongoRepository<Donor, String> {

    Optional<Donor> findByEmail(String email);

    Optional<Donor> findByPhone(String phone);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    List<Donor> findByBloodGroupAndAvailableTrue(Donor.BloodGroup bloodGroup);

    List<Donor> findByBloodGroupInAndAvailableTrueAndActiveTrue(List<Donor.BloodGroup> bloodGroups);

    List<Donor> findByCity(String city);

    List<Donor> findByCityAndBloodGroupAndAvailableTrue(String city, Donor.BloodGroup bloodGroup);

    List<Donor> findByAvailableTrue();

    List<Donor> findByActiveTrue();

    long countByBloodGroup(Donor.BloodGroup bloodGroup);

    long countByAvailableTrue();
}
