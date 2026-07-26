package com.moa.backend.user;

import com.moa.backend.global.exception.BusinessException;
import com.moa.backend.global.exception.ErrorCode;
import com.moa.backend.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public UserResponse getMe(Long userId) {
        return UserResponse.from(getUserOrThrow(userId));
    }

    public User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}
