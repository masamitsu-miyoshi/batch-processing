package com.example.batchprocessing;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("step") // 起動引数の参照に必要
public class QueryQueueTasklet implements Tasklet {
	
	private static final Logger log = LoggerFactory.getLogger(QueryQueueTasklet.class);

	// 起動引数の参照に必要
    @Value("#{jobParameters['ifid']}")
    private String ifid;
	
	@Autowired
	public DataSource dataSource;
	
	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		log.info("QueryQueueTasklet!!");
		
		
		return RepeatStatus.FINISHED;
	}
}
