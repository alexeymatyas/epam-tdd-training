package com.epam.jamp2.service;

import com.epam.jamp2.model.CalculationCommand;
import com.epam.jamp2.model.Operation;
import com.epam.jamp2.model.UnknownCurrencyException;
import com.epam.jamp2.model.Value;
import com.epam.jamp2.rest.FixerioServiceProxyConfiguration;
import com.epam.jamp2.service.impl.CalculationCommandExecutionServiceImpl;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContextManager;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static org.hamcrest.number.BigDecimalCloseTo.closeTo;
import static org.junit.Assert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

@RunWith(Parameterized.class)
//@RunWith(SpringJUnit4ClassRunner.class)
@SuppressWarnings("InstanceMethodNamingConvention")
@ContextConfiguration(classes = {TestConfig.class, FixerioServiceProxyConfiguration.class })
public class ITCalculationCommandExecutionService {

    // Performs the same job as @RunWith(SpringJUnit4ClassRunner.class)
    private static TestContextManager testContextManager;
    @BeforeClass
    public static void setUpStringContext() throws Exception {
        testContextManager = new TestContextManager(ITCalculationCommandExecutionService.class);
    }
    @Before
    public void setUpStringContextt() throws Exception {
        testContextManager.prepareTestInstance(this);
    }

    @Autowired
    private CalculationCommandExecutionServiceImpl target;

    private CalculationCommand command;
    private Value expected;

    // Parameterized data for tests
    @Parameters(name = "{index}: test calculate (command: {0}, {1}, {2}, {3}) should return = {4}")
    public static Collection<Object[]> data() {

        return Arrays.asList(new Object[][]{
                {new Value("gbp", BigDecimal.valueOf(1.0)), new Value("gbp", BigDecimal.valueOf(2.0))
                        , Operation.ADD, "gbp", new Value("gbp", BigDecimal.valueOf(3.0))},
                {new Value("gbp", BigDecimal.valueOf(3.0)), new Value("gbp", BigDecimal.valueOf(2.0))
                        , Operation.SUBTRACT, "gbp", new Value("gbp", BigDecimal.valueOf(1.0))},
                {new Value("gbp", BigDecimal.valueOf(1.0)), new Value("cny", BigDecimal.valueOf(2.0))
                        , Operation.ADD, "cny", new Value("cny", BigDecimal.valueOf(12.0))},
                {new Value("gbp", BigDecimal.valueOf(10000000000000000001.0)),
                        new Value("cny", BigDecimal.valueOf(20000000000000000000.0))
                        , Operation.ADD, "cny", new Value("cny", BigDecimal.valueOf(120000000000000000000.0))},
                {new Value("gbp", BigDecimal.valueOf(0.00001)), new Value("gbp", BigDecimal.valueOf(0.0))
                        , Operation.ADD, "cny", new Value("cny", BigDecimal.valueOf(0.0001))},
                {new Value("gbp", BigDecimal.valueOf(1.0)), new Value("gbp", BigDecimal.valueOf(2.0))
                        , Operation.ADD, null, new Value("gbp", BigDecimal.valueOf(3.0))},
                {new Value("gbp", BigDecimal.valueOf(1.0)), new Value("cny", BigDecimal.valueOf(2.0))
                        , Operation.ADD, null, new Value("gbp", BigDecimal.valueOf(1.2))},
                {new Value("gbp", BigDecimal.valueOf(1.0)), new Value(null, BigDecimal.valueOf(2.0))
                        , Operation.ADD, null, new Value("gbp", BigDecimal.valueOf(3.0))},
                {new Value(null, BigDecimal.valueOf(1.0)), new Value(null, BigDecimal.valueOf(2.0))
                        , Operation.ADD, "gbp", new Value("gbp", BigDecimal.valueOf(3.0))}
        });
    }

    // constructor
    public ITCalculationCommandExecutionService(Value leftOperand, Value rightOperand, Operation operation
                , String resultCurrencyCode, Value expected) {
        this.command = new CalculationCommand(leftOperand, rightOperand, operation, resultCurrencyCode);
        this.expected = expected;
    }

    @Test
    public void test_calculate() throws IOException, UnknownCurrencyException {
        Value result = target.calculate(command);
        BigDecimal errorTolerance = expected.getValue().multiply(BigDecimal.valueOf(0.15)); // i.e. ±15% for currency fluctuations
        assertThat((result), hasProperty("value", closeTo(expected.getValue(), errorTolerance)));
        assertThat(result.getCurrencyCode(), equalTo(expected.getCurrencyCode()));
    }

}