package com.profession.suggest.controllers.auth;

import com.profession.suggest.database.services.auth.AccountService;
import com.profession.suggest.dto.auth.AccountDTO;
import com.profession.suggest.dto.auth.RoleDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.security.auth.login.AccountNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {
    private final AccountService accountService;

    public AuthController(AccountService accountService) {
        this.accountService = accountService;
    }
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody AccountDTO account) {
        try {
            return ResponseEntity.ok(accountService.login(account));
        } catch (AccountNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("please check login or password");
        }

    }
    @GetMapping("/account-roles")
    public ResponseEntity<List<RoleDTO>> getAccountRoles(@RequestAttribute("accountId") Long accountId) throws AccountNotFoundException {
        try {
            return ResponseEntity.ok(accountService.getRolesByAccount(accountId).stream()
                    .map((r) -> new RoleDTO(r.getName()))
                    .collect(Collectors.toList()));
        } catch (AccountNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

    }
    @PostMapping("/update-password")
    public ResponseEntity<?> updatePassword(@RequestAttribute("accountId") Long accountId,
                                            @RequestBody AccountDTO accountDTO) {
        try {
            accountService.updatePassword(accountId, accountDTO.getPassword());
            return ResponseEntity.ok().body(Map.of("message", "Password updated successfully"));
        } catch (AccountNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Account not found"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error updating password for account {}", accountId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error occurred while updating password"));
        }
    }
    @GetMapping("/is-email-free")
    public ResponseEntity<Boolean> isEmailFree(@RequestParam String email ) {
        try {
            return ResponseEntity.ok(accountService.isEmailFree(email));
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
}
