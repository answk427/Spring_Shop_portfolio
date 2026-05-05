package work.trade.wallet.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import work.trade.order.domain.Order;
import work.trade.order.domain.OrderItem;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public void onUserCreated(UserCreatedEvent event) {
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

    private Map<User, BigDecimal> getSellerAmount(Order order) {
        Map<User, BigDecimal> sellerRefunds = new HashMap<>();

        for (OrderItem orderItem : order.getOrderItems()) {
            User seller = orderItem.getProduct().getSeller();
            BigDecimal amount = orderItem.getSubtotalPrice();

            sellerRefunds.merge(seller, amount, BigDecimal::add);
        }
        return sellerRefunds;
    }

//----------------------------//

    public void depositFromOrderDelivery(Order order) {
        log.info("판매 완료 시 판매자에게 돈 입금 시작");

        // 판매자별로 따로 계산
        Map<User, BigDecimal> sellerAmounts = getSellerAmount(order);

        // 각 판매자에게 입금
        List<AccountRecord> accountRecords = new ArrayList<AccountRecord>();
        AccountRecordType accountRecordType = accountRecordTypeRepository
                .findById(AccountRecordTypeConstant.SALE)
                .orElseThrow(()-> new AccountRecordTypeNotFoundException());

        for (Map.Entry<User, BigDecimal> entry : sellerAmounts.entrySet()) {
            User seller = entry.getKey();
            BigDecimal amount = entry.getValue();

            Wallet wallet = walletRepository.findByUser_Id(seller.getId())
                    .orElseThrow();
            wallet.depositFromSale(amount);
            log.info("판매자ID:{}에게 {}원 입금", seller.getId(), amount);

            AccountRecord record = AccountRecord.builder()
                    .wallet(wallet)
                    .type(accountRecordType)
                    .amount(amount)
                    .build();

            accountRecords.add(record);
        }

        accountRecordRepository.saveAll(accountRecords);
        log.info("판매 완료 시 판매자에게 돈 입금 완료");
    }

    @Transactional
    public void refundForCancelledOrder(Order order) {
        log.info("주문 취소 시 판매자에게 돈 차감 시작");

        // 판매자별로 금액 합산
        Map<User, BigDecimal> sellerRefunds = getSellerAmount(order);

        AccountRecordType refundType = accountRecordTypeRepository
                .findById(AccountRecordTypeConstant.REFUND)
                .orElseThrow(() -> new AccountRecordTypeNotFoundException());

        // 판매자별로 처리
        List<AccountRecord> accountRecords = new ArrayList<>();

        for (Map.Entry<User, BigDecimal> entry : sellerRefunds.entrySet()) {
            User seller = entry.getKey();
            BigDecimal totalRefund = entry.getValue();

            Wallet sellerWallet = walletRepository.findByUser_Id(seller.getId())
                    .orElseThrow(() -> new WalletNotFoundException(seller.getId()));

            sellerWallet.withdraw(totalRefund);
            log.info("판매자[{}]에게서 {}원 환불됨", seller.getId(), totalRefund);

            // 환불 기록
            AccountRecord record = AccountRecord.builder()
                    .wallet(sellerWallet)
                    .type(refundType)
                    .amount(totalRefund)
                    .build();

            accountRecords.add(record);
        }

        accountRecordRepository.saveAll(accountRecords);

        log.info("주문 취소 시 판매자에게 돈 차감 완료");
    }
}

