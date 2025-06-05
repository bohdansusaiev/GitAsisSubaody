package com.sigmadevs.aiintegration.ai;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class OpenAIRequest {

    private String model;

    private List<Message> messages;
    public OpenAIRequest(String model, String prompt) {
        this.model = model;
        this.messages = new ArrayList<>();
        this.messages.add(new Message("user", prompt));
    }
}
