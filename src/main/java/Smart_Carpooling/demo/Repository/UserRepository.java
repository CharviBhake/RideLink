package Smart_Carpooling.demo.Repository;

import Smart_Carpooling.demo.Entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends MongoRepository<User,String> {
   User findByUsername(String username);
    void deleteByUsername(String username);
    User findByIdIn(String userIds);
    List<User> findByIdIn(List<String> userId);
  //  List<User> findByUsername(List<String> usernames);
}
