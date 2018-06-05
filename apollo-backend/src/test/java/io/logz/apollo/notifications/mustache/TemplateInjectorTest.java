package io.logz.apollo.notifications.mustache;

import org.junit.Test;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

public class TemplateInjectorTest {

    private TemplateInjector templateInjector = new TemplateInjector();

    @Test
    public void testLowercaseFunction() {
        String varName = "testVar";
        String varValue = "testVarValue";

        String template = String.format("{{#lowercase}}{{%s}}{{/lowercase}}", varName);
        String result = templateInjector.injectToTemplate(template, singletonMap(varName, varValue));
        assertThat(result).isEqualTo(varValue.toLowerCase());
    }

}
