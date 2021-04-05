package com.example.batchprocessing.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.example.batchprocessing.model.QueryQueue;

public class QueryQueueRowMapper implements RowMapper<QueryQueue> {
    @Override
    public QueryQueue mapRow(ResultSet rs, int rowNum) throws SQLException {
        return QueryQueue.builder()
        		.ifid(rs.getString("IFID"))
                .seq(rs.getInt("SEQ"))
                .timestamp(rs.getTimestamp("Timestamp"))
                .primaryKeyset(rs.getString("PrimaryKeyset"))
                .query(rs.getString("Query"))
                .status(rs.getString("Status"))
                .postCount(rs.getInt("PostCount"))
                .result(rs.getString("Result"))
                .build();
    }
}
