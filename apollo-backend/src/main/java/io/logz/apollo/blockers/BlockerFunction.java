package io.logz.apollo.blockers;

import java.io.IOException;

public interface BlockerFunction {
    void init(String jsonConfiguration) throws IOException;
}
