package com.astrotech.chat.entites;

import com.astrotech.chat.core.AppGenerators;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;


@Document(collection = "user_contacts")
@CompoundIndex(name = "owner_contact_idx", def = "{'ownerId': 1, 'contactId': 1}", unique = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserContact {

    @Id
    @Builder.Default
    private String id = AppGenerators.generateTimestampedUUID();
    @Field("owner_id")
    private String ownerId;
    @Field("contact")
   
    private String contact;
    @Field("contact_name")
    private String contactName;

    @Field("blocked")
    @Builder.Default
    private boolean blocked = false;

    @CreatedDate
    @Field("created_at")
    private Instant createdAt;

}
