package tr.com.w124ai.chunker;

import java.util.ArrayList;
import java.util.List;

public class MarkdownChunker implements Chunker {

    @Override
    public List<String> chunk(String content) {
        List<String> chunks = new ArrayList<>();

        String[] sections = content.split("\n(?=# )");

        for (String sec : sections) {
            if (sec.length() > 1200)
                chunks.addAll(smartSplit(sec, 1000, 150));
            else
                chunks.add(sec);
        }

        return chunks;
    }
}
