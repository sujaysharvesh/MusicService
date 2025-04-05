package MusicCloud.example.MusicService.Music.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailsDTO {
    private UUID id;
    private String email;
    private String name;
    private List<Authority> authorities;
    private Map<String, Object> attributes;
    private String provider;

    // Getters and setters

    public static class Authority {
        private String authority;

        public Authority() {}

        public String getAuthority() {
            return authority;
        }

        public void setAuthority(String authority) {
            this.authority = authority;
        }
    }
}
