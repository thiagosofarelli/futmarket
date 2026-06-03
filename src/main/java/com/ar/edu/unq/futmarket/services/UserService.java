package com.ar.edu.unq.futmarket.services;

import com.ar.edu.unq.futmarket.model.User;

public interface UserService {

    User findById(Long id);

    User findByUsername(String username);

    User findSuperuser();
}
