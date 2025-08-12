package walletrest.controller;

import walletrest.entity.dto.BalanceResponseDto;
import walletrest.entity.dto.OperationRequest;
import walletrest.service.WalletOperationsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/wallet")
@RequiredArgsConstructor
@Tag(name = "Операции с кошельком", description = "API для пополнения, снятия и получения баланса кошелька")
public class WalletOperationsController {

    private final WalletOperationsService walletOperationsService;

    @Operation(
            summary = "Отправить операцию в обработку",
            description = """
            Принимает запрос на пополнение или снятие средств.
            Обработка происходит асинхронно, поэтому метод возвращает HTTP 202 (Accepted).
            """,
            responses = {
                    @ApiResponse(responseCode = "202", description = "Операция принята в обработку"),
                    @ApiResponse(responseCode = "400", description = "Ошибка валидации входных данных"),
                    @ApiResponse(responseCode = "404", description = "Кошелёк не найден")
            }
    )
    @PostMapping
    public ResponseEntity<Void> submitOperation(@Valid @RequestBody OperationRequest request) {
        walletOperationsService.submitOperation(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @Operation(
            summary = "Получить баланс кошелька",
            description = "Возвращает текущий баланс по ID кошелька",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Баланс успешно получен"),
                    @ApiResponse(responseCode = "404", description = "Кошелёк не найден")
            }
    )
    @GetMapping("/{walletId}")
    public ResponseEntity<BalanceResponseDto> getBalance(@Valid @PathVariable UUID walletId) {
        BalanceResponseDto response = walletOperationsService.getWalletBalance(walletId);
        return ResponseEntity.ok(response);
    }
}
