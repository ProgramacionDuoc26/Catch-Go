package com.catchandgo.jobs.repository;

import com.catchandgo.jobs.entity.JobOffer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobOfferRepository extends JpaRepository<JobOffer, Long> {
}
