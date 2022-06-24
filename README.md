# spring-boot-batch

# Spring Boot Batch with Schedule and REST Method

### Reference Documentation

This is a practice for my teacher Mr. Madhaj. I tried to provide a microservice for doing a regular task by
scheduling and REST method trigger. this project contain a CSV dataset including **50K records. in my local
machine, it transfered all these data to a database table within 2.4 second with chunk 100 record** per process.


### Guides

The following guides illustrate how to use some features concretely:

* [Accessing Data with JPA](https://spring.io/guides/gs/accessing-data-jpa/)
* [Creating a Batch Service](https://spring.io/guides/gs/batch-processing/)
* [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
* [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
* [Building REST services with Spring](https://spring.io/guides/tutorials/rest/)
* [Accessing data with MySQL](https://spring.io/guides/gs/accessing-data-mysql/)

## The Implementations
in this project, a controller and a batch config class is defined to handle the schedule task.
I used a MySql database as datasource, so i had to define the propertie file with intialize schema property.
so here is my implementation:
#### Property File:
````
spring.datasource.url=jdbc:mysql://localhost:3306/batchDB?autoReconnect=true&useSSL=false&createDatabaseIfNotExist=true
spring.datasource.driverClassName=com.mysql.cj.jdbc.Driver
spring.datasource.username=root
spring.datasource.password=*******
spring.jpa.database-platform=org.hibernate.dialect.MySQL5InnoDBDialect
spring.jpa.generate-ddl=true
spring.jpa.hibernate.ddl-auto=create-drop
spring.batch.jdbc.initialize-schema=ALWAYS
spring.batch.job.enabled=false
````

#### BatchConfiguration Class:
````
#### imports removed for brevity

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
````

#### My Job Controller for invoke by REST or perform Schedule task to run and clean table:
````
####imports are removed for brevity

@RestController
@Slf4j
public class JobController {

    @Autowired
    JobLauncher jobLauncher;
    @Autowired
    Job job;
    @Autowired
    FinanceRepository financeRepository;

    @RequestMapping(method = RequestMethod.GET,path = "/invokejob")
    public String jobHandler() throws JobInstanceAlreadyCompleteException,
            JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        JobParameters jobParameters = new JobParametersBuilder().
                addLong("time",System.currentTimeMillis()).toJobParameters();
        jobLauncher.run(job,jobParameters);
        return "Job is Invoked.";
    }

    @Scheduled(cron = "0/60 * * * * *")
    private void clearTable(){
        financeRepository.deleteAll();
        log.info("Finance Report Cleard.");
    }
    @Scheduled(cron = "0/30 * * * * *")
    private void invokeReportSchedule() throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        jobHandler();
    }

}
````
