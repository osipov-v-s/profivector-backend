package com.profession.suggest.database.services.auth;

import com.profession.suggest.database.entities.auth.Account;
import com.profession.suggest.database.entities.auth.role.Role;
import com.profession.suggest.database.entities.auth.role.RoleEnum;
import com.profession.suggest.database.repositories.auth.AccountRepository;
import com.profession.suggest.database.services.auth.role.RoleService;
import com.profession.suggest.dto.auth.AccountDTO;
import com.profession.suggest.dto.auth.AccountRegisterRequestDTO;
import com.profession.suggest.services.jwt.JWTService;
import org.apache.coyote.BadRequestException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.security.auth.login.AccountNotFoundException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
public class AccountService {
    private final AccountRepository repository;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;
    private final JWTService jwtService;
    public AccountService(AccountRepository repository, RoleService roleService, PasswordEncoder passwordEncoder, JWTService jwtService) {
        this.repository = repository;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }
    public Account createAccount(Account account) {
        return repository.save(account);
    }
    public Boolean isEmailFree(String email) {
        return repository.findByEmail(email) == null;
    }
    @Transactional
    public String login (AccountDTO accountDTO) throws AccountNotFoundException {
        Account account = repository.findByEmail(accountDTO.getEmail().toLowerCase());
        if (account == null)
            throw new AccountNotFoundException("Email or password incorrect");
        if (!passwordEncoder.matches(accountDTO.getPassword(), account.getPassword()))
            throw new AccountNotFoundException("Email or password incorrect");
        if (account.getApplicant() != null && !account.getApplicant().isActive())
            throw new AccountNotFoundException("Email or password incorrect");
        String token = jwtService.generateTokenWithAccountInfo(account);
        if (account.getFirstLogin()) account.setFirstLogin(false);
        return token;
    }
    public Account registration(AccountRegisterRequestDTO accountRegisterRequestDTO, RoleEnum defaultRole) throws BadRequestException {
        if (defaultRole == null) {
            throw new BadRequestException("Default role cannot be null");
        }
        return registration(accountRegisterRequestDTO, new RoleEnum[]{defaultRole});
    }
    @Transactional
    public Account registration(AccountRegisterRequestDTO accountRegisterRequestDTO, RoleEnum ...roles) throws BadRequestException {
        if (accountRegisterRequestDTO == null || accountRegisterRequestDTO.getEmail() == null
                || accountRegisterRequestDTO.getPassword() == null)
            throw new BadRequestException("Fill all fields");
        if (!isEmailFree(accountRegisterRequestDTO.getEmail().toLowerCase()))
            throw new IllegalArgumentException("Email already in use");
        Account account = new Account();
        account.setEmail(accountRegisterRequestDTO.getEmail().toLowerCase());
        account.setPassword(passwordEncoder.encode(accountRegisterRequestDTO.getPassword()));
        Set<Role> accountRoles = new HashSet<>();
        for (RoleEnum roleName : roles) {
            if (roleName == null) continue;
            Role role = roleService.findByName(roleName);
            accountRoles.add(role);
        }
        account.setRoles(accountRoles);

        return repository.save(account);
    }
    public void updatePassword(Long accountId, String newPassword) throws AccountNotFoundException {
        Account account = getAccountById(accountId);

        if (newPassword == null || newPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }

        if (newPassword.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }

        account.setPassword(passwordEncoder.encode(newPassword));
        repository.save(account);
    }

    public Account getAccountById(Long id) throws AccountNotFoundException {
        return repository.findById(id).orElseThrow(() -> new AccountNotFoundException("ss"));
    }
    public Account getAccountByEmail(String email) throws AccountNotFoundException {
        return repository.findByEmail(email);
    }
    public Set<Role> getRolesByAccount(Long accountId) throws AccountNotFoundException {
        Account account = getAccountById((accountId));
        return account.getRoles();
    }
    //good for getting all HR's accounts and etc
    public Set<Account> getAccountsByRole(RoleEnum roleName) {
        Role role = roleService.findByName(roleName);
        return role.getAccounts();
    }

    public Page<Account> getAccountsByCompany(Long companyId, RoleEnum role, Pageable pageable) {
        return role == null
                ? repository.findByCompanyId(companyId, pageable)
                : repository.findByCompanyIdAndRole(companyId, role, pageable);
    }

}
