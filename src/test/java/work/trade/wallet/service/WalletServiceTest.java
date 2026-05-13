package work.trade.wallet.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import work.trade.user.domain.User;
import work.trade.wallet.domain.AccountRecord;
import work.trade.wallet.domain.AccountRecordType;
import work.trade.wallet.domain.Wallet;
import work.trade.wallet.domain.constant.AccountRecordTypeConstant;
import work.trade.wallet.exception.WalletNotFoundException;
import work.trade.wallet.mapper.WalletMapper;
import work.trade.wallet.repository.AccountRecordRepository;
import work.trade.wallet.repository.AccountRecordTypeRepository;
import work.trade.wallet.repository.WalletRepository;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    private WalletRepository walletRepository;
    @Mock private AccountRecordRepository accountRecordRepository;
    @Mock private AccountRecordTypeRepository accountRecordTypeRepository;
    @Mock private WalletMapper mapper;

    @InjectMocks
    private WalletService walletService;

    private User user;
    private Wallet wallet;
    private Long userId = 1L;

//*******************************//

    @BeforeEach
    void Init() {
        user = User.builder().email("test@test.com").build();
        ReflectionTestUtils.setField(user, "id", userId);
        // 초기 잔액 10,000원 설정
        wallet = Wallet.builder()
                .user(user)
                .build();
        wallet.deposit(new BigDecimal(10000));
    }

    private static AccountRecordType getAccountRecordType(String recordType) {
        return AccountRecordType.builder()
                .code(recordType)
                .name("name").description("desc").build();
    }

//*******************************//

    @Test
    @DisplayName("잔고 차감(결제) 성공 - 지갑 잔액이 줄고 기록이 남아야 한다")
    void deduct_success() {
        // given
        BigDecimal amount = new BigDecimal("3000");
        AccountRecordType paymentType = getAccountRecordType(AccountRecordTypeConstant.PAYMENT);


        given(walletRepository.findByUser_Id(userId)).willReturn(Optional.of(wallet));
        given(accountRecordTypeRepository.findById(AccountRecordTypeConstant.PAYMENT))
                .willReturn(Optional.of(paymentType));

        // when
        walletService.deduct(userId, amount);

        // then
        assertThat(wallet.getBalance()).isEqualByComparingTo(new BigDecimal("7000"));
        verify(accountRecordRepository, times(1)).save(any(AccountRecord.class));
    }

    @Test
    @DisplayName("잔고 증가(판매) 성공 - 지갑 잔액이 늘고 기록이 남아야 한다")
    void deposit_success() {
        // given
        BigDecimal amount = new BigDecimal("5000");
        AccountRecordType saleType = getAccountRecordType(AccountRecordTypeConstant.SALE);

        given(walletRepository.findByUser_Id(userId)).willReturn(Optional.of(wallet));
        given(accountRecordTypeRepository.findById(AccountRecordTypeConstant.SALE))
                .willReturn(Optional.of(saleType));

        // when
        walletService.deposit(userId, amount);

        // then
        assertThat(wallet.getBalance()).isEqualByComparingTo(new BigDecimal("15000"));
        verify(accountRecordRepository, times(1)).save(any(AccountRecord.class));
    }

    @Test
    @DisplayName("계좌 출금 성공 - 잔액이 줄고 WITHDRAWAL 타입으로 기록되어야 한다")
    void payout_success() {
        // given
        BigDecimal amount = new BigDecimal("10000");
        AccountRecordType withdrawalType = getAccountRecordType(AccountRecordTypeConstant.WITHDRAWAL);

        given(walletRepository.findByUser_Id(userId)).willReturn(Optional.of(wallet));
        given(accountRecordTypeRepository.findById(AccountRecordTypeConstant.WITHDRAWAL))
                .willReturn(Optional.of(withdrawalType));

        // when
        walletService.payout(userId, amount);

        // then
        assertThat(wallet.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        verify(accountRecordRepository, times(1)).save(any(AccountRecord.class));
    }

    @Test
    @DisplayName("지갑이 없는 사용자의 경우 예외가 발생한다")
    void getWallet_fail_notFound() {
        // given
        given(walletRepository.findByUser_Id(userId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> walletService.deduct(userId, new BigDecimal("100")))
                .isInstanceOf(WalletNotFoundException.class);
    }

    @Test
    @DisplayName("환불로 인한 잔고 증가 시 REFUND 타입으로 기록되어야 한다")
    void depositByRefund_success() {
        // given
        BigDecimal amount = new BigDecimal("2000");
        AccountRecordType refundType = getAccountRecordType(AccountRecordTypeConstant.REFUND_TO_BUYER);

        given(walletRepository.findByUser_Id(userId)).willReturn(Optional.of(wallet));
        given(accountRecordTypeRepository.findById(AccountRecordTypeConstant.REFUND_TO_BUYER))
                .willReturn(Optional.of(refundType));

        // when
        walletService.depositByRefund(userId, amount);

        // then
        assertThat(wallet.getBalance()).isEqualByComparingTo(new BigDecimal("12000"));
        // 기록이 REFUND 타입인지 확인하는 로직이 서비스에 잘 녹아있는지 검증
        verify(accountRecordTypeRepository).findById(AccountRecordTypeConstant.REFUND_TO_BUYER);
    }
}