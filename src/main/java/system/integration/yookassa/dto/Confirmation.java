package system.integration.yookassa.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Confirmation {
    @JsonProperty("confirmation_token")
    private String data;
    private String type;
}
