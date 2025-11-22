package tr.com.w124ai.chunker;

import java.util.ArrayList;
import java.util.List;

public class PropertiesChunker implements Chunker {
    @Override
    public List<String> chunk(String content) {
        List<String> lines = List.of(content.split("\n"));
        List<String> chunks = new ArrayList<>();

        StringBuilder buff = new StringBuilder();
        int count = 0;

        for (String line : lines) {
            buff.append(line).append("\n");
            count++;

            if (count >= 20) {
                chunks.add(buff.toString());
                buff.setLength(0);
                count = 0;
            }
        }

        if (buff.length() > 0)
            chunks.add(buff.toString());

        return chunks;
    }
}
