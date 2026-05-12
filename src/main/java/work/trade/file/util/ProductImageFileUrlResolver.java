package work.trade.file.util;

import org.springframework.stereotype.Component;

@Component
public class ProductImageFileUrlResolver extends FileUrlResolver{
    @Override
    public String resolve(String path) {
        if (path == null) {
            return super.resolve("images/default_thumbnail.jpg");
        }
        return super.resolve(path);
    }
}
