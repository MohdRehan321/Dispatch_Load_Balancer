package com.dispatch_load_balancer.service;


import com.dispatch_load_balancer.entity.DispatchBatch;
import com.dispatch_load_balancer.repository.DispatchBatchRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ActiveBatchService {

    private final DispatchBatchRepository batchRepository;

    public ActiveBatchService(
            DispatchBatchRepository batchRepository) {
        this.batchRepository = batchRepository;
    }

    @Transactional
    public DispatchBatch getOrCreateActiveBatch() {

        batchRepository.createActiveBatchIfMissing();

        return batchRepository.findFirstByActiveTrue()
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Unable to create or retrieve active dispatch batch."
                        )
                );
    }
}

