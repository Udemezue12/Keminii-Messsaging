package com.astrotech.chat.core;

import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class GetPageRequest {
    public static Pageable getPageableWithSorting(int page, int size, String sortBy, boolean withAscending)


    {
        var pagination = new GetCalculatedPagination(page, size);
        if (withAscending){
            return PageRequest.of(pagination.page(), pagination.size(), getSort(sortBy).ascending());
        } else {
            return PageRequest.of(pagination.page(), pagination.size(), getSort(sortBy).descending());
        }


    }
    public static Pageable getPageableWithoutSorting(int page, int size) {
        var pagination = new GetCalculatedPagination(page, size);
        return PageRequest.of(pagination.page(), pagination.size());


    }

    public static @NonNull Sort getSort(String sortBy) {
        return Sort.by(sortBy);
    }
}
