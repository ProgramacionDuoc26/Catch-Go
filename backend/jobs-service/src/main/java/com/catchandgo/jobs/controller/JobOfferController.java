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

    @PostMapping
    public JobOfferDto create(@RequestBody JobOfferDto dto) {
        return service.create(dto);
    }
}
