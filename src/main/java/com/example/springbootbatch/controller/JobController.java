package com.example.springbootbatch.controller;


import com.example.springbootbatch.entity.FinanceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

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
