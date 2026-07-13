package com.astrotech.chat.mappers;

import com.astrotech.chat.dto.request.AddContactRequest;
import com.astrotech.chat.dto.response.UserContactResponse;
import com.astrotech.chat.entites.UserContact;

import java.time.Instant;

public class UserContactMapper {
    public static UserContact createContact(AddContactRequest request, String userId, String targetUserId){
        return UserContact.builder()
                .ownerId(userId)
                .contact(targetUserId)
                .contactName(request.contactName())
                .blocked(false)
                .createdAt(Instant.now())
                .build();

    }
    public static UserContactResponse toResponse(UserContact contact){
        return new UserContactResponse(
                contact.getContactName(),
                contact.getContact(),
                contact.isBlocked()

        );
    }
}
