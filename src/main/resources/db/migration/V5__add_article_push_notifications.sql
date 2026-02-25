-- V5: 게시글 푸시 알림 기능 추가
-- 작성일: 2025-02-25
-- 설명: 친구 간 게시글 푸시 알림 설정 및 발송 이력 관리

-- =====================================================
-- 1. Friendship 테이블: 게시글 푸시 알림 설정 컬럼 추가
-- =====================================================
-- requester와 receiver 각각 상대방의 게시글 푸시를 받을지 설정
ALTER TABLE friendship
ADD COLUMN receiver_allows_post_push BOOLEAN DEFAULT TRUE COMMENT 'receiver가 requester의 게시글 푸시를 받을지 여부',
ADD COLUMN requester_allows_post_push BOOLEAN DEFAULT TRUE COMMENT 'requester가 receiver의 게시글 푸시를 받을지 여부';

-- =====================================================
-- 2. Member 테이블: 전역 친구 운동 푸시 설정 컬럼 추가
-- =====================================================
-- 모든 친구의 운동 게시글 푸시 알림을 받을지 전역 설정
ALTER TABLE member
ADD COLUMN friend_workout_push_enabled BOOLEAN DEFAULT TRUE COMMENT '친구 운동 게시글 푸시 알림 전역 설정';

-- =====================================================
-- 3. ArticlePushHistory 테이블: 푸시 발송 이력 관리
-- =====================================================
-- 하루 1회 발송 정책을 위한 이력 테이블
CREATE TABLE article_push_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    author_id BINARY(16) NOT NULL COMMENT '게시글 작성자 ID',
    receiver_id BINARY(16) NOT NULL COMMENT '푸시 수신자 ID',
    push_date DATE NOT NULL COMMENT '푸시 발송 날짜',
    article_id BINARY(16) NOT NULL COMMENT '게시글 ID',
    created_at DATETIME(6) COMMENT '생성 시각',
    updated_at DATETIME(6) COMMENT '수정 시각',
    is_deleted BOOLEAN DEFAULT FALSE COMMENT '소프트 삭제 여부',

    -- 제약조건: 동일 작성자가 동일 수신자에게 같은 날 중복 발송 방지
    CONSTRAINT uk_author_receiver_date UNIQUE (author_id, receiver_id, push_date),

    -- 인덱스: 빠른 조회를 위한 복합 인덱스
    INDEX idx_author_receiver_date (author_id, receiver_id, push_date),

    -- 인덱스: 오래된 데이터 삭제를 위한 인덱스
    INDEX idx_created_at (created_at)
) COMMENT='게시글 푸시 알림 발송 이력 (하루 1회 정책)';

-- =====================================================
-- 4. 기존 데이터 업데이트 (이미 존재하는 friendship, member)
-- =====================================================
-- 기존 friendship 레코드에 기본값 설정 (이미 DEFAULT로 처리됨)
-- 기존 member 레코드에 기본값 설정 (이미 DEFAULT로 처리됨)

-- =====================================================
-- 주의사항
-- =====================================================
-- 1. article_push_history는 30일 이상 된 데이터를 주기적으로 삭제할 수 있습니다.
--    예: DELETE FROM article_push_history WHERE created_at < DATE_SUB(NOW(), INTERVAL 30 DAY);
-- 2. Unique 제약조건으로 중복 발송이 DB 레벨에서 방지됩니다.
-- 3. 푸시 발송 실패 시에도 이력은 저장되어 재발송 방지에 사용됩니다.
