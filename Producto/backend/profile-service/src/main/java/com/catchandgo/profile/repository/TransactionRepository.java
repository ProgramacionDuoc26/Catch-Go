package com.catchandgo.profile.repository;

import com.catchandgo.profile.entity.Transaction;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findByToken(String token);
    Optional<Transaction> findByBuyOrder(String buyOrder);
}
