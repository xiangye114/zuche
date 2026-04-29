package com.zuche.controller;

import com.zuche.common.Result;
import com.zuche.entity.User;
import com.zuche.service.UserService;
import com.zuche.dto.LoginDTO;
import com.zuche.dto.RegisterDTO;
import com.zuche.dto.UpdateInfoDTO;
import com.zuche.dto.UpdatePasswordDTO;
import com.zuche.dto.UserDTO;
import com.zuche.utils.JwtUtils;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    
    @Resource
    private UserService userService;
    
    @Resource
    private JwtUtils jwtUtils;
    
    @PostMapping("/login")
    public Result<?> login(@Valid @RequestBody LoginDTO dto) {
        User user = userService.login(dto);
        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole());
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("user", UserDTO.fromEntity(user));
        return Result.success(data);
    }
    
    @PostMapping("/register")
    public Result<?> register(@Valid @RequestBody RegisterDTO dto) {
        userService.register(dto);
        return Result.success("注册成功");
    }
    
    @GetMapping("/info")
    public Result<?> getInfo(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        Long userId = jwtUtils.getUserId(token);
        User user = userService.getById(userId);
        return Result.success(user);
    }
    
    @PutMapping("/info")
    public Result<?> updateInfo(@Valid @RequestBody UpdateInfoDTO dto, HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        Long userId = jwtUtils.getUserId(token);
        userService.updateInfo(userId, dto);
        return Result.success("更新成功");
    }
    
    @PutMapping("/password")
    public Result<?> updatePassword(@Valid @RequestBody UpdatePasswordDTO dto, HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        Long userId = jwtUtils.getUserId(token);
        userService.updatePassword(userId, dto);
        return Result.success("密码修改成功");
    }
}
