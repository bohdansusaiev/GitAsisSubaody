package com.sigmadevs.aiintegration.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/open-ai")
@RequiredArgsConstructor
public class OpenAIController {

    @Value("${openai.model}")
    private String model;

    @Value("${openai.api.url}")
    private String apiURL;

//    @Autowired
    private final   RestTemplate restTemplate;

    @GetMapping("/chat")
    public String chat(@RequestParam("prompt") String prompt) {
        OpenAIRequest request = new OpenAIRequest(model, prompt);
        RestTemplate restTemplate = new RestTemplate();
        OpenAIResponse openAIResponse = restTemplate.postForObject(apiURL, request, OpenAIResponse.class);
        return openAIResponse.getChoiceList().getFirst().getMessage().getContent();
    }

    @GetMapping("/createProject")
    public String createProject(@RequestParam("prompt") String prompt) {
        OpenAIRequest request = new OpenAIRequest(model, prompt);
//        RestTemplate restTemplate = new RestTemplate();

        String openAIResponse = restTemplate.postForObject(apiURL, request, String.class);
        return openAIResponse;
    }

}
