package com.gl.springbootdemo.service;

import com.gl.springbootdemo.entity.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserService implements UserDetailsService {

    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private Map<String, String> users = new ConcurrentHashMap<>();

    @Inject
    public UserService(BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        save("admin", "123");
    }

    public void save(String username, String password) {
        users.put(username, bCryptPasswordEncoder.encode(password));
    }

    public String  getPasswordByUsername(String username) {
        return users.get(username);
    }

    public User getUserById(Integer id) {
        return null;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if(!users.containsKey(username)) {
         throw  new UsernameNotFoundException(username + " 不存在 ");
        }
        String password = users.get(username);

        return new org.springframework.security.core.userdetails.User(
                username,
                password,
                Collections.emptyList()
        );


    }
}
