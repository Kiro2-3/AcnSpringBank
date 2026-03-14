package com.banking.system.repository;

import com.banking.system.model.Account;
import com.banking.system.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByUser(User user);
    List<Account> findByUserAndActive(User user, boolean active);
    Optional<Account> findByAccountNumber(String accountNumber);
    Optional<Account> findByIdAndUser(Long id, User user);
    List<Account> findByUserOrderByCreatedAtDesc(User user);
}
