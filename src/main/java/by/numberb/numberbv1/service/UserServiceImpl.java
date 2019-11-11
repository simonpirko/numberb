package by.numberb.numberbv1.service;

import by.numberb.numberbv1.domain.User;
import by.numberb.numberbv1.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;

  public UserServiceImpl(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public void createUser(User user) {
    userRepository.save(user);
  }

  @Override
  public List<User> findAll() {
    return userRepository.findAll();
  }

  @Override
  public User findByChatId(Long id) {
    return userRepository.findByChatId(id);
  }

  @Override
  public boolean contains(Integer integer) {
    return userRepository.existsUserByClientId(integer);
  }

  @Override
  public void updateScore(Long id, Integer integer) {
    User userByChatId = userRepository.getUserByChatId(id);
    userByChatId.setScore(integer);
    userRepository.save(userByChatId);
  }
}
