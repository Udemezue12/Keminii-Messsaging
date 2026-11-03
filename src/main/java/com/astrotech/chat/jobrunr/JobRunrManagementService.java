package com.astrotech.chat.jobrunr;

import lombok.RequiredArgsConstructor;
import org.jobrunr.jobs.states.StateName;
import org.jobrunr.storage.StorageProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class JobRunrManagementService {


    private final StorageProvider storageProvider;


    @Transactional
    public int purgeSucceededJobs() {
        return storageProvider.deleteJobsPermanently(StateName.SUCCEEDED, Instant.now());
    }

    @Transactional
    public int purgeDeletedJobs() {
        return storageProvider.deleteJobsPermanently(StateName.DELETED, Instant.now());
    }


}