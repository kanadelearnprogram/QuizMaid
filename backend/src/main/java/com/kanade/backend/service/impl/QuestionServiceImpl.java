package com.kanade.backend.service.impl;

import com.kanade.backend.ai.AiService;
import com.kanade.backend.ai.AiServiceFactory;
import com.kanade.backend.ai.model.LabelResult;
import com.kanade.backend.model.enums.TaskEnum;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.kanade.backend.model.entity.Question;
import com.kanade.backend.mapper.QuestionMapper;
import com.kanade.backend.service.QuestionService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * 试题主表 服务层实现。
 *
 * @author kanade
 */

@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question>  implements QuestionService{

    @Resource
    private AiServiceFactory aiServiceFactory;
    @Override
    public Question addLabels(Question question) {
        AiService aiService = aiServiceFactory.getAiService(TaskEnum.LABEL);
        LabelResult labelResult = aiService.generateQuestionLabel(question.toString());
        question.setDifficulty(labelResult.getDifficult());
        question.setTags(labelResult.getKnowledgePoints().toString());
        question.setSubject(labelResult.getSubject());
        this.updateById(question);
        return null;
    }

    public void updateQuestion(Question question) {
        // 2. 用实例调用，不是静态调用！
        AiService aiService = aiServiceFactory.getAiService(TaskEnum.LABEL);

        LabelResult labelResult = aiService.generateQuestionLabel(question.toString());
        question.setDifficulty(labelResult.getDifficult());
        question.setTags(labelResult.getKnowledgePoints().toString());
        question.setSubject(labelResult.getSubject());
        this.updateById(question);
    }
}
