package com.leaguehq.repository;

import com.leaguehq.model.SubscriptionEvent;
import com.leaguehq.model.SubscriptionEvent.EventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SubscriptionEventRepository extends JpaRepository<SubscriptionEvent, UUID> {

    List<SubscriptionEvent> findByTeamId(UUID teamId);

    List<SubscriptionEvent> findBySubscriptionId(String subscriptionId);

    List<SubscriptionEvent> findByEventType(EventType eventType);

    List<SubscriptionEvent> findByTeamIdOrderByCreatedAtDesc(UUID teamId);

    List<SubscriptionEvent> findBySubscriptionIdOrderByCreatedAtDesc(String subscriptionId);
}
