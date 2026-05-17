package com.catchandgo.jobs.repository;

import com.catchandgo.jobs.entity.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    List<JobApplication> findByJobId(Long jobId);
    List<JobApplication> findByUserId(String userId);
    List<JobApplication> findByUserIdAndJobId(String userId, Long jobId);

    @Query("SELECT a FROM JobApplication a WHERE a.jobId IN (SELECT o.id FROM JobOffer o WHERE o.empresaId = :empresaId)")
    List<JobApplication> findAllByEmpresaId(@Param("empresaId") String empresaId);
}
