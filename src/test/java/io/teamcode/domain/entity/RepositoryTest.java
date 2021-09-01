package io.teamcode.domain.entity;

import org.hibernate.validator.HibernateValidator;
import org.junit.Before;
import org.junit.Test;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

/**
 * Created by chiang on 2017. 1. 19..
 */
public class RepositoryTest {

    private LocalValidatorFactoryBean localValidatorFactory;


    @Before
    public void setup() {
        localValidatorFactory = new LocalValidatorFactoryBean();
        localValidatorFactory.setProviderClass(HibernateValidator.class);
        localValidatorFactory.afterPropertiesSet();
    }

    @Test
    public void validate() {
        /*Repository repository = new Repository();
        repository.setName("");

        Set<ConstraintViolation<Repository>> constraintViolations = localValidatorFactory.validate(repository);
        Assert.assertTrue(constraintViolations.size() == 1);

        repository.setName("   ");
        constraintViolations = localValidatorFactory.validate(repository);
        Assert.assertTrue(constraintViolations.size() == 1);

        repository.setName("21");
        constraintViolations = localValidatorFactory.validate(repository);
        Assert.assertTrue(constraintViolations.size() == 0);

        repository.setName("한a");
        constraintViolations = localValidatorFactory.validate(repository);
        Assert.assertTrue(constraintViolations.size() == 1);

        repository.setName("한 a");
        constraintViolations = localValidatorFactory.validate(repository);
        Assert.assertTrue(constraintViolations.size() == 1);

        repository.setName("한 a   ");
        constraintViolations = localValidatorFactory.validate(repository);
        Assert.assertTrue(constraintViolations.size() == 1);*/
        System.out.println(1);
    }
}
