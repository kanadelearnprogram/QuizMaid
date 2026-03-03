package com.kanade.backend.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.kanade.backend.model.entity.User;
import com.kanade.backend.mapper.UserMapper;
import com.kanade.backend.service.UseraccountService;
import org.springframework.stereotype.Service;

/**
 * 用户账号表 服务层实现。
 *
 * @author kanade
 */
@Service
public class UseraccountServiceImpl extends ServiceImpl<UserMapper, User>  implements UseraccountService{

}
