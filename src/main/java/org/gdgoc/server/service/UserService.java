package org.gdgoc.server.service;

import java.util.List;
import java.util.Optional;
import org.gdgoc.server.domain.User;
import org.gdgoc.server.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long signid) {
        return userRepository.findById(signid);
    }

    public User createUser(User user) {
        return userRepository.save(user);
    }

    public User updateUser(Long signid, User userDetails) {
        User user = userRepository.findById(signid)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + signid));

        // 기존 정보를 업데이트
        user.setName(userDetails.getName());
        user.setGender(userDetails.getGender());
        user.setAge(userDetails.getAge());
        user.setBirth(userDetails.getBirth());
        user.setSignid(userDetails.getSignid());
        user.setPassword(userDetails.getPassword());
        user.setPhone(userDetails.getPhone());
        user.setEmail(userDetails.getEmail());


        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        userRepository.delete(user);
    }

    public User findByIdAndPw(String signid, String password) {
        return userRepository.findBySignidAndPassword(signid, password)
            .orElse(null); // 사용자 없으면 null 반환
    }
}

