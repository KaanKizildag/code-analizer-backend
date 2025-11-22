package tr.com.w124ai.ignore;

import java.io.*;
import java.util.*;

public class IgnoreFileLoader {

    public static IgnoreRules loadIgnoreFile(File ignoreFile) throws IOException {
        List<String> patterns = new ArrayList<>();

        if (!ignoreFile.exists()) {
            return new IgnoreRules(patterns);
        }

        try (BufferedReader br = new BufferedReader(new FileReader(ignoreFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                patterns.add(line);
            }
        }

        return new IgnoreRules(patterns);
    }
}
