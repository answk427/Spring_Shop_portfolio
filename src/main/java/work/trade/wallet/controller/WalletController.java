package work.trade.wallet.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import work.trade.wallet.dto.response.AccountRecordDto;
import work.trade.wallet.dto.response.WalletDto;
import work.trade.wallet.service.WalletService;

import java.math.BigDecimal;
import java.util.Map;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    private final WalletService walletService;

    @GetMapping
    public ResponseEntity<WalletDto> getWallet(
            Authentication authentication
    ) {
        Long userId = Long.parseLong(authentication.getName());
        return ResponseEntity.ok(walletService.findWallet(userId));
    }

    @GetMapping("/records")
    public ResponseEntity<Page<AccountRecordDto>> getAccountRecords(
            Authentication authentication,
            Pageable pageable
    ) {
        Long userId = Long.parseLong(authentication.getName());
        return ResponseEntity.ok(walletService.findAccountRecords(userId, pageable));
    }


//Test용*********************//
    @PostMapping("/test/add")
    public ResponseEntity<Map<String, String>> addMoney(
            Authentication authentication)
    {
        Long userId = Long.parseLong(authentication.getName());
        walletService.testIncreaseAmount(userId, BigDecimal.valueOf(100000L));

        return ResponseEntity.ok(
                Map.of("message", "테스트용 잔액 100000 증가"));
    }
}
