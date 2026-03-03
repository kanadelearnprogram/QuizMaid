package com.kanade.backend.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.kanade.backend.model.entity.Useranswerstats;
import com.kanade.backend.mapper.UseranswerstatsMapper;
import com.kanade.backend.service.UseranswerstatsService;
import org.springframework.stereotype.Service;

/**
 * 用户做题日统计表 服务层实现。
 *
 * @author kanade
 */
@Service
public class UseranswerstatsServiceImpl extends ServiceImpl<UseranswerstatsMapper, Useranswerstats>  implements UseranswerstatsService{

}
