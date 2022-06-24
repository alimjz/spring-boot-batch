package com.example.springbootbatch.entity;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
public class FinanceProcessor implements ItemProcessor<Finance,Finance> {

    @Override
    public Finance process(Finance finance) throws Exception {
        String period = finance.getPeriod();
        String data = finance.getData_value();
        String status = finance.getStatus();
        String units = finance.getUnits();
        String magnitude = finance.getMagnitude();
        String subject = finance.getSubject();
        log.info("100 Record Processed.");
        return new Finance(period,data,status,units,magnitude,subject);
    }


    public void scheduleCheck(){
        log.info("schedule method is invoked.");
    }
}
