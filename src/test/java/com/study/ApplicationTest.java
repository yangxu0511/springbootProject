package com.study;

import com.study.common.model.Person;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ApplicationTest {
    @Autowired
    private Person person;

    @Test
    public void testProperties() {
        System.out.println(person);

    }
}
