package walletrest.service;

import walletrest.entity.Wallet;
import walletrest.entity.dto.BalanceResponseDto;
import walletrest.entity.dto.OperationRequest;
import walletrest.entity.enums.OperationType;
import walletrest.event.KafkaDepositEvent;
import walletrest.event.KafkaWithdrawEvent;
import walletrest.exception.DataValidationException;
import walletrest.exception.WalletNotFoundException;
import walletrest.producer.KafkaDepositProducer;
import walletrest.producer.KafkaWithdrawProducer;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletOperationsServiceTest {

    @Mock
    private KafkaWithdrawProducer kafkaWithdrawProducer;

    @Mock
    private KafkaDepositProducer kafkaDepositProducer;

    @Mock
    private WalletRepository walletRepository;

    @InjectMocks
    private WalletOperationsService walletOperationsService;

    private UUID walletId;
    private Wallet wallet;

    @BeforeEach
    void setUp() {
        walletId = UUID.randomUUID();
        wallet = new Wallet();
        wallet.setId(walletId);
        wallet.setBalance(BigDecimal.valueOf(100));
    }

    @Test
    void positiveWithdrawFunds() {
        OperationRequest request = new OperationRequest(walletId, OperationType.WITHDRAW, BigDecimal.valueOf(50));

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        doNothing().when(kafkaWithdrawProducer).sendWithdrawEvent(any(KafkaWithdrawEvent.class));

        walletOperationsService.submitOperation(request);

        verify(walletRepository).findById(walletId);
        verify(kafkaWithdrawProducer).sendWithdrawEvent(argThat(event ->
                event.walletId().equals(walletId) &&
                        event.amount().equals(BigDecimal.valueOf(50))
        ));
        verifyNoInteractions(kafkaDepositProducer);
    }

    @Test
    void negativeSubmitWithdrawNotEnoughFunds() {
        OperationRequest request = new OperationRequest(walletId, OperationType.WITHDRAW, BigDecimal.valueOf(150));

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));

        DataValidationException ex = assertThrows(DataValidationException.class,
                () -> walletOperationsService.submitOperation(request));
        assertTrue(ex.getMessage().contains("недостаточно средств"));

        verify(walletRepository).findById(walletId);
        verifyNoInteractions(kafkaWithdrawProducer);
        verifyNoInteractions(kafkaDepositProducer);
    }

    @Test
    void positiveSubmitDeposit() {
        OperationRequest request = new OperationRequest(walletId, OperationType.DEPOSIT, BigDecimal.valueOf(75));

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        doNothing().when(kafkaDepositProducer).sendDepositEvent(any(KafkaDepositEvent.class));

        walletOperationsService.submitOperation(request);

        verify(walletRepository).findById(walletId);
        verify(kafkaDepositProducer).sendDepositEvent(argThat(event ->
                event.walletId().equals(walletId) &&
                        event.amount().equals(BigDecimal.valueOf(75))
        ));
        verifyNoInteractions(kafkaWithdrawProducer);
    }

    @Test
    void negativeSubmitOperationTypeNull() {
        OperationRequest request = new OperationRequest(walletId, null, BigDecimal.valueOf(10));

        DataValidationException ex = assertThrows(DataValidationException.class,
                () -> walletOperationsService.submitOperation(request));
        assertTrue(ex.getMessage().contains("не может быть null"));

        verifyNoInteractions(kafkaWithdrawProducer);
        verifyNoInteractions(kafkaDepositProducer);
    }

    @Test
    void positiveGetWalletBalance() {
        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));

        BalanceResponseDto balanceResponse = walletOperationsService.getWalletBalance(walletId);

        assertEquals(walletId, balanceResponse.walletId());
        assertEquals(wallet.getBalance(), balanceResponse.balance());
        verify(walletRepository).findById(walletId);
    }

    @Test
    void negativeGetBalanceWalletNotFound() {
        when(walletRepository.findById(walletId)).thenReturn(Optional.empty());

        WalletNotFoundException ex = assertThrows(WalletNotFoundException.class,
                () -> walletOperationsService.getWalletBalance(walletId));
        assertTrue(ex.getMessage().contains("не найден"));

        verify(walletRepository).findById(walletId);
    }
}
