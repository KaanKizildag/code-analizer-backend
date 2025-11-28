package tr.com.analizer.chunker;

import java.util.ArrayList;
import java.util.List;

public class YamlChunker implements Chunker{

    @Override
    public List<String> chunk(String content) {
        List<String> chunks = new ArrayList<>();

        String[] topLevelBlocks = content.split("\n(?=[a-zA-Z0-9_-]+:)");

        for (String block : topLevelBlocks) {
            if (block.length() > 800)
                chunks.addAll(smartSplit(block, 800, 100));
            else
                chunks.add(block);
        }
        return chunks;
    }
}
