package com.blooddonation.repository;

import com.blooddonation.model.BloodRequest;
import com.blooddonation.model.Donor;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BloodRequestRepository extends MongoRepository<BloodRequest, String> {

    List<BloodRequest> findByStatus(BloodRequest.RequestStatus status);

    List<BloodRequest> findByBloodGroupNeededAndStatus(Donor.BloodGroup bloodGroup,
                                                        BloodRequest.RequestStatus status);

    List<BloodRequest> findByRequesterEmail(String email);

    List<BloodRequest> findByUrgencyAndStatus(BloodRequest.Urgency urgency,
                                               BloodRequest.RequestStatus status);

    List<BloodRequest> findByCity(String city);

    List<BloodRequest> findByCityAndStatus(String city, BloodRequest.RequestStatus status);

    long countByStatus(BloodRequest.RequestStatus status);
}
