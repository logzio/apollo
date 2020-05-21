package io.logz.apollo.common;

import com.google.common.base.Splitter;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by roiravhon on 5/29/17.
 */
public class StringParser {
    public static int getIntFromQueryString(String queryString, String key) {
        Map<String, String> queryStringParams = getQueryStringMap(queryString);
        return Integer.parseInt(queryStringParams.get(key));
    }

    public static Map<String, String> getQueryStringMap(String queryString) {
        if(queryString.trim().isEmpty())
            return Collections.emptyMap();
        return Splitter.on('&').trimResults().withKeyValueSeparator('=').split(queryString);
    }

    public static List<String> splitCsvToStringList(String csv) {
        return Splitter
                .on(",")
                .omitEmptyStrings()
                .trimResults()
                .splitToList(csv)
                .stream()
                .map(String::toString)
                .collect(Collectors.toList());
    }

    public static List<Integer> splitCsvToIntegerList(String csv) {
        return Splitter
                .on(",")
                .omitEmptyStrings()
                .trimResults()
                .splitToList(csv)
                .stream()
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }

    public static <T> String createCsvFromList(List<T> list) {
        return StringUtils.join(list, ",");
    }
}
