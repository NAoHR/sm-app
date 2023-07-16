package najmi.learn.smservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name="tokens")
public class TokenEntity {
    @Id
    @GeneratedValue
    private Integer id;
    private String token;
    private boolean revoked;
    private boolean expired;
    @Column(name = "token_type")
    private String tokenType;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

}
