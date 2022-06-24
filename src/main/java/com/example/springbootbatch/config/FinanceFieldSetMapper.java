package com.example.springbootbatch.config;

import com.example.springbootbatch.entity.Finance;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

public class FinanceFieldSetMapper implements FieldSetMapper {

    @Override
    public Object mapFieldSet(FieldSet fieldSet) throws BindException {
        Finance finance = new Finance();
        finance.setPeriod(fieldSet.readString("period"));
        finance.setData_value(fieldSet.readString("data_value"));
        finance.setStatus(fieldSet.readString("status"));
        finance.setUnits(fieldSet.readString("units"));
        finance.setMagnitude(fieldSet.readString("magnitude"));
        finance.setSubject(fieldSet.readString("subject"));

        return finance;
    }
}
