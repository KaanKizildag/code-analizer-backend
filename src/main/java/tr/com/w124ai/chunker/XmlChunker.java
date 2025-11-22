package tr.com.w124ai.chunker;

import java.util.ArrayList;
import java.util.List;

public class XmlChunker implements Chunker {
    @Override
    public List<String> chunk(String content) {
        List<String> chunks = new ArrayList<>();

        String[] tags = content.split("(?=<[^/].*?>)");

        for (String t : tags) {
            if (t.length() > 1200)
                chunks.addAll(smartSplit(t, 1000, 100));
            else
                chunks.add(t);
        }

        return chunks;
    }
}
