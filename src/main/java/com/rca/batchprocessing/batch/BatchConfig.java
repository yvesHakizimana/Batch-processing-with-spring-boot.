package com.rca.batchprocessing.batch;

import com.rca.batchprocessing.Student;
import com.rca.batchprocessing.StudentProcessor;
import com.rca.batchprocessing.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class BatchConfig {

    private final StudentRepository studentRepository;
    private final PlatformTransactionManager platformTransactionManager;
    private final JobRepository jobRepository;

    //Reading the data from the datasource through using an itemReader.
    @Bean
    public FlatFileItemReader<Student> itemReader(){
        FlatFileItemReader<Student> reader = new FlatFileItemReader<>();
        //Resource where you want to read the data.
        reader.setResource(new FileSystemResource("src/main/resources/student.csv"));
        //Setting the name of the reader.
        reader.setName("csvReader");
        //Skip the header (the first line containing the column names if present) while you are reading the csv
        reader.setLinesToSkip(1);
        //Transform the data read into a given entity depending on the businessLogic.
        reader.setLineMapper(lineMapper());
        return reader;
    }

    //Mapping the data read to a given entity.
    private LineMapper<Student> lineMapper(){
        DefaultLineMapper<Student> lineMapper = new DefaultLineMapper<>();

        //Line tokenizer and separator
        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
        //Data being separated by a comma
        lineTokenizer.setDelimiter(",");
        lineTokenizer.setStrict(false);
        //Set the Names of the column you are reading or the data is separated into.
        lineTokenizer.setNames("id","firstName", "lastName", "age");


        // Mapping the data read from the csv into the corresponding entity.
        BeanWrapperFieldSetMapper<Student> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(Student.class);


        //Returning and populating our lineMapper with lineTokenizer and fieldSetMapper.
        lineMapper.setLineTokenizer(lineTokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);
         return lineMapper;
    }

    //Encapsulates the business logic to process the data.
    @Bean
    public StudentProcessor processor(){
        return new StudentProcessor();
    }

    //Writes the data to a given data sink incorporated in the process.
    @Bean
    public RepositoryItemWriter<Student> writer(){
        RepositoryItemWriter<Student> writer = new RepositoryItemWriter<>();
        //Repository where to send/save the data.
        writer.setRepository(studentRepository);
        //Saving the data same as ---> 'repository.save'
        writer.setMethodName("save");
        return writer;
    }

    //Steps of executing the jobs.
    @Bean
    public Step importStep(){
        return new StepBuilder("csvImport", jobRepository)
                .<Student, Student>chunk(100, platformTransactionManager)
                .reader(itemReader())
                .processor(processor())
                .writer(writer())
                .build();

    }

    //Finally, run the job.
    @Bean
    public Job runJob(){
        return new JobBuilder("importStudents", jobRepository)
                .start(importStep())
                .build();
    }


}
