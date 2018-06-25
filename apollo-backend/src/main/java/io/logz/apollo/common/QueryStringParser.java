package io.logz.apollo.common;

import com.google.common.base.Splitter;

import java.util.Map;

/**
 * Created by roiravhon on 5/29/17.
 */
public class QueryStringParser {
    public static int getIntFromQueryString(String queryString, String key) {
        Map<String, String> queryStringParams = getQueryStringMap(queryString);
        return Integer.parseInt(queryStringParams.get(key));
    }

    public static Map<String, String> getQueryStringMap(String queryString) {
        return Splitter.on('&').trimResults().withKeyValueSeparator('=').split(queryString);
    }
}
