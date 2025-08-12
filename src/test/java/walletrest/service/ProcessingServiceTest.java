package walletrest.service;

import walletrest.entity.Wallet;
import walletrest.entity.enums.OperationType;
import walletrest.event.KafkaDepositEvent;
import walletrest.event.KafkaWithdrawEvent;
import walletrest.exception.WalletNotFoundException;
import walletrest.exception.WrongOperationException;
import walletrest.repository.TransactionRepository;
import walletrest.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcessingServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private ProcessingService processingService;

    private UUID walletId;
    private Wallet wallet;

    @BeforeEach
    void setup() {
        walletId = UUID.randomUUID();
        wallet = new Wallet();
        wallet.setId(walletId);
        wallet.setBalance(BigDecimal.valueOf(100));
    }

    @Test
    void positiveDepositFunds() {
        KafkaDepositEvent event = new KafkaDepositEvent(walletId, BigDecimal.valueOf(50));

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(transactionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        processingService.depositFunds(event);

        assertEquals(BigDecimal.valueOf(150), wallet.getBalance());

        verify(walletRepository).save(wallet);
        verify(transactionRepository).save(argThat(t ->
                t.getAmount().equals(BigDecimal.valueOf(50)) && t.getOperationType() == OperationType.DEPOSIT));
    }

    @Test
    void positiveWithdrawFunds() {
        KafkaWithdrawEvent event = new KafkaWithdrawEvent(walletId, BigDecimal.valueOf(40));

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(transactionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        processingService.withdrawFunds(event);

        assertEquals(BigDecimal.valueOf(60), wallet.getBalance());

        verify(walletRepository).save(wallet);
        verify(transactionRepository).save(argThat(t ->
                t.getAmount().equals(BigDecimal.valueOf(40)) && t.getOperationType() == OperationType.WITHDRAW));
    }

    @Test
    void negativeWithdrawFundsNotEnough() {
        KafkaWithdrawEvent event = new KafkaWithdrawEvent(walletId, BigDecimal.valueOf(150));

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));

        WrongOperationException ex = assertThrows(WrongOperationException.class,
                () -> processingService.withdrawFunds(event));
        assertTrue(ex.getMessage().contains("Недостаточно средств"));

        verify(walletRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void negativeDepositWalletNotFound() {
        KafkaDepositEvent event = new KafkaDepositEvent(walletId, BigDecimal.TEN);

        when(walletRepository.findById(walletId)).thenReturn(Optional.empty());

        WalletNotFoundException ex = assertThrows(WalletNotFoundException.class,
                () -> processingService.depositFunds(event));
        assertTrue(ex.getMessage().contains("не найден"));

        verify(walletRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void negativeWithdrawFundsWalletNotFound() {
        KafkaWithdrawEvent event = new KafkaWithdrawEvent(walletId, BigDecimal.TEN);

        when(walletRepository.findById(walletId)).thenReturn(Optional.empty());

        WalletNotFoundException ex = assertThrows(WalletNotFoundException.class,
                () -> processingService.withdrawFunds(event));
        assertTrue(ex.getMessage().contains("не найден"));

        verify(walletRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }
}
