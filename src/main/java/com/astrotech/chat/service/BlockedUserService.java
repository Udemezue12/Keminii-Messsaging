package com.astrotech.chat.service;



import com.astrotech.chat.core.GetPageRequest;
import com.astrotech.chat.dto.response.SliceResponse;
import com.astrotech.chat.dto.response.UserResponse;
import com.astrotech.chat.entites.BlockedUser;
import com.astrotech.chat.exceptions.BadRequestException;
import com.astrotech.chat.exceptions.ConflictException;
import com.astrotech.chat.exceptions.ResourceNotFoundException;
import com.astrotech.chat.mappers.UserMapper;
import com.astrotech.chat.repositories.BlockedUserRepository;
import com.astrotech.chat.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class BlockedUserService {
    private final BlockedUserRepository blockedUserRepository;

    private final UserService userService;
    private final UserContactService contactService;
    private final UserRepository userRepository;
    private final AuditService auditService;
    @Transactional
    public void blockUser(String blockerId,String targetId) {

        if (blockerId.equals(targetId)) throw new BadRequestException("Cannot block yourself");
        if (blockedUserRepository.existsByBlockerIdAndBlockedId(blockerId, targetId))
            throw new ConflictException("User is already blocked");
        var target = userService.getAuthorizedUser(targetId);
        blockedUserRepository.save(BlockedUser.builder()
                .blockerId(blockerId).blockedId(target.getId()).build());
        contactService.updateBlockStatus(blockerId, target.getId(), true);
//        auditService.log(blockerId, "USER_BLOCKED", "User", targetId, null, null, null);
    }

    @Transactional
    public void unblockUser(String blockerId,String targetId) {


        if (!blockedUserRepository.existsByBlockerIdAndBlockedId(blockerId, targetId))
            throw new ResourceNotFoundException("Block not found");
        var target = userService.getAuthorizedUser(targetId);
        blockedUserRepository.deleteByBlockerIdAndBlockedId(blockerId, target.getId());

        contactService.updateBlockStatus(blockerId, target.getId(), false);
//        auditService.log(blockerId, "USER_UNBLOCKED", "User", targetId, null, null, null);
    }




    public SliceResponse<UserResponse> getBlockedUsers(String currentUserId, int page, int size) {
        var pageable = GetPageRequest.getPageableWithSorting(page, size, "createdAt", true);


       var blockedIds = blockedUserRepository.findByBlockerId(currentUserId, pageable)
                .stream()
                .map(BlockedUser::getBlockedId)
                .toList();

        if (blockedIds.isEmpty()) {
            return new SliceResponse<>(Collections.emptyList(), page, size, false, false);
        }

       var blockedUsersSlice = userRepository.findByIdIn(blockedIds, pageable);

        var content = blockedUsersSlice.getContent().stream()
                .map(UserMapper::response)
                .toList();

        return new SliceResponse<>(
                content,
                page,
                size,
                blockedUsersSlice.hasNext(),
                blockedUsersSlice.hasPrevious()
        );
    }



    public boolean isBlockActive(String userA, String userB) {

        return blockedUserRepository.existsByBlockerIdAndBlockedId(userA, userB) ||
                blockedUserRepository.existsByBlockerIdAndBlockedId(userB, userA);
    }
}
