package unicon.Achiva.global.config;

import org.springframework.core.convert.converter.Converter;
import unicon.Achiva.domain.category.Category;

public class CategoryConverter implements Converter<String, Category> {

    @Override
    public Category convert(String source) {
        if (source == null || source.isBlank()) {
            return null;
        }
        return Category.fromDisplayName(source);
    }
}
