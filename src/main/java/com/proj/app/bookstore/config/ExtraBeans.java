package com.proj.app.bookstore.config;

import com.proj.app.bookstore.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@Configuration
@RequiredArgsConstructor
public class ExtraBeans {

    private final UserRepository userRepository;

    @Bean
    public UserDetailsService userDetails(){
        return str -> userRepository.findByEmail(str)
                .orElseThrow(() -> new UsernameNotFoundException("User with this email doesn't exist"));
    }
}
