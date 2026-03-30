package com.blooddonation.service;

import com.blooddonation.model.User;
import com.blooddonation.repository.DonorRepository;
import com.blooddonation.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DonorRepository donorRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // First check admin/regular users
        var userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                    .map(r -> new SimpleGrantedAuthority(r.name()))
                    .collect(Collectors.toList());
            return new org.springframework.security.core.userdetails.User(
                    user.getEmail(), user.getPassword(), user.isActive(),
                    true, true, true, authorities);
        }

        // Then check donors
        var donorOpt = donorRepository.findByEmail(email);
        if (donorOpt.isPresent()) {
            var donor = donorOpt.get();
            return new org.springframework.security.core.userdetails.User(
                    donor.getEmail(), donor.getPassword(), donor.isActive(),
                    true, true, true,
                    List.of(new SimpleGrantedAuthority("ROLE_DONOR")));
        }

        throw new UsernameNotFoundException("User not found with email: " + email);
    }
}
