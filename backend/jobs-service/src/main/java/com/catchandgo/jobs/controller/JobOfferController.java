package com.catchandgo.jobs.controller;

import com.catchandgo.jobs.dto.JobOfferDto;
import com.catchandgo.jobs.service.JobOfferService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public JobOfferDto findById(@org.springframework.web.bind.annotation.PathVariable("id") Long id) {
        return service.findById(id);
    }

    @PostMapping
    public JobOfferDto create(@RequestBody JobOfferDto dto) {
        return service.create(dto);
    }

    @PostMapping("/{id}/apply")
    public void apply(@org.springframework.web.bind.annotation.PathVariable("id") Long id, @org.springframework.web.bind.annotation.RequestParam("userId") String userId) {
        service.apply(id, userId);
    }

    @GetMapping("/applications/user/{userId}")
    public List<com.catchandgo.jobs.dto.JobApplicationDto> findByUserId(@org.springframework.web.bind.annotation.PathVariable("userId") String userId) {
        return service.findApplicationsByUserId(userId);
    }

    @GetMapping("/applications/employer/{employerId}")
    public List<com.catchandgo.jobs.entity.JobApplication> findByEmployerId(@org.springframework.web.bind.annotation.PathVariable("employerId") String employerId) {
        return service.findApplicationsByEmpresaId(employerId);
    }

    @GetMapping("/applications")
    public List<com.catchandgo.jobs.entity.JobApplication> findAllApplications() {
        return service.findAllApplications();
    }

    @org.springframework.web.bind.annotation.PutMapping("/{id}")
    public JobOfferDto update(@org.springframework.web.bind.annotation.PathVariable("id") Long id, @RequestBody JobOfferDto dto) {
        return service.update(id, dto);
    }

    @org.springframework.web.bind.annotation.DeleteMapping("/{id}")
    public void delete(@org.springframework.web.bind.annotation.PathVariable("id") Long id) {
        service.delete(id);
    }

    @org.springframework.web.bind.annotation.DeleteMapping("/applications/{id}")
    public void deleteApplication(@org.springframework.web.bind.annotation.PathVariable("id") Long id) {
        service.deleteApplication(id);
    }

    @org.springframework.web.bind.annotation.PutMapping("/applications/{id}/status")
    public void updateApplicationStatus(@org.springframework.web.bind.annotation.PathVariable("id") Long id, @RequestBody java.util.Map<String, String> body) {
        service.updateApplicationStatus(id, body.get("status"));
    }
}
