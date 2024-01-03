package com.jeciel.challenge.history;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.jeciel.challenge.chatgpt.ChatGPTRequest;
import com.jeciel.challenge.chatgpt.ChatGPTResponse;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Optional;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "request_responses")
public class History {

    @Id
    private String id;

    private double latitude;
    private double longitude;
    private LocalDateTime timestamp;
    private String response;

}
