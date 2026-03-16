-- 주 3회 운동반의 카테고리(러닝, 맨몸운동, 크로스핏)를 삭제합니다.

DELETE FROM moim_categories 
WHERE moim_id = (SELECT id FROM moim WHERE name = '주 3회 운동반' LIMIT 1);
