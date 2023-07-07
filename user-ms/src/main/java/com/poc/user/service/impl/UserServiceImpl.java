/**
 *
 */
package com.poc.user.service.impl;


import com.poc.user.domain.request.ParticularUserInfo;
import com.poc.user.domain.request.UserInfo;
import com.poc.user.domain.response.UserResponse;
import com.poc.user.exception.UserNotFoundException;
import com.poc.user.jpa.document.UserDocument;
import com.poc.user.jpa.repository.UserRepository;
import com.poc.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Implementation of {@link UserService UserService.class}
 *
 * @author didel
 */
@Service("userService")
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final ModelMapper modelMapper;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, ModelMapper modelMapper) {
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public Mono<UserResponse> readUser(String id) {
        return userRepository.findByIdAndActiveTrue(id).map(u -> modelMapper.map(u, UserResponse.class))
                .switchIfEmpty(Mono.error(new UserNotFoundException("Could not find specific user")));
    }

    @Override
    public Flux<UserResponse> readUsers(List<String> ids) {
        return userRepository.findAllByIdInAndActiveTrue(ids).map(u -> modelMapper.map(u, UserResponse.class));
    }

    @Override
    public Mono<UserResponse> createUser(UserInfo user) {
        UserDocument userDocument = modelMapper.map(user, UserDocument.class);
        userDocument.setCreatedDateTime(Instant.now());
        userDocument.setActive(true);
        return userRepository.save(userDocument)
                .flatMap(savedEntity -> Mono.just(modelMapper.map(savedEntity, UserResponse.class)));
    }

    public Mono<UserResponse> upsertUser(String id, UserInfo user) {
        //if resource with specified id does not exist, a new resource will be created
        return userRepository.findByIdAndActiveTrue(id)
                .switchIfEmpty(Mono.defer(() -> {
                    UserDocument userDocument =modelMapper.map(user, UserDocument.class);
                    userDocument.setId(id);
                    userDocument.setCreatedDateTime(Instant.now());
                    userDocument.setActive(true);
                    return userRepository.save(userDocument);
                }))
                .flatMap(entity -> {
                    BeanUtils.copyProperties(user, entity);
                    entity.setLastModifiedDateTime(Instant.now());
                    return userRepository.save(entity);
                })
                .map(savedEntity -> modelMapper.map(savedEntity, UserResponse.class));

    }

    public Flux<UserResponse> upsertUsers(List<ParticularUserInfo> users) {
        return Flux.fromIterable(users)
                .filter(Objects::nonNull)
                .flatMap(user -> upsertUser(user.getId(), user));
    }

    public Mono<UserResponse> deleteUser(String id) {
        return userRepository.findByIdAndActiveTrue(id)
                .switchIfEmpty(Mono.error(new UserNotFoundException("Could not find user to deleted")))
                .flatMap(entity -> {
                    entity.setActive(false);
                    entity.setDeactivatedTimestamp(Instant.now());
                    return userRepository.save(entity);
                })
                .map(savedEntity -> modelMapper.map(savedEntity, UserResponse.class));

    }

    public Flux<UserResponse> deleteUsers(List<String> ids) {
        return userRepository.findAllByIdInAndActiveTrue(ids)
                . switchIfEmpty(Mono.error(new UserNotFoundException("Could not find user to delete")))
                .collectList()
                .flatMapMany(entities -> {
                    entities.forEach(entity -> {
                        entity.setActive(false);
                        entity.setDeactivatedTimestamp(Instant.now());
                    });
                    return userRepository.saveAll(entities)
                            .doOnNext(entity -> log.info("Entity with id: {} deleted successfully", entity.getId()));
                })
                .map(entity -> modelMapper.map(entity, UserResponse.class));

    }
}