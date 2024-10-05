package com.AI.AIEXAMPLE.Controller;

import ch.qos.logback.core.model.Model;
import com.AI.AIEXAMPLE.DTO.Details;
import com.AI.AIEXAMPLE.DTO.Profile;
import com.AI.AIEXAMPLE.Service.AIService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/ink")
public class AIController {

    private final ChatClient chatClient;
    private final AIService aiService;
    private final VectorStore vectorStore;

    @Value("classpath:/PromptTemplates/rag-prompt-template.st")
    private Resource ragPromptTemplate;


    public AIController(ChatClient.Builder chatClient, AIService aiService, VectorStore vectorStore) {
        this.chatClient = chatClient.build();
        this.aiService = aiService;
        this.vectorStore = vectorStore;
    }


    @GetMapping("/chat")
    public String sample(@RequestParam String input)
    {
        return aiService.sendPostRequest(input);
    }

//    @GetMapping("/huh")
//    public String huh(@RequestParam String company)
//    {
//        String message = """
//                give Market Cap, EV,Shares Out,Revenue,Employee of the company {company} in proper units about the company
//                """;
//        PromptTemplate promptTemplate = new PromptTemplate(message);
//        Prompt prompt = promptTemplate.create(Map.of("company", company));
//        return chatClient.prompt(prompt).call().content();
//    }

    @GetMapping("/overview")
    public String yoo(@RequestParam String company)
    {
        var system = new SystemMessage("Limit it to almost 30 words and dont use *");
        var user = new UserMessage("Give a over view of company "+company);

        Prompt prompt = new Prompt(List.of(system,user));
        return chatClient.prompt(prompt).call().content();
    }

    @GetMapping("/topCompanies")
    public List<String> boo()
    {
        var system = new SystemMessage("Limit list to 5 elements and dont use * and dont use any heading");
        var user = new UserMessage("top gross company names");

        Prompt prompt = new Prompt(List.of(system,user));
        ChatResponse response =  chatClient.prompt(prompt).call().chatResponse();
        System.out.println(response.getResult().getOutput().getContent());

        List<String> strings = Arrays.stream(response.getResult().getOutput().getContent().split("\n"))
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .toList();

        return strings;
    }

    @GetMapping("/details")
    public Details details(@RequestParam String company)
    {
        List<Document> documents = vectorStore.similaritySearch(SearchRequest.query(company).withTopK(2));
        List<String> strings = documents.stream().map(Document::getContent).toList();


        BeanOutputConverter<Details> beanOutputConverter = new BeanOutputConverter<>(Details.class);
        String format = beanOutputConverter.getFormat();
        String message = "get details about "+company+" in "+format;
        PromptTemplate promptTemplate = new PromptTemplate(ragPromptTemplate);
        promptTemplate.add("input",message);
        promptTemplate.add("documents" ,String.join("\n",strings));

        Prompt prompt = promptTemplate.create();

        Generation generation = chatClient.prompt(prompt).call().chatResponse().getResult();

        return beanOutputConverter.convert(generation.getOutput().getContent());
    }

    @GetMapping("/profile")
    public Profile profile(@RequestParam String company)
    {
        String message = """
                get details about {company} in {format}
                """;
        PromptTemplate promptTemplate = new PromptTemplate(message);
        BeanOutputConverter<Profile> beanOutputConverter = new BeanOutputConverter<>(Profile.class);

        String format = beanOutputConverter.getFormat();

        Prompt prompt = promptTemplate.create(Map.of("company", company, "format",format));

        Generation generation = chatClient.prompt(prompt).call().chatResponse().getResult();

        return beanOutputConverter.convert(generation.getOutput().getContent());
    }

    @GetMapping("/chatOllama")
    public String chatOllama(@RequestParam String input)
    {
        Prompt prompt = new Prompt(input);
        return chatClient.prompt(prompt).call().content();
    }

    @GetMapping("/getStockDetails")
    public String getStockDetails(@RequestParam String company)
    {
        String message = """
                get historical sales data (or stock prices) for {company}
                """;
        PromptTemplate promptTemplate = new PromptTemplate(message);
        Prompt prompt = promptTemplate.create(Map.of("company", company));
        return chatClient.prompt(prompt).call().content();
    }



}
