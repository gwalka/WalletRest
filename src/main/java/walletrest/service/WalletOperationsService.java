package walletrest.service;

import walletrest.entity.Wallet;
import walletrest.entity.dto.BalanceResponseDto;
import walletrest.entity.dto.OperationRequest;
import walletrest.event.KafkaDepositEvent;
import walletrest.event.KafkaWithdrawEvent;
import walletrest.exception.DataValidationException;
import walletrest.exception.WalletNotFoundException;
import walletrest.producer.KafkaDepositProducer;
import walletrest.producer.KafkaWithdrawProducer;
import walletrest.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletOperationsService {

    private final KafkaWithdrawProducer kafkaWithdrawProducer;
    private final KafkaDepositProducer kafkaDepositProducer;

    private final WalletRepository walletRepository;


    public void submitOperation(OperationRequest request) {
        if (request.operationType() == null) {
            throw new DataValidationException("Тип операции не может быть null");
        }
        Wallet wallet = validateWallet(request.walletId());

        switch (request.operationType()) {
            case WITHDRAW -> {
                if (wallet.getBalance().compareTo(request.amount()) < 0) {
                    throw new DataValidationException("На кошельке %s недостаточно средств", wallet.getId());
                }
                try {
                    kafkaWithdrawProducer
                            .sendWithdrawEvent(KafkaWithdrawEvent.builder()
                                    .walletId(wallet.getId())
                                    .amount(request.amount())
                                    .build());
                } catch (Exception e) {
                    log.error("Ошибка отправки события Kafka", e);
                }
            }

            case DEPOSIT -> {

                try {
                    kafkaDepositProducer.sendDepositEvent(KafkaDepositEvent.builder()
                                    .amount(request.amount())
                                    .walletId(wallet.getId())
                                    .build());
                } catch (Exception e) {
                    log.error("Ошибка отправки события Kafka", e);
                }
            }

            default -> throw new DataValidationException(
                    String.format("Неизвестный тип операции: %s", request.operationType()));

        }
    }

    public BalanceResponseDto getWalletBalance(UUID walletId) {
        Wallet wallet = validateWallet(walletId);

        return BalanceResponseDto.builder()
                .balance(wallet.getBalance())
                .walletId(walletId)
                .build();
    }

    private Wallet validateWallet(UUID walletId) {
        return walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException(
                        String.format("Кошелек с %s не найден", walletId)));
    }


}
