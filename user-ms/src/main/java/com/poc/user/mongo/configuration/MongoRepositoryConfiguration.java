package com.poc.user.mongo.configuration;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.ReactiveMongoTransactionManager;
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

@Configuration
@EnableReactiveMongoRepositories
public class MongoRepositoryConfiguration extends AbstractReactiveMongoConfiguration {

    private final MongoProperties mongoProperties;

    @Autowired
    public MongoRepositoryConfiguration(MongoProperties mongoProperties) {
        this.mongoProperties = mongoProperties;
    }

    @Override
    public MongoClient reactiveMongoClient() {
        MongoCredential credential = MongoCredential.createCredential(mongoProperties.getUsername(), mongoProperties.getDatabase(), mongoProperties.getPassword());
        return MongoClients.create(
                MongoClientSettings.builder()
                        .credential(credential)
                        .build()
        );
    }

    @Override
    protected String getDatabaseName() {
        return mongoProperties.getDatabase();
    }


    @Bean
    ReactiveMongoTransactionManager transactionManager(ReactiveMongoDatabaseFactory reactiveMongoDatabaseFactory) {
        return new ReactiveMongoTransactionManager(reactiveMongoDatabaseFactory);
    }

}
