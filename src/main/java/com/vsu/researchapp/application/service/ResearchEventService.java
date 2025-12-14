package com.vsu.researchapp.application.service;

import org.springframework.stereotype.Service;

import com.vsu.researchapp.domain.repository.ResearchEventRepository;

@Service
public class ResearchEventService {
    private final ResearchEventRepository researchEventRepository;

    public ResearchEventService(ResearchEventRepository researchEventRepository) {
        this.researchEventRepository = researchEventRepository;
    }

    
}
