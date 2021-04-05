package com.example.batchprocessing;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.tasklet.MethodInvokingTaskletAdapter;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.support.MySqlPagingQueryProvider;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import com.example.batchprocessing.mapper.QueryQueueRowMapper;
import com.example.batchprocessing.model.Person;
import com.example.batchprocessing.model.QueryQueue;
import com.example.batchprocessing.service.OrderService;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {
	@Autowired
	public JobBuilderFactory jobBuilderFactory;

	@Autowired
	public StepBuilderFactory stepBuilderFactory;

	@Autowired
	public DataSource dataSource;

	
	// =============================
	// Step Sample
	// =============================

	// -----------------ChunkStep↓---------------------
	@Bean
	public Step insertUserFromCsvStep() {
		return stepBuilderFactory.get("insertUserFromCsvStep")
				.<Person, Person> chunk(10)
				.reader(reader())
				.processor(processor())
				.writer(writer())
				.build();
	}
	
	@Bean
	public FlatFileItemReader<Person> reader() {
		return new FlatFileItemReaderBuilder<Person>()
				.name("personItemReader")
				.resource(new ClassPathResource("sample-data.csv"))
				.delimited()
				.names(new String[]{"firstName", "lastName"})
				.fieldSetMapper(new BeanWrapperFieldSetMapper<Person>() {{
					setTargetType(Person.class);
				}})
				.build();
	}

	@Bean
	public PersonItemProcessor processor() {
		return new PersonItemProcessor();
	}

	@Bean
	public JdbcBatchItemWriter<Person> writer() {
		return new JdbcBatchItemWriterBuilder<Person>()
				.itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
				.sql("INSERT INTO people (first_name, last_name) VALUES (:firstName, :lastName)")
				.dataSource(dataSource)
				.build();
	}
	
	
	@Bean
	public Step dataUpdateStep() {
		
		return stepBuilderFactory.get("dataUpdateStep")
				.<QueryQueue, QueryQueue> chunk(10)
				.reader(new FlatFileItemReaderBuilder<QueryQueue>()
						.name("personItemReader")
						.resource(new ClassPathResource("sample-data.csv"))
						.delimited()
						.names(new String[]{"firstName", "lastName"})
						.fieldSetMapper(new BeanWrapperFieldSetMapper<QueryQueue>() {{
							setTargetType(QueryQueue.class);
						}})
						.build())
				.processor(new QueryQueueItemProcessor())
				.writer(new JdbcBatchItemWriterBuilder<QueryQueue>()
						.itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
						.sql("UPDATE QueryQueue (status, postcount, result) VALUES (:status, :postCount, :result)")
						.dataSource(dataSource)
						.build())
				.build();
	}

	// -----------------TaskletStep↓---------------------
	@Bean
	public Step printMessageStep() {
		return stepBuilderFactory.get("printMessageStep")
				.tasklet((contribution, chunkContext) -> {
					System.out.println("Yet another Tasklet!");
					return RepeatStatus.FINISHED;
				})
				.build();
	}

	// -----------------TaskletStep with ServiceClass↓---------------------
	@Bean
	public Step orderStep(OrderService orderService) {
	    // xxxService#execute()を実行する
	    MethodInvokingTaskletAdapter tasklet = new MethodInvokingTaskletAdapter();
	    tasklet.setTargetObject(orderService);
	    tasklet.setTargetMethod("execute");
		
		return stepBuilderFactory.get("orderStep")
				.tasklet(tasklet)
				.build();
	}
	
	// -------------thread----------------
	
    @Bean
    public ColumnRangePartitioner partitioner() 
    {
        ColumnRangePartitioner columnRangePartitioner = new ColumnRangePartitioner();
        columnRangePartitioner.setColumn("id");
        columnRangePartitioner.setDataSource(dataSource);
        columnRangePartitioner.setTable("customer");
        return columnRangePartitioner;
    }
 
    @Bean
    @StepScope
    public JdbcPagingItemReader<QueryQueue> pagingItemReader(
            @Value("#{stepExecutionContext['minValue']}") Long minValue,
            @Value("#{stepExecutionContext['maxValue']}") Long maxValue) 
    {
        System.out.println("reading " + minValue + " to " + maxValue);
 
        Map<String, Order> sortKeys = new HashMap<>();
        sortKeys.put("id", Order.ASCENDING);
         
        MySqlPagingQueryProvider queryProvider = new MySqlPagingQueryProvider();
        queryProvider.setSelectClause("id, firstName, lastName, birthdate");
        queryProvider.setFromClause("from customer");
        queryProvider.setWhereClause("where id >= " + minValue + " and id < " + maxValue);
        queryProvider.setSortKeys(sortKeys);
         
        JdbcPagingItemReader<QueryQueue> reader = new JdbcPagingItemReader<>();
        reader.setDataSource(this.dataSource);
        reader.setFetchSize(1000);
        reader.setRowMapper(new QueryQueueRowMapper());
        reader.setQueryProvider(queryProvider);
         
        return reader;
    }
     
     
    @Bean
    @StepScope
    public JdbcBatchItemWriter<QueryQueue> customerItemWriter()
    {
        JdbcBatchItemWriter<QueryQueue> itemWriter = new JdbcBatchItemWriter<>();
        itemWriter.setDataSource(dataSource);
        itemWriter.setSql("INSERT INTO QueryQueue VALUES (:ifid, :firstName, :lastName, :birthdate)");
 
        itemWriter.setItemSqlParameterSourceProvider
            (new BeanPropertyItemSqlParameterSourceProvider<>());
        itemWriter.afterPropertiesSet();
         
        return itemWriter;
    }
     
    // Master
    @Bean
    public Step step1() 
    {
        return stepBuilderFactory.get("step1")
                .partitioner(slaveStep().getName(), partitioner())
                .step(slaveStep())
                .gridSize(4)
                .taskExecutor(new SimpleAsyncTaskExecutor())
                .build();
    }
     
    // slave step
    @Bean
    public Step slaveStep() 
    {
        return stepBuilderFactory.get("slaveStep")
                .<QueryQueue, QueryQueue>chunk(1000)
                .reader(pagingItemReader(null, null))
                .writer(customerItemWriter())
                .build();
    }
     
	
	
	
	// =============================
	// Job Sample
	// =============================

	// インターセプトして、ジョブを呼び出す
	@Bean
	public Job insertJob(JobCompletionNotificationListener listener, Step insertUserFromCsvStep) {
		return jobBuilderFactory.get("insertJob")
				.incrementer(new RunIdIncrementer())
				.listener(listener)
				.flow(insertUserFromCsvStep)
				.end()
				.build();
	}
	
	@Bean
	public Job printJob(Step printMessageStep) {
		return jobBuilderFactory.get("printJob")
				.incrementer(new RunIdIncrementer())
				.start(printMessageStep)
				.build();
	}
	
	@Bean
	public Job orderJob(Step orderStep) {
		return jobBuilderFactory.get("orderJob")
				.incrementer(new RunIdIncrementer())
				.start(orderStep)
				.build();
	}
	
	// 複合ジョブ
	@Bean
	public Job complexJob(Step insertUserFromCsvStep, Step orderStep) {
		return jobBuilderFactory.get("complexJob")
				.incrementer(new RunIdIncrementer())
				.start(insertUserFromCsvStep)
				.next(orderStep)
				.build();
	}
	
}
