package system.integration.mainserver.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Login {
    private String email;
    private String password;
}
