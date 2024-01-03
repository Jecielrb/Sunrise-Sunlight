package com.jeciel.challenge.controller;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.jeciel.challenge.history.History;
import com.jeciel.challenge.repository.HistoryRepository;
import com.jeciel.challenge.sunrisesunset.SunriseSunsetResponse;

@RestController
@RequestMapping("/jeciel")
public class KelvinColorController {
	
	
	@Value("${sunrise-sunset.api.url}")
    private String sunriseSunsetApiURL;
	
	@Autowired
    private HistoryRepository repository;
	
	
	@GetMapping("/kelvin")
	public ResponseEntity<Integer> getColorTemp(
	        @RequestParam double latitude,
	        @RequestParam double longitude,
	        RestTemplate restTemplate) {

	    String url = sunriseSunsetApiURL + "?lat=" + latitude + "&lng=" + longitude;

	    SunriseSunsetResponse response = restTemplate.getForObject(url, SunriseSunsetResponse.class);
	    // Calculate the color temperature based on the retrieved times
	    int colorTemperature = calculateColorTemperature(response.getResults().getSunrise(), response.getResults().getSunset());
        saveRequestResponse(latitude, longitude, Integer.toString(colorTemperature));

	    return ResponseEntity.ok(colorTemperature);
	}
	 
	private int calculateColorTemperature(String sunrise, String sunset) {
		
		// Define a DateTimeFormatter for parsing time in the AM/PM format
	    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm:ss a", Locale.ENGLISH);
	    
	    // Parse sunrise and sunset times to get hours and minutes
	    LocalTime sunriseTime = LocalTime.parse(sunrise, timeFormatter);
	    LocalTime sunsetTime = LocalTime.parse(sunset, timeFormatter);

	    // Calculate daylight duration in minutes
	    long daylightDurationMinutes = Duration.between(sunriseTime, sunsetTime).toMinutes();

	    // Calculate the current time of the day
	    LocalTime currentTime = LocalTime.now();
	    
	    //debugging purposes, prints out in military time
        System.out.println(sunsetTime);
        System.out.println(sunriseTime);
        System.out.println(currentTime);

	    // Calculate the elapsed time from sunrise in minutes
	    long elapsedTimeMinutes = Duration.between(sunriseTime, currentTime).toMinutes();

	    // Normalize the elapsed time to be between 0 and daylightDurationMinutes
	    long normalizedElapsedTime = (elapsedTimeMinutes % daylightDurationMinutes + daylightDurationMinutes) % daylightDurationMinutes;

	    // Calculate the color temperature using linear interpolation between 2700K and 6000K
	    double minTemperature = 2700.0;
	    double maxTemperature = 6000.0;

	    double temperature = minTemperature + (maxTemperature - minTemperature) * (normalizedElapsedTime / (double) daylightDurationMinutes);

	    // Ensure the calculated temperature is within the desired range (2700K to 6000K)
	    return (int) Math.max(2700.0, Math.min(6000.0, temperature));
	}
	
	// Save the request and response in MongoDB
	private void saveRequestResponse(double latitude, double longitude, String response) {
    	History history = History.builder()
    	        .latitude(latitude)
    	        .longitude(longitude)
    	        .response(response)
    	        .timestamp(LocalDateTime.now())
    	        .build();
        repository.save(history);
    }

}
