package com.astrotech.chat.tasks;


import com.astrotech.chat.events.MediaUploadEvent;
import com.astrotech.chat.service.MediaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jobrunr.jobs.annotations.Job;
import org.jobrunr.scheduling.JobScheduler;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import java.util.Objects;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class SaveMessageMediaTask {
    private final MediaService mediaService;
    private final JobScheduler jobScheduler;

    @EventListener(MediaUploadEvent.class)
    @Job(name = "handle-media-save", retries = 3)
    public void handleMediaSave(MediaUploadEvent event){
        var secureUrl = event.secureUrl();
        var mediaType = event.mediaType();
        var originalName = event.originalName();
        var mimeType = event.mimeType();
        var fileSize = event.fileSize();
        var thumbNail = event.thumbnailUrl();
        var finalWidth = Objects.requireNonNullElse(event.width(), 0);
        var finalHeight = Objects.requireNonNullElse(event.height(), 0);
        var finalDuration = event.duration() != null ? event.duration() : 0;
        var checkSum = event.checksum();
        var publicId = event.publicId();
        var fileHash  = event.fileHash();
        var messageId = event.messageId();

        jobScheduler.enqueue(() -> {

                mediaService.createMedia(secureUrl, mediaType, originalName, mimeType, fileSize,
                        thumbNail, finalWidth, finalHeight,finalDuration,
                        checkSum, publicId, fileHash, messageId);}
        );

    }


}
