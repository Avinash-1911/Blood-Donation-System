package com.blooddonation.repository;

import com.blooddonation.model.DonationEvent;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DonationEventRepository extends MongoRepository<DonationEvent, String> {

    List<DonationEvent> findByDonorId(String donorId);

    List<DonationEvent> findByRequestId(String requestId);

    List<DonationEvent> findByStatus(DonationEvent.EventStatus status);

    long countByDonorId(String donorId);
}
