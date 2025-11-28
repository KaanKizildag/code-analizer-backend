package tr.com.analizer.document;

import java.util.ArrayList;
import java.util.List;

public class TextSplitter {
    public static List<String> split(String text, int chunkSize) {
        List<String> chunks = new ArrayList<>();
        for (int i = 0; i < text.length(); i += chunkSize) {
            int end = Math.min(text.length(), i + chunkSize);
            chunks.add(text.substring(i, end));
        }
        return chunks;
    }
}
