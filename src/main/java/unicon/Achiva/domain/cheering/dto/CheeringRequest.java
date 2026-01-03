package unicon.Achiva.domain.cheering.dto;

import lombok.Getter;
import unicon.Achiva.domain.cheering.CheeringCategory;

@Getter
public class CheeringRequest {
    private String content;
    private CheeringCategory cheeringCategory;
}
