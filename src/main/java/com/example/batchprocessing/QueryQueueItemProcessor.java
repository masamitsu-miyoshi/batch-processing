package com.example.batchprocessing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import com.example.batchprocessing.model.QueryQueue;

public class QueryQueueItemProcessor  implements ItemProcessor<QueryQueue, QueryQueue>  {
	private static final Logger log = LoggerFactory.getLogger(QueryQueueItemProcessor.class);

	@Override
	public QueryQueue process(QueryQueue queryQueue) throws Exception {
		
		// 値をバインド
		
		// 送信
		
		// 送信結果を格納
		
		return queryQueue;
	}
}
