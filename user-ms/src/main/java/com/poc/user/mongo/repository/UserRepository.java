package com.poc.user.mongo.repository;

import com.poc.user.mongo.document.UserDocument;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Spring data JPA repository for mongoDB
 * 
 * @author didel
 *
 */
/**
 * {@inheritDoc}
 */
@Repository
public interface UserRepository extends ReactiveCrudRepository<UserDocument, String> {
    Mono<UserDocument> findByIdAndActiveTrue(String id);
    Flux<UserDocument> findAllByIdInAndActiveTrue(List<String> ids);
}
