package com.codingshuttle.razorpay.vault_service.controller;

import com.codingshuttle.razorpay.vault_service.service.VaultService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/vault")
public class InternalVaultController {

    private final VaultService vaultService;


}
