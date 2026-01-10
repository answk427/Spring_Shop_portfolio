package work.trade.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import work.trade.user.domain.User;

@Getter
@Setter
@NoArgsConstructor
public class SellerDto {
    private Long id;
    private String email;
    private String name;

    public static SellerDto from(User user) {
        SellerDto sellerDto = new SellerDto();
        sellerDto.setId(user.getId());
        sellerDto.setName(user.getName());
        sellerDto.setEmail(user.getEmail());
        return sellerDto;
    }
}
