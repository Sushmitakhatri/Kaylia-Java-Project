package com.app.Kaylia.services;

import com.app.Kaylia.model.User;
import com.app.Kaylia.model.UserPrincipal;
import com.app.Kaylia.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MyUserDetailService implements UserDetailsService {

    @Autowired
    private UserRepo userRepo;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<User> isUser = userRepo.findUserByEmail(email);
        if(isUser.isEmpty()){
            System.out.println("user not found");
            throw new UsernameNotFoundException("User not found");
        }else {
            User foundUser = isUser.get();
            return new UserPrincipal(foundUser);
        }
    }
}

