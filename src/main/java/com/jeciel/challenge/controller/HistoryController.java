package com.jeciel.challenge.controller;

import com.jeciel.challenge.history.History;
import com.jeciel.challenge.repository.HistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/jeciel")
public class HistoryController {

    @Autowired
    private HistoryRepository repository;
    
    //display list of request and responses from the api
    @GetMapping("/history")
    public List<History> getHistory() {
        return repository.findAll();
    }
}
