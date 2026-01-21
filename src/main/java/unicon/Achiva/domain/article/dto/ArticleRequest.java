package unicon.Achiva.domain.article.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import org.hibernate.validator.constraints.URL;
import unicon.Achiva.domain.article.entity.Question;
import unicon.Achiva.domain.category.Category;
import unicon.Achiva.global.validation.ValidHexColor;

import java.util.List;

public record ArticleRequest(@Size(max = 5, message = "사진은 최대 5개까지 업로드 가능합니다") List<@URL(protocol = "https") String> photoUrls, @NotNull @Size(min = 1, max = 50) String title,
                             Category category, List<QuestionDTO> question, @ValidHexColor String backgroundColor) {

    public ArticleRequest(List<String> photoUrls, String title, Category category, List<QuestionDTO> question, String backgroundColor) {
        this.photoUrls = photoUrls;
        this.title = title;
        this.category = category;
        this.question = question;
        this.backgroundColor = backgroundColor;
    }

    @Getter
    @Builder
    public static class QuestionDTO {
        private String question;
        private String content;

        public QuestionDTO(String question, String content) {
            this.question = question;
            this.content = content;
        }

        public static Question toEntity(QuestionDTO questionDTO) {
            return Question.builder()
                    .title(questionDTO.getQuestion())
                    .content(questionDTO.getContent())
                    .build();
        }

        public static QuestionDTO fromEntity(Question question) {
            return QuestionDTO.builder()
                    .question(question.getTitle())
                    .content(question.getContent())
                    .build();
        }
    }
}
