package com.astrotech.chat.config;

import org.jobrunr.jobs.Job;
import org.jobrunr.jobs.filters.ApplyStateFilter;
import org.jobrunr.jobs.states.JobState;
import org.jobrunr.jobs.states.StateName;
import org.springframework.stereotype.Component;

@Component
public class DeleteOnSuccessFilter implements ApplyStateFilter {

    @Override
    public void onStateApplied(Job job, JobState oldState, JobState newState) {
        if (newState.getName() == StateName.SUCCEEDED) {
            job.delete("Deleted automatically upon successful execution.");
        }
    }
}

