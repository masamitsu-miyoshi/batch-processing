package com.example.batchprocessing.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class OrderService {
	private static final Logger log = LoggerFactory.getLogger(OrderService.class);

	void execute() {
		log.info("ABCDEFG");
	}
}
