package org.albertosegura.loteria.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openpojo.reflection.filters.FilterPackageInfo;
import com.openpojo.validation.Validator;
import com.openpojo.validation.ValidatorBuilder;
import com.openpojo.validation.rule.impl.EqualsAndHashCodeMatchRule;
import com.openpojo.validation.rule.impl.GetterMustExistRule;
import com.openpojo.validation.rule.impl.NoFieldShadowingRule;
import com.openpojo.validation.test.Tester;
import com.openpojo.validation.test.impl.GetterTester;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class PojoTest {
    private static final EasyRandom random = new EasyRandom();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testPojos() {
        Tester testToString = (clazz) -> {
            final Class<?> cl = clazz.getClazz();
            Object o = random.nextObject(cl);
            try {
                String s = objectMapper.writerFor(cl).writeValueAsString(o);
                Object copy = objectMapper.readerFor(cl).readValue(s);
                assertEquals(o.toString(), copy.toString());
            } catch (JsonProcessingException e) {
                fail(e);
            }
        };
        Validator validator = ValidatorBuilder.create()
                .with(new GetterMustExistRule())
                .with(new GetterTester())
                .with(new EqualsAndHashCodeMatchRule())
                .with(new NoFieldShadowingRule())
                .with(testToString)
                .build();

        validator.validate(this.getClass().getPackageName(), new FilterPackageInfo(), cl -> {
            final String simpleName = cl.getClazz().getSimpleName();
            return !simpleName.endsWith("BuilderImpl") && !simpleName.endsWith("Builder") && !cl.getSourcePath().contains("/test-classes/");
        });
    }
}