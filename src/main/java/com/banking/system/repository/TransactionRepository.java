package com.banking.system.repository;

import com.banking.system.model.Account;
import com.banking.system.model.Transaction;
import com.banking.system.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUserOrderByCreatedAtDesc(User user);
    Page<Transaction> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    Optional<Transaction> findByReferenceNumber(String referenceNumber);

    @Query("SELECT t FROM Transaction t WHERE t.user = :user AND " +
           "(t.sourceAccount = :account OR t.destinationAccount = :account) " +
           "ORDER BY t.createdAt DESC")
    List<Transaction> findByUserAndAccount(@Param("user") User user, @Param("account") Account account);

    @Query("SELECT t FROM Transaction t WHERE t.user = :user ORDER BY t.createdAt DESC")
    List<Transaction> findRecentByUser(@Param("user") User user, Pageable pageable);
}
