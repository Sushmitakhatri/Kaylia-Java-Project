package com.app.Kaylia.services;

import com.app.Kaylia.dto.LoginDto;
import com.app.Kaylia.model.User;
import com.app.Kaylia.repository.UserRepo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthServices {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private AuthenticationManager authenticationManager;

    public String registerUser(User user) {
        try{
            Optional<User> existingUser = userRepo.findUserByEmail(user.getEmail());
            if(existingUser.isPresent()){
                throw new RuntimeException("existing user");
            }
        }catch (Exception e){
            return "Email Already in Use!";
        }

        User userToRegister = new User();
        //set name and email
        userToRegister.setFName(user.getFName());
        userToRegister.setLName(user.getLName());
        userToRegister.setEmail(user.getEmail());
        userToRegister.setRole("ROLE_USER");

        //encrypt and set password
        BCryptPasswordEncoder bCrypt = new BCryptPasswordEncoder();
        userToRegister.setPassword(bCrypt.encode(user.getPassword()));

        try{
            userRepo.save(userToRegister);
            return "Successful";
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed To Register User";
        }

    }

    public String verifyUser(LoginDto loginDto, HttpServletRequest request) {
        try {
            // Only authenticate once
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword())
            );

            if(authentication.isAuthenticated()) {
                // Set SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authentication);

                // Start session and store email
                HttpSession session = request.getSession(true);
                session.setAttribute("email", loginDto.getEmail());
                session.setAttribute(
                        HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                        SecurityContextHolder.getContext()
                );

                return authentication.getAuthorities().iterator().next().getAuthority();
            }
        } catch (AuthenticationException e) {
            System.out.println("Authentication failed: " + e.getMessage());
            return null;
        }

        return null;
    }
}
