package ${package};

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = ${sample}TestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ${sample}Test {

    @Test
    public void test() {

    }

}
