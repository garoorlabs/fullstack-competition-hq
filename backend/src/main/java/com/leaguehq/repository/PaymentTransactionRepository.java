package com.leaguehq.repository;

import com.leaguehq.model.PaymentTransaction;
import com.leaguehq.model.PaymentTransaction.TransactionStatus;
import com.leaguehq.model.PaymentTransaction.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, UUID> {

    Optional<PaymentTransaction> findByStripePaymentIntentId(String stripePaymentIntentId);

    Optional<PaymentTransaction> findByStripeCheckoutSessionId(String stripeCheckoutSessionId);

    List<PaymentTransaction> findByTeamId(UUID teamId);

    List<PaymentTransaction> findByCompetitionId(UUID competitionId);

    List<PaymentTransaction> findByUserId(UUID userId);

    List<PaymentTransaction> findByTransactionType(TransactionType transactionType);

    List<PaymentTransaction> findByStatus(TransactionStatus status);

    @Query("SELECT pt FROM PaymentTransaction pt WHERE pt.competition.id = :competitionId " +
           "AND pt.transactionType = :transactionType AND pt.status = :status")
    List<PaymentTransaction> findByCompetitionIdAndTypeAndStatus(
        UUID competitionId,
        TransactionType transactionType,
        TransactionStatus status
    );

    @Query("SELECT SUM(pt.netToOwnerCents) FROM PaymentTransaction pt " +
           "WHERE pt.competition.id = :competitionId AND pt.status = 'SUCCEEDED'")
    Long sumNetToOwnerByCompetitionId(UUID competitionId);
}
