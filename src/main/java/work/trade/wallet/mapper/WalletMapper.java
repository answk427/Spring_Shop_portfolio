package work.trade.wallet.mapper;

import org.mapstruct.Mapper;
import work.trade.wallet.domain.Wallet;
import work.trade.wallet.dto.response.WalletDto;

@Mapper(componentModel = "spring")
public interface WalletMapper {

//Entity -> Response
//-------------------------------------//
    WalletDto toDto(Wallet wallet);
}
