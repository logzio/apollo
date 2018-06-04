package io.logz.apollo.notifications.mustache.functions;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Function;

public class LowercaseFunction implements Function<String, String> {

    @Override
    public String apply(String text) {
        return StringUtils.isNotBlank(text) ? text.toLowerCase() : StringUtils.EMPTY;
    }

}
