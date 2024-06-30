package com.rca.batchprocessing;

import lombok.NonNull;
import org.springframework.batch.item.ItemProcessor;

public class StudentProcessor implements ItemProcessor<Student, Student> {
    @Override
    public Student process(@NonNull Student student) throws Exception {
        //Business Logic of processing data being received from the item reader.
        return student;
    }
}
