package io.logz.apollo.notifications.mustache;

import com.github.mustachejava.Mustache;
import io.logz.apollo.notifications.mustache.functions.LowercaseFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import static java.util.Collections.singletonMap;

public class TemplateInjector {
    private static final Logger logger = LoggerFactory.getLogger(TemplateInjector.class);
    private static final LowercaseFunction lowercaseFunction = new LowercaseFunction();
    private final MustacheFactoryUnescapeHtml mustacheFactoryUnescapeHtml = new MustacheFactoryUnescapeHtml();

    public String injectToTemplate(String template, Object scope) {
        Mustache mustache = mustacheFactoryUnescapeHtml.compile(new StringReader(template), "template");

        StringWriter result = new StringWriter();
        try {
            mustache.execute(result, new Object[] { scope, singletonMap("lowercase", lowercaseFunction) }).flush();
            return result.toString();
        } catch (IOException e) {
            String msg = String.format("Failed to inject values to template template=%s, scope=%s", template, scope);
            logger.warn(msg, e);
            throw new RuntimeException(msg, e);
        }
    }

}
