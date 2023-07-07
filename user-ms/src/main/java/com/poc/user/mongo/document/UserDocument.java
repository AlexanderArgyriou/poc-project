package com.poc.user.mongo.document;


import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

import lombok.Data;
/**
 * Class that represents a user on mongoDB
 * 
 * @author didel
 *
 */
@Document("user")
@Data
public class UserDocument {
	@Id
	private String id;
	private String username;
	private String email;
	private String firstname;
	private String lastname;
	private boolean active;
	private Instant createdDateTime;
	private Instant lastModifiedDateTime;
	private Instant deactivatedTimestamp;
}