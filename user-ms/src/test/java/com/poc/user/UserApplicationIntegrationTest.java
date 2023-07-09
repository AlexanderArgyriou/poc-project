package com.poc.user;

import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import com.poc.user.mongo.document.UserDocument;
import com.poc.user.mongo.repository.UserRepository;
import com.poc.user.domain.request.ParticularUserInfo;
import com.poc.user.domain.request.UserInfo;
import com.poc.user.domain.request.UserInfoList;
import com.poc.user.domain.response.UserResponse;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.ReactiveMongoTransactionManager;
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.transaction.ReactiveTransactionManager;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testcontainers.utility.DockerImageName;
import reactor.core.publisher.Mono;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.*;


@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserApplicationIntegrationTest  {

    static final String DB_NAME = "userDB";
    static final String USERS_ENDPOINT_PATH="/v1/users";
    static final String MONGO_DB_CONTAINER_VERSION="mongo:4.4.2";


    @Autowired
    private static final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private ReactiveMongoTemplate mongoTemplate;



    @Autowired
    UserRepository userRepository;

    @Autowired
    private WebTestClient webTestClient;

    public static MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.
            parse(MONGO_DB_CONTAINER_VERSION)).withReuse(true).withCommand("--replSet", "rs0");


    @DynamicPropertySource
    static void mongoDbProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @BeforeAll
    public static void initializeTestEnvironment() {
        //start mongo container
        mongoDBContainer.start();

    }

    @BeforeEach
    public void emptyUsersCollection(){
        //ensure user collection is empty before each test
        mongoTemplate.dropCollection("user").block();
        mongoTemplate.createCollection("user").block();
    }

    @Test
    public void givenNewUser_whenSaved_userExistsOnDB() {
        UserDocument testUserDocument =new UserDocument();
        testUserDocument.setId("test"); testUserDocument.setEmail("test@test.com"); testUserDocument.setFirstname("testName");
        testUserDocument.setLastname("testLastname"); testUserDocument.setUsername("testuser");
        testUserDocument.setActive(true);
        
        UserDocument userDocument =mongoTemplate.save(testUserDocument).block();

        assertThat(userDocument.getId()).isNotNull();
        assertEquals(1, userRepository.findAll().count().block());
    }

    @Test
    void givenAllEligibleFieldsPopulated_whenCreateNewUser_thenUserCreatedSuccessfully() {
        UserInfo testUserInfo=new UserInfo();
        testUserInfo.setEmail("test@test.com"); testUserInfo.setFirstname("testName");
        testUserInfo.setLastname("testLastname"); testUserInfo.setUsername("testuser");

        UserResponse userResponse=webTestClient.post()
                .uri(USERS_ENDPOINT_PATH).body(Mono.just(testUserInfo), UserResponse.class)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(UserResponse.class)
                .returnResult().getResponseBody();
        UserDocument userDocument = userRepository.findById(userResponse.getId()).block();

        assertEquals(testUserInfo.getEmail(),userResponse.getEmail());
        assertEquals(testUserInfo.getFirstname(),userResponse.getFirstname());
        assertEquals(testUserInfo.getLastname(),userResponse.getLastname());
        assertEquals(testUserInfo.getUsername(),userResponse.getUsername());
        assertTrue(userDocument.isActive()); assertNull(userDocument.getDeactivatedTimestamp());
        assertNotNull(userResponse.getCreatedDateTime());
        assertNull(userResponse.getLastModifiedDateTime());
    }

    @Test
    void givenSpecificUserExists_whenReadUser_thenUserInfoReadSuccessfully() {
        UserDocument testUserDocument =new UserDocument();
        testUserDocument.setId("test"); testUserDocument.setEmail("test@test.com"); testUserDocument.setFirstname("testName");
        testUserDocument.setLastname("testLastname"); testUserDocument.setUsername("testuser"); testUserDocument.setActive(true);
        mongoTemplate.insert(testUserDocument).block();

        UserResponse userResponse= webTestClient.get()
                .uri(USERS_ENDPOINT_PATH+"/test")
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserResponse.class)
                .returnResult().getResponseBody();

        assertEquals(testUserDocument.getEmail(),userResponse.getEmail());
        assertEquals(testUserDocument.getFirstname(),userResponse.getFirstname());
        assertEquals(testUserDocument.getLastname(), userResponse.getLastname());
        assertEquals(testUserDocument.getUsername(), userResponse.getUsername());
    }

    @Test
    void givenSpecificUserNotExists_whenReadUser_thenUserNotFound() {
        webTestClient.get()
                .uri(USERS_ENDPOINT_PATH+"/1234")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void givenSpecificUsersExists_whenUsersEdited_thenAllFieldsUpdatedSuccessfully() {
        UserDocument userDocument1 =new UserDocument();
        userDocument1.setId("test"); userDocument1.setEmail("test@test.com"); userDocument1.setFirstname("testName");
        userDocument1.setLastname("testLastname"); userDocument1.setUsername("testuser"); userDocument1.setActive(true);
        UserDocument userDocument2 =mongoTemplate.save(userDocument1).block();
        userDocument2.setId("test2");
        userDocument2.setEmail("test2@test.com");
        mongoTemplate.save(userDocument2).block();

        ParticularUserInfo userInfo1=new ParticularUserInfo();
        userInfo1.setId("test");userInfo1.setEmail("newTest@test.com");userInfo1.setUsername("testuser");
        userInfo1.setLastname("newTestlastname");
        ParticularUserInfo userInfo2=new ParticularUserInfo();
        userInfo2.setId("test2");userInfo2.setEmail("newTest2@test.com"); userInfo2.setUsername("testuser2");
        userInfo2.setLastname("newTestlastname2");
        UserInfoList<ParticularUserInfo> userInfoList = new UserInfoList() {
            {
                add(userInfo1);
                add(userInfo2);
            }
        };

       webTestClient.put()
               .uri(USERS_ENDPOINT_PATH).body(Mono.just(userInfoList), UserInfoList.class)
               .exchange()
               .expectStatus().isOk()
               .expectBody()
               .jsonPath("$[0].id").value(Matchers.in(userInfoList.stream().map(u-> u.getId() ).collect(Collectors.toList())))
               .jsonPath("$[0].email").value(Matchers.in(userInfoList.stream().map(u-> u.getEmail()).collect(Collectors.toList())))
               .jsonPath("$[0].lastname").value(Matchers.in(userInfoList.stream().map(u-> u.getLastname()).collect(Collectors.toList())))
               .jsonPath("$[0].firstname").value(Matchers.in(userInfoList.stream().map(u-> u.getFirstname()).collect(Collectors.toList())))
               .jsonPath("$[0].lastModifiedDateTime").isNotEmpty().jsonPath("$[1].lastModifiedDateTime")
               .isNotEmpty();
    }

    @Test
    void givenNonExistentUserProvided_whenReadUser_thenUserMotFound() {
        webTestClient.get()
                .uri(USERS_ENDPOINT_PATH+"/test")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void givenSpecificUsersExists_whenUsersDeleted_thenUsersNotFetchedAfterDelete() {
        UserDocument userDocument1 =new UserDocument();
        userDocument1.setId("test"); userDocument1.setEmail("test@test.com"); userDocument1.setFirstname("testName");
        userDocument1.setLastname("testLastname"); userDocument1.setUsername("testuser");
        userDocument1.setActive(true);
        UserDocument userDocument2 =new UserDocument();
        userDocument2.setId("test2"); userDocument2.setEmail("test2@test.com"); userDocument2.setFirstname("testName2");
        userDocument2.setLastname("testLastname2"); userDocument2.setUsername("testuser2");
        userDocument2.setActive(true);
        mongoTemplate.insert(userDocument1).block();
        mongoTemplate.insert(userDocument2).block();

        List<UserResponse> userReadResponse= webTestClient.get()
                .uri(USERS_ENDPOINT_PATH+"?id=test&id=test2")
                .exchange()
                .expectStatus().isOk()
                .expectBody(List.class)
                .returnResult().getResponseBody();
        webTestClient.delete()
                .uri(USERS_ENDPOINT_PATH+"?id=test&id=test2")
                .exchange()
                .expectStatus().isOk()
                .expectBody(List.class);
        List<UserResponse> userReadResponseAfterDeletion= webTestClient.get()
                .uri(USERS_ENDPOINT_PATH+"?id=test&id=test2")
                .exchange()
                .expectBody(List.class)
                .returnResult().getResponseBody();

        assertEquals(2,userReadResponse.size());
        assertEquals(0,userReadResponseAfterDeletion.size());
    }
}
