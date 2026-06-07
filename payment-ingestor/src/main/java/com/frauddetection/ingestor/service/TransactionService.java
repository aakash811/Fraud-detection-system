package com.frauddetection.ingestor.service;

import com.frauddetection.common.event.TransactionEvent;
import com.frauddetection.ingestor.producer.TransactionProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionProducer transactionProducer;

    public void publishTransaction(TransactionEvent event) {
        transactionProducer.sendTransaction(event);
        log.debug("Published transaction event: {}", event.getEventId());
    }
}