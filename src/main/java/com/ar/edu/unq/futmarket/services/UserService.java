package com.ar.edu.unq.futmarket.services;

import com.ar.edu.unq.futmarket.exception.UserNotFoundException;
import com.ar.edu.unq.futmarket.model.User;
import com.ar.edu.unq.futmarket.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(UserNotFoundException::new);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(UserNotFoundException::new);
    }

    public User findSuperuser() {
        return userRepository.findBySuperuserTrue()
                .orElseThrow(UserNotFoundException::new);
    }
}
