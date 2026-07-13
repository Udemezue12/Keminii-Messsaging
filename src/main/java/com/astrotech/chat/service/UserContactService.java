package com.astrotech.chat.service;



import com.astrotech.chat.core.GetPageRequest;
import com.astrotech.chat.customCache.CustomCacheable;
import com.astrotech.chat.dto.request.AddContactRequest;
import com.astrotech.chat.dto.request.UpdateContactRequest;
import com.astrotech.chat.dto.response.SliceResponse;
import com.astrotech.chat.dto.response.UserContactResponse;
import com.astrotech.chat.exceptions.BadRequestException;
import com.astrotech.chat.exceptions.ConflictException;
import com.astrotech.chat.exceptions.ResourceNotFoundException;
import com.astrotech.chat.mappers.UserContactMapper;
import com.astrotech.chat.repositories.UserContactRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserContactService {

    private final UserService userService;
    private final UserContactRepository userContactRepo;

    @Transactional(readOnly = true)
    @CustomCacheable(value = "contacts",  key = "#userId + '-' + #page + '-' + #size")
    public SliceResponse<UserContactResponse> getContacts(int page, int size, String userId) {

        var pageable = GetPageRequest.getPageableWithSorting(page, size, "contactName", true);
        var result = userContactRepo.findByOwnerId(userId, pageable);
        var content = result.getContent().stream().map(UserContactMapper::toResponse).toList();
        return new SliceResponse<>(
                content,
                page,
                size,
                result.hasNext(),
                result.hasPrevious());

    }
    @Transactional
    @CustomCacheable(value = "single-contact", key= "#userId")
    public UserContactResponse getContact(String userId, String contactId){
        return userContactRepo.findByIdAndOwnerId(contactId, userId).map(UserContactMapper::toResponse).orElseThrow(() -> new  ResourceNotFoundException("User contact not found"));
    }
    @Transactional
    @Caching(
            evict = {
                    @CacheEvict(value = "contacts", key = "#userId"),
                    @CacheEvict(value = "single-contact", key= "#userId")
            }
    )
    public UserContactResponse addContact(AddContactRequest req, String userId) {
        var currentUser = userService.getAuthorizedUser(userId);

        if (currentUser.getPhoneNumber().equals(req.phoneNumber())) {
            throw new BadRequestException("You cannot add yourself as a contact");
        }


        var targetUser = userService.findUserByPhoneNumber(req.phoneNumber())
                .orElseThrow(() -> new ResourceNotFoundException("No user found with this phone number"));


        if (userContactRepo.existsByOwnerIdAndContact(currentUser.getId(), targetUser.getId())) {
            throw new ConflictException("This contact is already in your list");
        }


        var contactMapper = UserContactMapper.createContact(req, currentUser.getId(), targetUser.getId());
        var savedContact = userContactRepo.save(contactMapper);

        return UserContactMapper.toResponse(savedContact);
    }
    @Transactional
    @Caching(
            evict = {
                    @CacheEvict(value = "contacts", key = "#userId"),
                    @CacheEvict(value = "single-contact", key= "#userId")
            }
    )
    public UserContactResponse updateContact(UpdateContactRequest request, String contactId, String userId) {



        var contact = userContactRepo.findByIdAndOwnerId(contactId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Contact not found"));


        if (request.contactName() != null) {
            contact.setContactName(request.contactName());
        }


        if (request.phoneNumber() != null) {

            var currentUser = userService.getUser();
            if (currentUser.getPhoneNumber().equals(request.phoneNumber())) {
                throw new BadRequestException("You cannot add yourself as a contact");
            }


            var targetUser = userService.findUserByPhoneNumber(request.phoneNumber())
                    .orElseThrow(() -> new ResourceNotFoundException("No registered user found with this phone number"));


            if (!targetUser.getId().equals(contact.getContact()) &&
                    userContactRepo.existsByOwnerIdAndContact(userId, targetUser.getId())) {
                throw new ConflictException("This contact is already on your list");
            }


            contact.setContact(targetUser.getId());
        }

        var updatedContact = userContactRepo.save(contact);
        return UserContactMapper.toResponse(updatedContact);
    }
    @Transactional
    @Caching(
            evict = {
                    @CacheEvict(value = "contacts", key = "#userId"),
                    @CacheEvict(value = "single-contact", key= "#userId")
            }
    )
    public void removeContact(String contactId, String userId) {
        if (!userContactRepo.existsByOwnerIdAndId(userId, contactId))
            throw new ResourceNotFoundException("Contact not found");
        userContactRepo.deleteByOwnerIdAndId(userId, contactId);
    }
     public void updateBlockStatus(String ownerId, String contactId, boolean isBlocked) {
        userContactRepo.updateBlockStatus(ownerId, contactId, isBlocked);
     }
}
