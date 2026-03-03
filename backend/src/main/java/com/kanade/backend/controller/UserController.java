package com.kanade.backend.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.kanade.backend.common.BaseResponse;
import com.kanade.backend.common.ResultUtils;
import com.kanade.backend.exception.BusinessException;
import com.kanade.backend.exception.ErrorCode;
import com.kanade.backend.model.dto.UserLoginDTO;
import com.kanade.backend.model.dto.UserRegisterDTO;
import com.kanade.backend.model.entity.User;
import com.kanade.backend.model.vo.UserLoginVO;
import com.kanade.backend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    @Autowired
    UserService userService;

    @GetMapping("/")
    public BaseResponse<String> healthCheck() {
        return ResultUtils.success("ok test  koko tttt check");

    }

    @PostMapping("/login")
    public BaseResponse<UserLoginVO> login(@RequestBody UserLoginDTO userLoginDTO, HttpServletRequest request){
        UserLoginVO userLoginVO = userService.UserLogin(userLoginDTO, request);
        log.info(String.valueOf(userLoginVO));
        return ResultUtils.success(userLoginVO);
    }


    @GetMapping("/get/login")
    public BaseResponse<UserLoginVO> getLoginUserInfo(HttpServletRequest request){
        User user = userService.getUserLoginInfo(request);
        log.info(String.valueOf(user));
        return ResultUtils.success(userService.getLoginUserVO(user));
    }
    @PostMapping("/logout")
    public BaseResponse<Boolean> logout(HttpServletRequest request){
        boolean result = userService.logout(request);
        return ResultUtils.success(result);
    }
    /**
     * user register
     */
    @PostMapping("/register")
    public BaseResponse<User> register(@RequestBody UserRegisterDTO userRegisterDTO){
        User register = userService.register(userRegisterDTO);
        return ResultUtils.success(register);
    }

    // 根据id 获取用户
    @GetMapping("/get/user")
    @SaCheckRole("admin")
    public BaseResponse<User> getUserById(long id){
        if (id < 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getById(id);
        if (user == null){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(user);
    }

    // 根据id 获取用户vo
    @GetMapping("/get/uservo")
    @SaCheckRole("admin")
    public BaseResponse<User> getUserVoById(long id){
        if (id < 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getById(id);
        if (user == null){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        userService.getUserVOById(user);
        return ResultUtils.success(user);
    }
}

