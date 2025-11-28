package tr.com.analizer.chunker;

import java.util.List;

public class ChunkerFactory {

    public static Chunker getChunker(String fileName) {

        if (fileName.endsWith(".java"))
            return new JavaChunker();

        if (fileName.endsWith(".yml") || fileName.endsWith(".yaml"))
            return new YamlChunker();

        if (fileName.endsWith(".properties"))
            return new PropertiesChunker();

        if (fileName.endsWith(".xml") || fileName.endsWith(".pom"))
            return new XmlChunker();

        if (fileName.endsWith(".md"))
            return new MarkdownChunker();

        return new Chunker() {
            @Override
            public List<String> chunk(String content) {
                return smartSplit(content, 1000, 150);
            }
        };
    }
}
