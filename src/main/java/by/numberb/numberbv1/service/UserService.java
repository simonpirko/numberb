package by.numberb.numberbv1.service;

import by.numberb.numberbv1.domain.User;

import java.util.List;

public interface UserService {
  void createUser(User user);

  List<User> findAll();

  User findByChatId(Long id);

  boolean contains(Integer integer);

  void updateScore(Long id, Integer integer);
}
