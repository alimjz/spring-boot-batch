package com.example.springbootbatch.config;


import com.example.springbootbatch.entity.Finance;
import com.example.springbootbatch.entity.FinanceProcessor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.sql.DataSource;

@Configuration
@EnableBatchProcessing
@EnableScheduling
public class BatchConfig {
    @Autowired
    public JobBuilderFactory jobBuilderFactory;
    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    @Bean
    public FlatFileItemReader<Finance> reader(){
        return new FlatFileItemReaderBuilder<Finance>().name("Finance")
                .resource(new ClassPathResource("finance.csv"))
                .delimited()
                .names(new String[]{ "period","data_value","status","units","magnitude","subject"})
                .lineMapper(lineMapper())
                .fieldSetMapper(new BeanWrapperFieldSetMapper<Finance>(){{
                    setTargetType(Finance.class);
                }})
                .build();
    }

    @Bean
    public LineMapper<Finance> lineMapper() {
        final DefaultLineMapper<Finance> defaultLineMapper
                = new DefaultLineMapper<>();
        final DelimitedLineTokenizer delimitedLineTokenizer
                = new DelimitedLineTokenizer();
        delimitedLineTokenizer.setDelimiter(",");
        delimitedLineTokenizer.setStrict(false);
        delimitedLineTokenizer.setNames(new String[]{ "period","data_value","status","units","magnitude","subject"});

        final FinanceFieldSetMapper financeFieldSetMapper =
                new FinanceFieldSetMapper();

        defaultLineMapper.setLineTokenizer(delimitedLineTokenizer);
        defaultLineMapper.setFieldSetMapper(financeFieldSetMapper);
        return defaultLineMapper;
    }

    @Bean
    public FinanceProcessor processor(){
        return new FinanceProcessor();
    }

    @Bean
    public JdbcBatchItemWriter<Finance> writer(DataSource dataSource){
        return new JdbcBatchItemWriterBuilder<Finance>()
                .itemSqlParameterSourceProvider(
                        new BeanPropertyItemSqlParameterSourceProvider<>()
                ).sql("INSERT INTO batchdb.TBL_FINANCE (PERIOD, DATA_VALUE,STATUS,UNITS,MAGNITUDE,SUBJECT) " +
                        "VALUES (:period, :data_value,:status,:units, :magnitude,:subject)")
                .dataSource(dataSource).build();
    }

    @Bean
    public Step step1 (JdbcBatchItemWriter<Finance> writer){
        return stepBuilderFactory.get("step1").<Finance,Finance> chunk(100)
                .reader(reader()).processor(processor()).writer(writer).build();
    }

    @Bean
    public Job importFinanceJob(NotificationListener listener, Step step1) {
        return jobBuilderFactory.get("importFinanceJob")
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .flow(step1)
                .end()
                .build();
    }
}
