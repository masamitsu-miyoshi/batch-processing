package com.example.batchprocessing;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.tasklet.MethodInvokingTaskletAdapter;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

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
