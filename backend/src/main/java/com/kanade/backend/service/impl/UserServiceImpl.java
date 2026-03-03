package com.kanade.backend.service.impl;

import cn.hutool.core.util.StrUtil;
import com.kanade.backend.exception.BusinessException;
import com.kanade.backend.exception.ErrorCode;
import com.kanade.backend.exception.ThrowUtils;
import com.kanade.backend.mapper.UserMapper;
import com.kanade.backend.model.dto.UserLoginDTO;
import com.kanade.backend.model.dto.UserRegisterDTO;
import com.kanade.backend.model.entity.User;
import com.kanade.backend.model.entity.UserSign;
import com.kanade.backend.model.vo.UserLoginVO;
import com.kanade.backend.service.UserService;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.DigestUtils;

public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {


    @Override
    public User register(UserRegisterDTO userRegisterDTO) {
        if (StrUtil.isBlank(userRegisterDTO.getUserPassword())|| StrUtil.isBlank(userRegisterDTO.getUserName())||StrUtil.isBlank(userRegisterDTO.getCheckUserPassword())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        if (userRegisterDTO.getUserName().length() < 3 || userRegisterDTO.getUserName().length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名长度必须在 3-20 位之间");
        }

        if (userRegisterDTO.getUserPassword().length() < 6 || userRegisterDTO.getUserPassword().length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度必须在 6-20 位之间");
        }

        if (!userRegisterDTO.getUserPassword().equals(userRegisterDTO.getCheckUserPassword())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }

        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("username",userRegisterDTO.getUserName());

        long count = this.mapper.selectCountByQuery(queryWrapper);
        if (count > 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"username 重复");
        }

        String md5DigestAsHex = DigestUtils.md5DigestAsHex(userRegisterDTO.getUserPassword().getBytes());

        User user = new User();
        user.setUsername(userRegisterDTO.getUserName());
        user.setPassword(md5DigestAsHex);
        return user;
    }

    @Override
    public UserLoginVO getLoginUserVO(UserSign user) {
        return null;
    }

    @Override
    public UserLoginVO UserLogin(UserLoginDTO userLoginDTO, HttpServletRequest request) {
        return null;
    }

    @Override
    public User getUserLoginInfo(HttpServletRequest request) {
        return null;
    }
}
