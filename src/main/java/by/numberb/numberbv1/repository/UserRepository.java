package by.numberb.numberbv1.repository;

import by.numberb.numberbv1.domain.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends MongoRepository<User, Long> {
  User findByChatId(Long id);

  boolean existsUserByClientId(Integer integer);

  User getUserByChatId(Long id);
}
