package io.logz.apollo.blockers;

import java.io.IOException;

public interface CrossBlockerFunction {
    void init(String jsonConfiguration) throws IOException;
}
