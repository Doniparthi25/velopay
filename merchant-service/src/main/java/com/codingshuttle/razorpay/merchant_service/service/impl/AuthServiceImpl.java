package com.codingshuttle.razorpay.merchant_service.service.impl;

import com.codingshuttle.razorpay.*;
import com.codingshuttle.razorpay.common_lib.enums.MerchantStatus;
import com.codingshuttle.razorpay.common_lib.enums.UserRole;
import com.codingshuttle.razorpay.common_lib.exceptions.DuplicateResourceException;
import com.codingshuttle.razorpay.common_lib.exceptions.ResourceNotFoundException;
import com.codingshuttle.razorpay.merchant_service.dto.request.LoginRequest;
import com.codingshuttle.razorpay.merchant_service.dto.request.MerchantSignupRequest;
import com.codingshuttle.razorpay.merchant_service.dto.response.LoginResponse;
import com.codingshuttle.razorpay.merchant_service.dto.response.MerchantResponse;
import com.codingshuttle.razorpay.merchant_service.entity.AppUser;
import com.codingshuttle.razorpay.merchant_service.entity.Merchant;
import com.codingshuttle.razorpay.merchant_service.mapper.MerchantMapper;
import com.codingshuttle.razorpay.merchant_service.repository.AppUserRepository;
import com.codingshuttle.razorpay.merchant_service.repository.MerchantRepository;
import com.codingshuttle.razorpay.merchant_service.security.JwtUtil;
import com.codingshuttle.razorpay.merchant_service.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final MerchantRepository merchantRepository;
    private final AppUserRepository appUserRepository;
    private final MerchantMapper merchantMapper;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public MerchantResponse signup(MerchantSignupRequest request) {
        if (merchantRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("DUPLICATE_MERCHANT_EMAIL", "merchant with email already exists: " + request.email());
        }

        Merchant merchant = merchantMapper.toEntityFromSignUpRequest(request);
        merchant.setStatus(MerchantStatus.PENDING_KYC);
        merchant = merchantRepository.save(merchant);

        AppUser appUser = AppUser.builder()
                .email(request.email())
                .merchant(merchant)
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(UserRole.OWNER)
                .build();
        appUserRepository.save(appUser);

        return merchantMapper.toResponse(merchant);
    }

    @Override
    public LoginResponse login(LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(),request.password())
        );

        AppUser appUser = appUserRepository.findByEmail(request.email())
                .orElseThrow(()-> new ResourceNotFoundException("User",request.email()));

        String token = jwtUtil.generateAccessToken(request.email(), appUser.getMerchant().getId(),appUser.getRole().toString());

        return new LoginResponse(token);
    }
}
