package com.AI.AIEXAMPLE.Service;

import com.AI.AIEXAMPLE.DTO.Contents;
import com.AI.AIEXAMPLE.DTO.Parts;
import com.AI.AIEXAMPLE.DTO.Prompt;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class AIService {

    private final RestTemplate restTemplate;

    @Autowired
    public AIService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }


    public String sendPostRequest(String query) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Parts parts = new Parts();
        parts.setText(query + "also dont format the result");


        List<Parts> partsList = new ArrayList<>();
        partsList.add(parts);

        Contents contents = new Contents();
        contents.setParts(partsList);

        List<Contents> contentsList = new ArrayList<>();
        contentsList.add(contents);

        Prompt prompt = new Prompt();
        prompt.setContents(contentsList);


        HttpEntity<Object> request = new HttpEntity<>(prompt, headers);


        JsonNode node = restTemplate.postForObject("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent?key=AIzaSyDg64BOiig8J4wy5VF550WSPt1XqvwUkgo", request, JsonNode.class);
        System.out.println(node);

        assert node != null;
        return node.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText().replace("\n", " ").replace("*","");
    }



    public List<Double> generateRandomDoubles(int size, double min, double max) {
        List<Double> randomDoubles = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            double randomValue = ThreadLocalRandom.current().nextDouble(min, max);
            randomDoubles.add(randomValue);
        }

        return randomDoubles;
    }


    public List<Double> calculateEMA(List<Double> prices, int period) {
        List<Double> ema = new ArrayList<>();
        double k = 2.0 / (period + 1);
        double previousEma = prices.getFirst();

        for (Double price : prices) {
            double currentEma = (price - previousEma) * k + previousEma;
            ema.add(currentEma);
            previousEma = currentEma;
        }
        return ema;
    }

    public MACD getMACD(List<Double> prices) {
        List<Double> shortEma = calculateEMA(prices, 12);
        List<Double> longEma = calculateEMA(prices, 26);

        List<Double> macd = new ArrayList<>();
        for (int i = 0; i < longEma.size(); i++) {
            macd.add(shortEma.get(i) - longEma.get(i));
        }

        // Calculate Signal Line (9-period EMA of MACD)
        List<Double> signalLine = calculateEMA(macd, 9);

        MACD macd1 = new MACD();
        macd1.setMacd(macd);
        macd1.setSingleline(signalLine);
        return macd1;
    }


    @Data
    public static class MACD{
       private List<Double> macd;
       private List<Double> singleline;
    }

    public static double mean(List<Double> data) {
        return data.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }

    public static double median(List<Double> data) {
        Collections.sort(data);
        int size = data.size();
        if (size % 2 == 0) {
            return (data.get(size / 2 - 1) + data.get(size / 2)) / 2.0;
        } else {
            return data.get(size / 2);
        }
    }

    public static double standardDeviation(List<Double> data) {
        double mean = mean(data);
        double variance = data.stream().mapToDouble(d -> Math.pow(d - mean, 2)).sum() / data.size();
        return Math.sqrt(variance);
    }
}
