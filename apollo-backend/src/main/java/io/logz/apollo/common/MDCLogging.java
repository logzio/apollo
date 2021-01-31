package io.logz.apollo.common;

import org.slf4j.MDC;

import java.util.Map;

public class MDCLogging {
    public static void withMDCFields(Map<String, String> mdcFields, Runnable block) {
        try {
            for (String fieldName : mdcFields.keySet()) {
                MDC.put(fieldName, mdcFields.get(fieldName));
            }

            block.run();
        } finally {
            for (String fieldName : mdcFields.keySet()) {
                MDC.remove(fieldName);
            }
        }
    }
}
