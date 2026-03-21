-- 5명의 더미 사용자 추가
-- 김러닝, 이헬스, 박요가, 최걷기, 정마라톤

INSERT INTO member (id, email, nick_name, profile_image_url, role, push_enabled, created_at, updated_at, is_deleted)
VALUES 
(UNHEX(REPLACE(UUID(), '-', '')), 'runner@dummy.com', '김러닝', 'https://achivadata.s3.ap-northeast-2.amazonaws.com/default-profile-image.png', 'USER', 1, NOW(), NOW(), 0),
(UNHEX(REPLACE(UUID(), '-', '')), 'health@dummy.com', '이헬스', 'https://achivadata.s3.ap-northeast-2.amazonaws.com/default-profile-image.png', 'USER', 1, NOW(), NOW(), 0),
(UNHEX(REPLACE(UUID(), '-', '')), 'yoga@dummy.com', '박요가', 'https://achivadata.s3.ap-northeast-2.amazonaws.com/default-profile-image.png', 'USER', 1, NOW(), NOW(), 0),
(UNHEX(REPLACE(UUID(), '-', '')), 'walker@dummy.com', '최걷기', 'https://achivadata.s3.ap-northeast-2.amazonaws.com/default-profile-image.png', 'USER', 1, NOW(), NOW(), 0),
(UNHEX(REPLACE(UUID(), '-', '')), 'marathon@dummy.com', '정마라톤', 'https://achivadata.s3.ap-northeast-2.amazonaws.com/default-profile-image.png', 'USER', 1, NOW(), NOW(), 0);

-- '주 3회 운동반' 모임 ID 가져오기
SET @moim_id = (SELECT id FROM moim WHERE name = '주 3회 운동반' LIMIT 1);

-- 더미 사용자들을 해당 모임에 가입시킴
INSERT INTO moim_member (created_at, updated_at, role, member_id, moim_id, is_deleted)
SELECT NOW(), NOW(), 'MEMBER', id, @moim_id, 0
FROM member 
WHERE email IN ('runner@dummy.com', 'health@dummy.com', 'yoga@dummy.com', 'walker@dummy.com', 'marathon@dummy.com')
  AND NOT EXISTS (
    SELECT 1 FROM moim_member mm 
    WHERE mm.member_id = member.id AND mm.moim_id = @moim_id
  );
