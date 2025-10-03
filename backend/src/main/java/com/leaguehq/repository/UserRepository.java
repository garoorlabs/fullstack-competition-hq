package com.leaguehq.repository;

import com.leaguehq.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findByStripeCustomerId(String stripeCustomerId);

    Optional<User> findByStripeConnectAccountId(String stripeConnectAccountId);
}
