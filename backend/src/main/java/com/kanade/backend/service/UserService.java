package com.kanade.backend.service;

import com.kanade.backend.model.dto.UserLoginDTO;
import com.kanade.backend.model.dto.UserRegisterDTO;
import com.kanade.backend.model.entity.User;
import com.kanade.backend.model.entity.UserSign;
import com.kanade.backend.model.vo.UserLoginVO;
import com.mybatisflex.core.service.IService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

@Service
public interface UserService extends IService<User> {

    User register(UserRegisterDTO userRegisterDTO);

    UserLoginVO getLoginUserVO(UserSign user);

    UserLoginVO UserLogin(UserLoginDTO userLoginDTO, HttpServletRequest request);

    User getUserLoginInfo(HttpServletRequest request);
}
