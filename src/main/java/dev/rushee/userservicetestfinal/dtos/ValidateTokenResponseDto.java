package dev.rushee.userservicetestfinal.dtos;

import dev.rushee.userservicetestfinal.models.SessionStatus;
import io.jsonwebtoken.Claims;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

@Getter
@Setter
@ToString
public class ValidateTokenResponseDto {
    private Map<String,Object> claims;

    public static ValidateTokenResponseDto from(Claims claims) {
        ValidateTokenResponseDto validateTokenResponseDto = new ValidateTokenResponseDto();
        validateTokenResponseDto.setClaims(claims);
        return validateTokenResponseDto;
    }
}
