package demo.keycloak.auth.forgotusername;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

public class UsernameLookupService {

    public UsernameLookupResponse lookupUsername(UsernameLookupRequest request) {

        UsernameLookupResponse response = new UsernameLookupResponse();

        if ("123".equals(request.getReferenceNumber())) {
            response.setErrorCode(null);
        } else if ("111".equals(request.getReferenceNumber())) {
            response.setErrorCode("lookup-error");
        }

        return response;
    }

    @Data
    public static class UsernameLookupRequest {

        String referenceNumber;

        String firstname;

        String lastname;

        String zipCode;

        String dateOfBirth;
    }

    @Data
    public static class UsernameLookupResponse {

        String errorCode;

        String username;
    }
}
