package unicon.Achiva.domain.book.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import unicon.Achiva.domain.article.dto.ArticleRequest;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookRequest {
    private String title;
    private String description;
    private ArticleRequest main;
    @Size(min = 1)
    private List<UUID> articleIds;
}