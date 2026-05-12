package work.trade.file.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FileUrlResolver {

    @Value("${file.display.prefix}")
    private String prefix;

    public String resolve(String path) {
        if (path == null) {
            return null;
        }

        if (path.startsWith("http")) {
            return path;
        }

        return prefix + (prefix.endsWith("/") ? "" : "/") + path;
    }
}