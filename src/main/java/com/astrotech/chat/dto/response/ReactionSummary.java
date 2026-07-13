package com.astrotech.chat.dto.response;

import java.util.List;

public record ReactionSummary(
        String emoji,
        int count,
        List<String> userIds
){


}
