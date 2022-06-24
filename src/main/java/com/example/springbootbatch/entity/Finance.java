package com.example.springbootbatch.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@NoArgsConstructor
@Entity(name = "TBL_FINANCE")
public class Finance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false)
    private int id;
    @Column(name = "PERIOD")
    private String period;
    @Column(name = "DATA_VALUE")
    private String data_value;
    @Column(name = "STATUS")
    private String status;
    @Column(name = "UNITS")
    private String units;
    @Column(name = "MAGNITUDE")
    private String magnitude;
    @Column(name = "SUBJECT")
    private String subject;

    public Finance(String period, String data_value, String status, String units, String magnitude, String subject){
        this.period = period;
        this.data_value = data_value;
        this.status = status;
        this.units = units;
        this.magnitude = magnitude;
        this.subject = subject;
    }
}
