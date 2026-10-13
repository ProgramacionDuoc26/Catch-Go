package com.catchandgo.common.events;

import java.time.Instant;

public record JobApplicationEvent(
    String eventId,
    Long applicationId,
    Long offerId,
    String offerTitle,
    String candidateId,
    String candidateName,
    String candidateEmail,
    String employerId,
    Instant timestamp
) {
    public static JobApplicationEvent create(
            Long applicationId,
            Long offerId,
            String offerTitle,
            String candidateId,
            String candidateName,
            String candidateEmail,
            String employerId) {
        return new JobApplicationEvent(
            java.util.UUID.randomUUID().toString(),
            applicationId,
            offerId,
            offerTitle,
            candidateId,
            candidateName,
            candidateEmail,
            employerId,
            Instant.now()
        );
    }
}
