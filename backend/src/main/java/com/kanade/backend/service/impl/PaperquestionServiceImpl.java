package com.kanade.backend.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.kanade.backend.model.entity.Paperquestion;
import com.kanade.backend.mapper.PaperquestionMapper;
import com.kanade.backend.service.PaperquestionService;
import org.springframework.stereotype.Service;

/**
 * 试卷试题关联表 服务层实现。
 *
 * @author kanade
 */
@Service
public class PaperquestionServiceImpl extends ServiceImpl<PaperquestionMapper, Paperquestion>  implements PaperquestionService{

}
