package com.medipay.unit;

import com.medipay.dto.ResetPasswordRequest;
import com.medipay.dto.SignupRequest;
import com.medipay.dto.VerifyEmailRequest;
import com.medipay.entity.User;
import com.medipay.mapper.UserMapper;
import com.medipay.repository.UserRepository;
import com.medipay.repository.WalletRepository;
import com.medipay.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AuthService authService;

    // ✅ REGISTER SUCCESS
    @Test
    void shouldRegisterUserSuccessfully() {

        SignupRequest request = new SignupRequest();
        request.setUsername("ndon");
        request.setEmail("ndon@mail.com");

        User user = new User();
        user.setId(1L);

        when(userRepository.existsByUsername(any())).thenReturn(false);
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(userMapper.toEntity(any())).thenReturn(user);
        when(userRepository.save(any())).thenReturn(user);

        User result = authService.registerUser(request);

        assertNotNull(result);
        assertEquals(1L, result.getId());

        verify(walletRepository).save(any());
    }

    // ❌ USERNAME EXISTS
    @Test
    void shouldThrowIfUsernameExists() {

        SignupRequest request = new SignupRequest();
        request.setUsername("ndon");

        when(userRepository.existsByUsername("ndon")).thenReturn(true);

        assertThrows(RuntimeException.class,
                () -> authService.registerUser(request));
    }

    // ❌ EMAIL EXISTS
    @Test
    void shouldThrowIfEmailExists() {

        SignupRequest request = new SignupRequest();
        request.setEmail("ndon@mail.com");

        when(userRepository.existsByUsername(any())).thenReturn(false);
        when(userRepository.existsByEmail("ndon@mail.com")).thenReturn(true);

        assertThrows(RuntimeException.class,
                () -> authService.registerUser(request));
    }

    // ✅ VERIFY EMAIL OK
    @Test
    void shouldVerifyEmail() {

        VerifyEmailRequest request = new VerifyEmailRequest();
        request.setEmail("ndon@mail.com");

        when(userRepository.existsByEmail(any())).thenReturn(true);

        ResponseEntity<?> response = authService.verifyEmail(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    // ❌ VERIFY EMAIL NOT FOUND
    @Test
    void shouldReturnNotFoundIfEmailDoesNotExist() {

        VerifyEmailRequest request = new VerifyEmailRequest();
        request.setEmail("unknown@mail.com");

        when(userRepository.existsByEmail(any())).thenReturn(false);

        ResponseEntity<?> response = authService.verifyEmail(request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // ✅ RESET PASSWORD
    @Test
    void shouldResetPassword() {

        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setEmail("ndon@mail.com");
        request.setNewPassword("123456");

        User user = new User();
        user.setEmail("ndon@mail.com");

        when(userRepository.findByEmail(any()))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.encode(any())).thenReturn("encoded");

        ResponseEntity<?> response = authService.resetPassword(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userRepository).save(any());
    }

    // ❌ RESET PASSWORD USER NOT FOUND
    @Test
    void shouldFailResetPasswordIfUserNotFound() {

        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setEmail("unknown@mail.com");

        when(userRepository.findByEmail(any()))
                .thenReturn(Optional.empty());

        ResponseEntity<?> response = authService.resetPassword(request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
