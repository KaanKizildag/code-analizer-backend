package tr.com.analizer.ignore;

import java.nio.file.*;
import java.util.*;

public class IgnoreRules {

    private final List<PathMatcher> matchers = new ArrayList<>();

    public IgnoreRules(List<String> patterns) {
        FileSystem fs = FileSystems.getDefault();
        for (String p : patterns) {
            // .gitignore gibi davranması için "glob:" prefix'liyoruz
            matchers.add(fs.getPathMatcher("glob:" + p));
        }
    }

    public boolean isIgnored(String filePath) {
        Path p = Paths.get(filePath);
        for (PathMatcher m : matchers) {
            if (m.matches(p)) {
                return true;
            }
        }
        return false;
    }
}
