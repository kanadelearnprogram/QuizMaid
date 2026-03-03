package com.kanade.backend.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.kanade.backend.model.entity.Userexamrecord;
import com.kanade.backend.mapper.UserexamrecordMapper;
import com.kanade.backend.service.UserexamrecordService;
import org.springframework.stereotype.Service;

/**
 * 用户考试记录表 服务层实现。
 *
 * @author kanade
 */
@Service
public class UserexamrecordServiceImpl extends ServiceImpl<UserexamrecordMapper, Userexamrecord>  implements UserexamrecordService{

}
