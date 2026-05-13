package work.trade.wallet.mapper;

import org.mapstruct.Mapper;
import work.trade.wallet.domain.AccountRecord;
import work.trade.wallet.domain.AccountRecordType;
import work.trade.wallet.domain.Wallet;
import work.trade.wallet.dto.response.AccountRecordDto;
import work.trade.wallet.dto.response.AccountRecordTypeDto;
import work.trade.wallet.dto.response.WalletDto;

@Mapper(componentModel = "spring")
public interface WalletMapper {

//Entity -> Response
//-------------------------------------//
    WalletDto toDto(Wallet wallet);
    AccountRecordDto toDto(AccountRecord record);
    AccountRecordTypeDto toDto(AccountRecordType recordType);
}
