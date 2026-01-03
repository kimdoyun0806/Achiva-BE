package unicon.Achiva.domain.member.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import unicon.Achiva.domain.category.Category;
import unicon.Achiva.domain.member.entity.MemberCategoryCounter;
import unicon.Achiva.domain.member.entity.MemberCategoryKey;

import java.util.List;
import java.util.UUID;

/**
 * {@code CounterHelper}는 회원별-카테고리별 게시글 카운터 {@link MemberCategoryCounter}의
 * 동시성 제어 및 초기화를 담당하는 유틸리티 서비스입니다.
 * <p>
 * 이 클래스는 주로 {@code ArticleService} 내에서 사용되어,
 * 게시글 생성/이동/삭제 시 각 카테고리의 게시글 개수(size)를 안전하게 증가·감소시킵니다.
 * <br>
 * 내부적으로 {@link MemberCategoryCounterRepository#lockById(MemberCategoryKey)}를 통해
 * 비관적 락(PESSIMISTIC_WRITE)을 획득하여 다중 트랜잭션 환경에서도 데이터 정합성을 보장합니다.
 */
@Service
@RequiredArgsConstructor
public class CounterHelper {

    private final MemberCategoryCounterRepository counterRepo;
    private final MemberRepository memberRepo;

    /**
     * 지정된 회원 ID와 카테고리에 해당하는 {@link MemberCategoryCounter} 엔티티를
     * 비관적 락(PESSIMISTIC_WRITE)으로 조회하거나, 존재하지 않을 경우 새로 생성합니다.
     * <p>
     * 이 메서드는 게시글 생성, 삭제, 카테고리 이동 등의 시점에서 호출되어
     * 해당 카테고리의 현재 size 값을 읽고 갱신하기 위한 안전한 락을 제공합니다.
     *
     * @param memberId 카운터를 조회할 회원의 식별자
     * @param category 카운터를 조회할 카테고리
     * @return 잠금된(혹은 새로 생성된) {@link MemberCategoryCounter} 엔티티
     */
    @Transactional
    public MemberCategoryCounter lockOrInit(UUID memberId, Category category) {
        MemberCategoryKey key = new MemberCategoryKey(memberId, category);
        return counterRepo.lockById(key).orElseGet(() -> {
            MemberCategoryCounter c = new MemberCategoryCounter();
            c.setId(key);
            c.setMember(memberRepo.getReferenceById(memberId));
            c.setSize(0L);
            return counterRepo.saveAndFlush(c);
        });
    }

    /**
     * 두 개의 카테고리에 대해 항상 동일한 락 획득 순서를 보장하기 위해,
     * 이름 기준으로 정렬된 {@link MemberCategoryKey} 목록을 반환합니다.
     * <p>
     * 이 메서드는 트랜잭션 간 교착상태(Deadlock)를 예방하기 위해 사용됩니다.
     *
     * @param memberId 회원 식별자
     * @param a        첫 번째 카테고리
     * @param b        두 번째 카테고리
     * @return 사전순으로 정렬된 {@link MemberCategoryKey} 두 개의 리스트
     */
    public List<MemberCategoryKey> orderedKeys(UUID memberId, Category a, Category b) {
        Category first = a.name().compareTo(b.name()) <= 0 ? a : b;
        Category second = (first == a) ? b : a;
        return List.of(
                new MemberCategoryKey(memberId, first),
                new MemberCategoryKey(memberId, second)
        );
    }
}
