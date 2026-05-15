package com.catchandgo.jobs.controller;

import com.catchandgo.jobs.dto.JobOfferDto;
import com.catchandgo.jobs.dto.JobApplicationDto;
import com.catchandgo.jobs.entity.JobApplication;
import com.catchandgo.jobs.service.JobOfferService;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/jobs")
public class JobOfferController {
    private final JobOfferService service;

    public JobOfferController(JobOfferService service) {
        this.service = service;
    }

    @GetMapping
    public List<JobOfferDto> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public JobOfferDto findById(@PathVariable("id") Long id) {
        return service.findById(id);
    }

    @PostMapping
    public JobOfferDto create(@RequestBody JobOfferDto dto) {
        return service.create(dto);
    }

    @PostMapping("/{id}/apply")
    public void apply(@PathVariable("id") Long id, @RequestParam("userId") String userId) {
        service.apply(id, userId);
    }

    @GetMapping("/applications/user/{userId}")
    public List<JobApplicationDto> findByUserId(@PathVariable("userId") String userId) {
        return service.findApplicationsByUserId(userId);
    }

    @GetMapping("/applications/employer/{employerId}")
    public List<JobApplication> findByEmployerId(@PathVariable("employerId") String employerId) {
        return service.findApplicationsByEmpresaId(employerId);
    }

    @GetMapping("/applications")
    public List<JobApplication> findAllApplications() {
        return service.findAllApplications();
    }

    @PutMapping("/{id}")
    public JobOfferDto update(@PathVariable("id") Long id, @RequestBody JobOfferDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") Long id) {
        service.delete(id);
    }

    @DeleteMapping("/applications/{id}")
    public void deleteApplication(@PathVariable("id") Long id) {
        service.deleteApplication(id);
    }

    @PutMapping("/applications/{id}/status")
    public void updateApplicationStatus(@PathVariable("id") Long id, @RequestBody Map<String, String> body) {
        service.updateApplicationStatus(id, body.get("status"));
    }
}
