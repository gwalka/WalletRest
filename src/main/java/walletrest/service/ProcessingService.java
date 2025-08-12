package walletrest.service;

import walletrest.entity.Transaction;
import walletrest.entity.Wallet;
import walletrest.entity.enums.OperationType;
import walletrest.event.KafkaDepositEvent;
import walletrest.event.KafkaWithdrawEvent;
import walletrest.exception.WalletNotFoundException;
import walletrest.exception.WrongOperationException;
import walletrest.repository.TransactionRepository;
import walletrest.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessingService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    @Transactional
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 3000, multiplier = 2))
    public void depositFunds(KafkaDepositEvent event) {
        Wallet wallet = initWallet(event.walletId());
        wallet.setBalance(wallet.getBalance().add(event.amount()));

        Transaction transaction = Transaction.builder()
                .wallet(wallet)
                .amount(event.amount())
                .operationType(OperationType.DEPOSIT)
                .createdAt(LocalDateTime.now())
                .build();

        walletRepository.save(wallet);
        transactionRepository.save(transaction);
        log.info("Баланс кошелька {} на сумму {} пополнен", wallet.getId(), event.amount());
    }

    @Transactional(noRollbackFor = WrongOperationException.class)
    @Retryable(maxAttempts = 3,
            backoff = @Backoff(delay = 5000, multiplier = 2),
            noRetryFor = WrongOperationException.class)
    public void withdrawFunds(KafkaWithdrawEvent event) {
        Wallet wallet = initWallet(event.walletId());

        BigDecimal newBalance = wallet.getBalance().subtract(event.amount());
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new WrongOperationException(
                    "Недостаточно средств для совершения вывода с кошелька %s", wallet.getId());
        }

        wallet.setBalance(newBalance);

        Transaction transaction = Transaction.builder()
                .wallet(wallet)
                .amount(event.amount())
                .operationType(OperationType.WITHDRAW)
                .createdAt(LocalDateTime.now())
                .build();

        walletRepository.save(wallet);
        transactionRepository.save(transaction);
        log.info("Вывод средств с  кошелька {} на сумму {} выполнен", wallet.getId(), event.amount());
    }


    private Wallet initWallet(UUID walletId) {
        return walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException(
                        String.format("Кошелек с id %s не найден", walletId)));
    }

}
