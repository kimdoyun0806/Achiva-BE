package unicon.Achiva.domain.organization.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import unicon.Achiva.global.common.LongBaseEntity;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Organization extends LongBaseEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    private String password;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    public boolean requiresPassword() {
        return password != null && !password.isBlank();
    }

    public boolean isPasswordValid(String inputPassword) {
        if (!requiresPassword()) {
            return true;
        }
        return password.equals(inputPassword);
    }
}
