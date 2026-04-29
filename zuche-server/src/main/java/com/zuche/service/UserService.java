package com.zuche.service;

import com.zuche.entity.User;
import com.zuche.dto.LoginDTO;
import com.zuche.dto.RegisterDTO;
import com.zuche.dto.UpdateInfoDTO;
import com.zuche.dto.UpdatePasswordDTO;

public interface UserService {
    User login(LoginDTO dto);
    void register(RegisterDTO dto);
    User getById(Long id);
    User getByUsername(String username);
    void updateInfo(Long userId, UpdateInfoDTO dto);
    void updatePassword(Long userId, UpdatePasswordDTO dto);
}
