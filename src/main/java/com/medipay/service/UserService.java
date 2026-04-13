package com.medipay.service;

import com.medipay.dto.UserResponse;
import com.medipay.entity.User;
import com.medipay.entity.Wallet;
import com.medipay.mapper.UserMapper;
import com.medipay.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public List<UserResponse> getAllUsersForAdmin() {
        List<User> users = userRepository.findAll();
        return userMapper.toListUserResponseDto(users);
    }
}
