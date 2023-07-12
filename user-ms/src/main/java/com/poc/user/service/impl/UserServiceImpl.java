/**
 *
 */
package com.poc.user.service.impl;


import com.poc.user.domain.request.ParticularUserInfo;
import com.poc.user.domain.request.UserInfo;
import com.poc.user.domain.response.UserResponse;
import com.poc.user.exception.UserNotFoundException;
import com.poc.user.mongo.document.UserDocument;
import com.poc.user.mongo.repository.UserRepository;
import com.poc.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

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
@Transactional
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

    static void dummyLog(String message, String correlation) {
        String threadName = Thread.currentThread().getName();
        String threadNameTail = threadName.substring(
                Math.max(0, threadName.length() - 10));
        System.out.printf("[%10s][%20s] %s%n",
                threadNameTail, correlation, message);
    }

    @Override
    public Flux<UserResponse> readUsers(List<String> ids) {
        return userRepository.findAllByIdInAndActiveTrue(ids).flatMap(u -> Mono.just(modelMapper.map(u, UserResponse.class)));
    }


    public Mono<UserResponse> upsertUser(String id, UserInfo user) {
        //if resource with specified id does not exist, a new resource will be created
        return userRepository.findByIdAndActiveTrue(id)
                .switchIfEmpty(Mono.defer(() -> {
                    UserDocument userDocument = modelMapper.map(user, UserDocument.class);
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
                .flatMap(user ->
                        Mono.deferContextual(contextView -> {
                            dummyLog("upsert user called in another threadpool,",
                                    contextView.get("dummy"));
                            return upsertUser(user.getId(), user);
                        }))
                .subscribeOn(Schedulers.boundedElastic())
                .log();
    }

    @Override
    public Mono<UserResponse> createUser(UserInfo user) {
        UserDocument userDocument = modelMapper.map(user, UserDocument.class);
        userDocument.setCreatedDateTime(Instant.now());
        userDocument.setActive(true);
        return userRepository.save(userDocument)
                .map(savedEntity -> modelMapper.map(savedEntity, UserResponse.class));
    }

    public Mono<UserResponse> deleteUser(String id) {
        return userRepository.findByIdAndActiveTrue(id)
                .switchIfEmpty(Mono.error(new UserNotFoundException("Could not find user to delete")))
                .flatMap(entity -> {
                    entity.setActive(false);
                    entity.setDeactivatedTimestamp(Instant.now());
                    return Mono.just(modelMapper.map(userRepository.save(entity), UserResponse.class));
                });
    }
}