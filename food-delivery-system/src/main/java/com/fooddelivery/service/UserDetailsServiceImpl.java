package com.fooddelivery.service;

import com.fooddelivery.model.User;
import com.fooddelivery.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;

/**
 * Custom UserDetailsService implementation for Spring Security
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        
        return UserPrincipal.create(user);
    }
    
    @Transactional
    public UserDetails loadUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));
        
        return UserPrincipal.create(user);
    }
    
    /**
     * Custom UserDetails implementation
     */
    public static class UserPrincipal implements UserDetails {
        private Long id;
        private String firstName;
        private String lastName;
        private String email;
        private String password;
        private Collection<? extends GrantedAuthority> authorities;
        private boolean isActive;
        private boolean isEmailVerified;
        
        public UserPrincipal(Long id, String firstName, String lastName, String email, String password,
                           Collection<? extends GrantedAuthority> authorities, boolean isActive, boolean isEmailVerified) {
            this.id = id;
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
            this.password = password;
            this.authorities = authorities;
            this.isActive = isActive;
            this.isEmailVerified = isEmailVerified;
        }
        
        public static UserPrincipal create(User user) {
            Collection<GrantedAuthority> authorities = Collections.singletonList(
                    new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
            );
            
            return new UserPrincipal(
                    user.getId(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getEmail(),
                    user.getPassword(),
                    authorities,
                    user.getIsActive(),
                    user.getIsEmailVerified()
            );
        }
        
        public Long getId() {
            return id;
        }
        
        public String getFirstName() {
            return firstName;
        }
        
        public String getLastName() {
            return lastName;
        }
        
        public String getEmail() {
            return email;
        }
        
        @Override
        public String getUsername() {
            return email;
        }
        
        @Override
        public String getPassword() {
            return password;
        }
        
        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return authorities;
        }
        
        @Override
        public boolean isAccountNonExpired() {
            return true;
        }
        
        @Override
        public boolean isAccountNonLocked() {
            return isActive;
        }
        
        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }
        
        @Override
        public boolean isEnabled() {
            return isActive && isEmailVerified;
        }
    }
}