package najmi.learn.postservice.repo;

import najmi.learn.postservice.entity.PostEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

@Repository
public interface PostRepo extends JpaRepository<PostEntity, Integer> {
}
