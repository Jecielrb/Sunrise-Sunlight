package com.jeciel.challenge.controller;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import com.jeciel.challenge.chatgpt.ChatGPTRequest;
import com.jeciel.challenge.chatgpt.ChatGPTResponse;
import com.jeciel.challenge.history.History;
import com.jeciel.challenge.repository.HistoryRepository;
import com.jeciel.challenge.sunrisesunset.SunriseSunsetResponse;

@RestController
@RequestMapping("/jeciel")
public class PoemController {

    @Value("${openai.model}")
    private String model;

    @Value("${openai.api.url}")
    private String chatGPTApiURL;

    @Value("${sunrise-sunset.api.url}")
    private String sunriseSunsetApiURL;

    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private HistoryRepository repository;
    
    //@Autowired
    //private AuthenticationService authenticationService;
    
    @GetMapping("/poem")
    public String generatePoem(
            @RequestParam("latitude") double latitude,
            @RequestParam("longitude") double longitude) {

        // Make a request to the sunrise-sunset API to get the current sun position
        String sunriseSunsetURL = sunriseSunsetApiURL + "?lat=" + latitude + "&lng=" + longitude;
        SunriseSunsetResponse sunriseSunsetResponse = restTemplate.getForObject(sunriseSunsetURL, SunriseSunsetResponse.class);
        
        // Determine the sun's position (e.g., sunrise, daylight, sunset, moonlight)
        String sunPosition = determineSunPosition(sunriseSunsetResponse);

        // Use the sun's position as a prompt for ChatGPT
        String prompt = getPromptBasedOnSunPosition(sunPosition);


        // Make a request to ChatGPT API
        ChatGPTRequest chatGPTRequest = new ChatGPTRequest(model, prompt);
        ChatGPTResponse chatGPTResponse = restTemplate.postForObject(chatGPTApiURL, chatGPTRequest, ChatGPTResponse.class);
        
        //Save request to database
        saveRequestResponse(latitude, longitude, chatGPTResponse.toString());

        return prompt + "\n" + chatGPTResponse.getChoices().get(0).getMessage().getContent();
    }
    
    private String determineSunPosition(SunriseSunsetResponse sunriseSunsetResponse) {
        // generic time formatter
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mm:ss a", Locale.ENGLISH);

        try {
            // Parse sunrise and sunset times
            LocalTime sunriseTime = LocalTime.parse(sunriseSunsetResponse.getResults().getSunrise(), formatter);
            LocalTime sunsetTime = LocalTime.parse(sunriseSunsetResponse.getResults().getSunset(), formatter);
      
            // Get the current time
            LocalTime currentTime = LocalTime.now();
            
            //debugging purposes
            System.out.println(sunsetTime);
            System.out.println(sunriseTime);
            System.out.println(currentTime);
            
            // Compare the current time with sunrise and sunset times
            if (currentTime.isBefore(sunriseTime)) {
                return "moonlight";
            } else if (currentTime.isBefore(sunsetTime)) {
                return "daylight";
            } else if (currentTime == sunriseTime){
            	return "sunrise";
            } else if (currentTime == sunsetTime) {
            	return "sunset";
            } else {
                return "moonlight";
            }
        } catch (DateTimeParseException e) {
            System.out.println(e);
        	return "unknown"; 
        }
    }

    private String getPromptBasedOnSunPosition(String sunPosition) {
        // Create a message for chat gpt based on the sun's position
        switch (sunPosition) {
            case "sunrise":
                return "🌅 Sunrise Short Poem";
            case "daylight":
                return "☀️ Daylight Short Poem";
            case "sunset":
                return "🌇 Sunset Short Poem";
            case "moonlight":
                return "🎑 Moonlight Short Poem";
            default:
                return "Unknown";
        }
    }
    
    private void saveRequestResponse(double latitude, double longitude, String response) {
        // Save the request and response in MongoDB
    	History history = History.builder()
    	        .latitude(latitude)
    	        .longitude(longitude)
    	        .response(response)
    	        .timestamp(LocalDateTime.now())
    	        .build();
        repository.save(history);
    }
}
