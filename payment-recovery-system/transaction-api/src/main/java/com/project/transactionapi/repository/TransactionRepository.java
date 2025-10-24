package com.project.transactionapi.repository;

import com.project.transactionapi.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    Optional<Transaction> findByTxnId(String txnId);

    // used by retry-service or by RecoveryService when using same repo
    @Query("select t from Transaction t where t.status = :status and (t.nextRetryAt is null or t.nextRetryAt <= :now)")
    List<Transaction> findByStatusAndNextRetryBefore(@Param("status") String status, @Param("now") OffsetDateTime now);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Transaction t where t.id = :id")
    Optional<Transaction> findByIdForUpdate(@Param("id") UUID id);
}
