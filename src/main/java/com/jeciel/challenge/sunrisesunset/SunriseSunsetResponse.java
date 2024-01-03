package com.jeciel.challenge.sunrisesunset;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SunriseSunsetResponse {

    @JsonProperty("results")
    private Results results;

    // Getters and setters

    public Results getResults() {
        return results;
    }

    public void setResults(Results results) {
        this.results = results;
    }
}

