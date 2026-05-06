package work.trade.wallet.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import work.trade.user.domain.User;
import work.trade.user.event.UserCreatedEvent;
import work.trade.user.exception.UserNotFoundException;
import work.trade.wallet.domain.AccountRecord;
import work.trade.wallet.domain.AccountRecordType;
import work.trade.wallet.domain.Wallet;
import work.trade.wallet.domain.constant.AccountRecordTypeConstant;
import work.trade.wallet.dto.response.WalletDto;
import work.trade.wallet.exception.AccountRecordTypeNotFoundException;
import work.trade.wallet.exception.WalletDuplicateException;
import work.trade.wallet.exception.WalletNotFoundException;
import work.trade.wallet.mapper.WalletMapper;
import work.trade.wallet.repository.AccountRecordRepository;
import work.trade.wallet.repository.AccountRecordTypeRepository;
import work.trade.wallet.repository.WalletRepository;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class WalletService {

    private final WalletRepository walletRepository;
    private final AccountRecordRepository accountRecordRepository;
    private final AccountRecordTypeRepository accountRecordTypeRepository;

    private final WalletMapper mapper;

//----------------------------//

    @EventListener
    public void onUserCreatedEvent(UserCreatedEvent event) {
        log.info("UserCreated Event 수신 : WalletService");

        User user = event.user();
        if (user == null) {
            throw new UserNotFoundException();
        }

        createWallet(user);
    }


    private WalletDto createWallet(User user) {
        log.info("createWallet userId: {}", user.getId());

        try {
            Wallet wallet = Wallet.builder().user(user).build();
            Wallet savedWallet = walletRepository.save(wallet);
            return mapper.toDto(savedWallet);
        } catch (DataIntegrityViolationException e) {
            throw new WalletDuplicateException();
        } catch (Exception e) {
            log.error("createWallet DB Exception", e);
            throw e;
        }
    }

    private Wallet getWallet(Long userId) {
        return walletRepository.findByUser_Id(userId).
                orElseThrow(() -> new WalletNotFoundException(userId));
    }

    private AccountRecordType getAccountRecordType(String recordType) {
        return accountRecordTypeRepository
                .findById(recordType)
                .orElseThrow(() -> new AccountRecordTypeNotFoundException());
    }

    private AccountRecord saveAccountRecord(String recordType, BigDecimal amount, Wallet wallet) {
        AccountRecordType accountRecordType = getAccountRecordType(recordType);

        AccountRecord record = AccountRecord.builder()
                .wallet(wallet)
                .type(accountRecordType)
                .amount(amount)
                .build();
        return accountRecordRepository.save(record);
    }
//----------------------------//

    // 잔고 차감
    public void deduct(Long userId, BigDecimal amount) {
        log.info("userId:{}, 잔고 차감 시작 amount:{}", userId, amount);

        Wallet wallet = getWallet(userId);
        wallet.deduct(amount);

        saveAccountRecord(AccountRecordTypeConstant.PAYMENT, amount, wallet);

        log.info("userId:{}, 잔고 차감 완료 amount:{}", userId, amount);
    }

    // 판매상품 환불로 인한 판매자 잔고 차감
    public void deductByRefund(Long userId, BigDecimal amount) {
        log.info("userId:{}, 환불 잔고 차감 시작 amount:{}", userId, amount);

        Wallet wallet = getWallet(userId);
        wallet.deductByRefund(amount);

        saveAccountRecord(AccountRecordTypeConstant.REFUND, amount, wallet);

        log.info("userId:{}, 환불 잔고 차감 완료 amount:{}", userId, amount);
    }

    // 잔고 증가
    public void deposit(Long userId, BigDecimal amount) {
        log.info("userId:{}, 잔고 증가 시작 amount:{}", userId, amount);

        Wallet wallet = getWallet(userId);
        wallet.deposit(amount);

        saveAccountRecord(AccountRecordTypeConstant.SALE, amount, wallet);

        log.info("userId:{}, 잔고 증가 완료 amount:{}", userId, amount);
    }

    // 판매상품 환불로 인한 구매자 잔고 증가
    public void depositByRefund(Long userId, BigDecimal amount) {
        log.info("userId:{}, 환불 잔고 증가 시작 amount:{}", userId, amount);

        Wallet wallet = getWallet(userId);
        wallet.deposit(amount);

        saveAccountRecord(AccountRecordTypeConstant.REFUND, amount, wallet);

        log.info("userId:{}, 환불 잔고 증가 완료 amount:{}", userId, amount);
    }

    // 계좌로 출금
    public void payout(Long userId, BigDecimal amount) {
        log.info("userId:{}, 계좌로 출금 시작 amount:{}", userId, amount);

        Wallet wallet = getWallet(userId);
        wallet.deduct(amount);

        saveAccountRecord(AccountRecordTypeConstant.WITHDRAWAL, amount, wallet);

        log.info("userId:{}, 계좌로 출금 완료 amount:{}", userId, amount);
    }


    @Transactional(readOnly = true)
    public BigDecimal getBalance(Long userId) {
        Wallet wallet = getWallet(userId);
        return wallet.getBalance();
    }
}

