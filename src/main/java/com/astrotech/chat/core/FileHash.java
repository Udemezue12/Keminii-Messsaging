package com.astrotech.chat.core;


import com.astrotech.chat.exceptions.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.InputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.UUID;

@Slf4j
@Service
public class FileHash {


    private final WebClient fileHashWebClient;

    public FileHash(@Qualifier("fileHashWebClient") WebClient fileHashWebClient) {
        this.fileHashWebClient = fileHashWebClient;
    }

    public Mono<String> fileHashAsync(String fileUrl) {

        return Mono.defer(() -> {

            final MessageDigest digest;

            try {
                digest = MessageDigest.getInstance("SHA-256");
            } catch (NoSuchAlgorithmException ex) {
                return Mono.error(new IllegalStateException(
                        "SHA-256 algorithm is unavailable", ex));
            }

            return fileHashWebClient.get()
                    .uri(fileUrl)
                    .retrieve()

                    .onStatus(
                            HttpStatusCode::isError,
                            response -> response.bodyToMono(String.class)
                                    .defaultIfEmpty("")
                                    .flatMap(body -> Mono.error(
                                            new BadRequestException(
                                                    "Failed to fetch file from URL. HTTP "
                                                            + response.statusCode()
                                                            + (body.isBlank() ? "" : ": " + body)
                                            )
                                    ))
                    )

                    .bodyToFlux(DataBuffer.class)

                    .doOnNext(buffer -> {
                        try (DataBuffer.ByteBufferIterator iterator =
                                     buffer.readableByteBuffers()) {

                            while (iterator.hasNext()) {
                                digest.update(iterator.next());
                            }
                        } finally {
                            DataBufferUtils.release(buffer);
                        }
                    })

                    .doOnDiscard(DataBuffer.class, DataBufferUtils.releaseConsumer())

                    .then(Mono.fromSupplier(() ->
                            sha256ToUuid(digest.digest())
                    ))

                    .timeout(Duration.ofSeconds(30));
        });
    }

    public String fileHashSync(String fileUrl) {
        try {
            var digest = MessageDigest.getInstance("SHA-256");


            var uri = URI.create(fileUrl);
            try (InputStream is = uri.toURL().openConnection().getInputStream()) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    digest.update(buffer, 0, bytesRead);
                }
            }

            return HexFormat.of().formatHex(digest.digest());

        } catch (Exception e) {
            log.error("Sync file hash failed for URL: {}", fileUrl, e);
            throw new BadRequestException("Failed to fetch file");
        }
    }

    public Mono<String> fileHash(String fileUrl) {
        return Mono.fromCallable(() -> fileHashSync(fileUrl))
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorResume(Exception.class, ex -> {
                    if (ex instanceof BadRequestException) {
                        return Mono.error(ex);
                    }
                    log.warn("Sync hashing failed, attempting fallback to Async Client Session: {}", ex.getMessage());
                    return fileHashAsync(fileUrl);
                });
    }
    private static String sha256ToUuid(byte[] hash) {

        if (hash.length < 16) {
            throw new IllegalArgumentException("SHA-256 hash must contain at least 16 bytes.");
        }

       var buffer = ByteBuffer.wrap(hash);

        return new UUID(
                buffer.getLong(0),
                buffer.getLong(8)
        ).toString();
    }
}