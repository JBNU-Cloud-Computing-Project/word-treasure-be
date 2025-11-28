
package cloudcomputing.wordtreasure.controller.member.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class SignupRequest {
    private String email;
    private String nickName;
    private String password;
}
