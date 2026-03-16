-- 시스템 유저(System)가 없으면 생성하거나, 그냥 관리자 없이 모임만 생성
INSERT INTO moim (created_at, updated_at, description, is_official, is_private, max_member, name, poke_days, target_amount, is_deleted)
VALUES (NOW(), NOW(), '꾸준함이 답이다! 일주일에 3번 이상 운동 기록하기 🏃‍♂️', 1, 0, 10000, '주 3회 운동반', 3, 10000, 0);

-- 카테고리 추가
SELECT @moim_id := id FROM moim WHERE name = '주 3회 운동반' LIMIT 1;
INSERT INTO moim_categories (moim_id, categories) VALUES (@moim_id, 'RUNNING');
INSERT INTO moim_categories (moim_id, categories) VALUES (@moim_id, 'BODYWEIGHT');
INSERT INTO moim_categories (moim_id, categories) VALUES (@moim_id, 'CROSSFIT');
