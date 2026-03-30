package com.blooddonation.service;

import com.blooddonation.config.JwtUtils;
import com.blooddonation.dto.AuthDTO;
import com.blooddonation.dto.DonorDTO;
import com.blooddonation.model.Donor;
import com.blooddonation.model.User;
import com.blooddonation.repository.DonorRepository;
import com.blooddonation.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthService {

    @Autowired private AuthenticationManager authManager;
    @Autowired private JwtUtils jwtUtils;
    @Autowired private UserRepository userRepository;
    @Autowired private DonorRepository donorRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    public AuthDTO.AuthResponse login(AuthDTO.LoginRequest req) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(auth);
        String jwt = jwtUtils.generateJwtToken(auth);

        var userDetails = (org.springframework.security.core.userdetails.UserDetails) auth.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).collect(Collectors.toList());

        // Get name and id
        String name = req.getEmail();
        String id = req.getEmail();

        var userOpt = userRepository.findByEmail(req.getEmail());
        if (userOpt.isPresent()) {
            name = userOpt.get().getName();
            id = userOpt.get().getId();
        } else {
            var donorOpt = donorRepository.findByEmail(req.getEmail());
            if (donorOpt.isPresent()) {
                name = donorOpt.get().getName();
                id = donorOpt.get().getId();
            }
        }

        return new AuthDTO.AuthResponse(jwt, id, name, req.getEmail(), roles);
    }

    public AuthDTO.AuthResponse registerAdmin(AuthDTO.RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }
        User user = User.builder()
                .name(req.getName())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .phone(req.getPhone())
                .roles(List.of(User.Role.ROLE_ADMIN))
                .active(true)
                .build();
        userRepository.save(user);

        return login(new AuthDTO.LoginRequest() {{
            setEmail(req.getEmail());
            setPassword(req.getPassword()); // raw password for re-auth
        }});
    }
}
