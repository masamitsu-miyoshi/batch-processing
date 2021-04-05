package com.example.batchprocessing.model;

import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Builder
@NoArgsConstructor
@Data
public class QueryQueue {
	private String ifid;
	private int seq;
	private Timestamp timestamp;
	private String primaryKeyset;
	private String query;
	private String status;
	private int postCount;
	private String result;
}
