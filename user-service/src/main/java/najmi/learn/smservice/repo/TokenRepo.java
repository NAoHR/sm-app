package najmi.learn.smservice.repo;

import najmi.learn.smservice.entity.TokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Repository
public interface TokenRepo extends JpaRepository<TokenEntity, Integer> {
    @Query("SELECT t FROM TokenEntity t INNER JOIN UserEntity u ON t.user.id = u.id WHERE u.id=:userId AND t.expired = false AND t.revoked = false")
    List<TokenEntity> findAllValidTokenByUser(UUID userId);

    Optional<TokenEntity> findByToken(String token);
}
