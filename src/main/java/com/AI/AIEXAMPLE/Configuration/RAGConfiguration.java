package com.AI.AIEXAMPLE.Configuration;


import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Configuration
@Slf4j
public class RAGConfiguration {

    @Value("classpath:/Docs/data.txt")
    private Resource resource;

    @Value("vectordatabase.json")
    private String vectorStoreName;


    @Bean
    SimpleVectorStore vectorStore(EmbeddingModel embedding) {
        SimpleVectorStore simpleVectorStore = new SimpleVectorStore(embedding);
        File vectorStoreFile = getVectorStoreFile();
        if (vectorStoreFile.exists()) {
            log.info("Vector Store File Exists,");
            simpleVectorStore.load(vectorStoreFile);
        }else {
            TextReader textReader = new TextReader(resource);
            log.info("Vector Store File Does Not Exist, loading documents");
            List<Document> documents = textReader.get();
            TextSplitter textSplitter = new TokenTextSplitter();
            List<Document> splitDocuments = textSplitter.apply(documents);
            simpleVectorStore.add(splitDocuments);
            simpleVectorStore.save(vectorStoreFile);
        }
        return simpleVectorStore;

    }


    private File getVectorStoreFile() {
        Path path = Paths.get("src", "main", "resources", "data");
        File directory = path.toFile();

        // Ensure directory exists
        if (!directory.exists()) {
            boolean dirCreated = directory.mkdirs();  // Create the directories if they don't exist
            if (!dirCreated) {
                throw new IllegalStateException("Failed to create directory: " + directory.getAbsolutePath());
            }
        }

        String absolutePath = directory.getAbsolutePath() + "/" + vectorStoreName;
        return new File(absolutePath);
    }

}
