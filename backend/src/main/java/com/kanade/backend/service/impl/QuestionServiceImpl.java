package com.kanade.backend.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.kanade.backend.model.entity.Question;
import com.kanade.backend.mapper.QuestionMapper;
import com.kanade.backend.service.QuestionService;
import org.springframework.stereotype.Service;

/**
 * 试题主表 服务层实现。
 *
 * @author kanade
 */
@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question>  implements QuestionService{

}
