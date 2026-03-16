package unicon.Achiva.domain.moim.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import unicon.Achiva.domain.category.Category;

import java.util.List;

@Getter
@NoArgsConstructor
public class MoimCreateRequest {
    private String name;
    private String description;
    private int maxMember;
    private boolean isPrivate;
    private String password;
    private List<Category> categories;
}
