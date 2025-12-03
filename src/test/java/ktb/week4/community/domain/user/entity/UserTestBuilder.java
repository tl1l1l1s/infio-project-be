package ktb.week4.community.domain.user.entity;
import java.util.UUID;

public class UserTestBuilder {

    private String nickname = "User-" + UUID.randomUUID();
    private String password = "Aa#12345";
    private String email = "user@test.t";
    private String profileImage = "/upload/test.png";

    private UserTestBuilder() {
    }

    public static UserTestBuilder aUser() {
        return new UserTestBuilder();
    }

    public UserTestBuilder withNickname(String nickname) {
        this.nickname = nickname;
        return this;
    }

    public UserTestBuilder withPassword(String password) {
        this.password = password;
        return this;
    }

    public UserTestBuilder withEmail(String email) {
        this.email = email;
        return this;
    }

    public UserTestBuilder withProfileImage(String profileImage) {
        this.profileImage = profileImage;
        return this;
    }

    public User build() {
        return new User(nickname, password, email, profileImage);
    }
}
