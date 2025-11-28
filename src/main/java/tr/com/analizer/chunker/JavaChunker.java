package tr.com.analizer.chunker;

import java.util.ArrayList;
import java.util.List;

public class JavaChunker implements Chunker {

    @Override
    public List<String> chunk(String content) {
        List<String> chunks = new ArrayList<>();
        content = content.replace("\r\n", "\n");

        String[] classes = content.split("(?=public class|class |interface |enum )");

        for (String cls : classes) {
            if (cls.length() < 50) continue;

            String[] methods = cls.split("(?=public |private |protected |static )");

            for (String m : methods) {
                if (m.length() < 80) continue;

                if (m.length() > 2000) {
                    chunks.addAll(smartSplit(m, 1200, 200));
                } else {
                    chunks.add(m);
                }
            }
        }
        return chunks;
    }
}
