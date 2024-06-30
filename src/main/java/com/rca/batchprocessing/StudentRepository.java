package com.rca.batchprocessing;

import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
@EnableJpaRepositories
public interface StudentRepository extends CrudRepository<Student, Integer> {
}
