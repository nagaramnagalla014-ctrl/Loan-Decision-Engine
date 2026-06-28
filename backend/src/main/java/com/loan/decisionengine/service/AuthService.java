package com.loan.decisionengine.service;

import com.loan.decisionengine.config.JwtTokenProvider;
import com.loan.decisionengine.dto.JwtResponse;
import com.loan.decisionengine.dto.LoginRequest;
import com.loan.decisionengine.dto.RegisterRequest;
import com.loan.decisionengine.exception.LoanDecisionException;
import com.loan.decisionengine.model.User;
import com.loan.decisionengine.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtTokenProvider tokenProvider;

    public JwtResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new LoanDecisionException("Invalid email or password"));

        if (!user.isActive()) {
            throw new LoanDecisionException("Account is deactivated");
        }
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new LoanDecisionException("Invalid email or password");
        }

        String token = tokenProvider.generateToken(user.getEmail());
        return new JwtResponse(token, user.getId(), user.getEmail(),
                user.getFirstName(), user.getLastName(), user.getRole().name());
    }

    public JwtResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new LoanDecisionException("Email already registered");
        }
        User user = new User();
        user.setEmail(req.getEmail());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setFirstName(req.getFirstName());
        user.setLastName(req.getLastName());
        user.setPhone(req.getPhone());
        user.setRole(User.Role.APPLICANT);
        userRepository.save(user);

        String token = tokenProvider.generateToken(user.getEmail());
        return new JwtResponse(token, user.getId(), user.getEmail(),
                user.getFirstName(), user.getLastName(), user.getRole().name());
    }
}
