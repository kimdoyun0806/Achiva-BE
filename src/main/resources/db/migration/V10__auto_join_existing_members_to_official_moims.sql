-- 모든 기존 사용자를 공식 모임에 가입시킵니다 (이미 가입된 경우 제외)
-- role은 'MEMBER'로 설정

INSERT INTO moim_member (created_at, updated_at, role, member_id, moim_id, is_deleted)
SELECT NOW(), NOW(), 'MEMBER', m.id, mo.id, 0
FROM member m
CROSS JOIN moim mo
WHERE mo.is_official = 1
  AND NOT EXISTS (
    SELECT 1 FROM moim_member mm 
    WHERE mm.member_id = m.id AND mm.moim_id = mo.id
  );
