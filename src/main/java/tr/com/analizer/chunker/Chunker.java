package tr.com.analizer.chunker;

import java.util.ArrayList;
import java.util.List;

public interface Chunker {
    List<String> chunk(String content);


    default List<String> smartSplit(String text, int max, int overlap) {
        List<String> chunks = new ArrayList<>();
        int start = 0;

        while (start < text.length()) {
            int end = Math.min(text.length(), start + max);
            chunks.add(text.substring(start, end));
            start = Math.max(end - overlap, start + max);
        }
        return chunks;
    }

}
