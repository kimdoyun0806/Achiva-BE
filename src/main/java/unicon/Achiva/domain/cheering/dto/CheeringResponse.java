package unicon.Achiva.domain.cheering.dto;

import lombok.Builder;
import lombok.Getter;
import unicon.Achiva.domain.cheering.CheeringCategory;
import unicon.Achiva.domain.cheering.entity.Cheering;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class CheeringResponse {
    private Long id;
    private String content;
    private CheeringCategory cheeringCategory;
    private UUID senderId;
    private String senderName;
    private String senderProfileImageUrl;
    private UUID receiverId;
    private String receiverName;
    private UUID articleId;
    private Boolean isRead;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CheeringResponse fromEntity(Cheering cheering) {
        return CheeringResponse.builder()
                .id(cheering.getId())
                .content(cheering.getContent())
                .cheeringCategory(cheering.getCheeringCategory())
                .senderId(cheering.getSender().getId())
                .senderName(cheering.getSender().getNickName())
                .senderProfileImageUrl(cheering.getSender().getProfileImageUrl())
                .receiverId(cheering.getReceiver().getId())
                .receiverName(cheering.getReceiver().getNickName())
                .articleId(cheering.getArticle().getId())
                .isRead(cheering.getIsRead())
                .createdAt(cheering.getCreatedAt())
                .updatedAt(cheering.getUpdatedAt())
                .build();
    }
}
