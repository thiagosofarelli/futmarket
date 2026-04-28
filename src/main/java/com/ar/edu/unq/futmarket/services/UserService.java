package com.ar.edu.unq.futmarket.services;

import com.ar.edu.unq.futmarket.model.User;

public interface UserService {

    public User findById(Long id);

    public User findByUsername(String username);

    public User findSuperuser();
}
