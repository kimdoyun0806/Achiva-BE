-- ============================================================================
-- Achiva Database Baseline Migration
-- Version: 1
-- Description: Baseline schema aligned with the current JPA entities
-- ============================================================================

CREATE TABLE IF NOT EXISTS member (
    id BINARY(16) NOT NULL,
    email VARCHAR(255) NOT NULL,
    nick_name VARCHAR(255) NOT NULL,
    profile_image_url VARCHAR(255),
    birth DATE,
    gender VARCHAR(255),
    region VARCHAR(255),
    description VARCHAR(500),
    role VARCHAR(20),
    push_enabled BIT(1) NOT NULL,
    friend_workout_push_enabled BIT(1) NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    is_deleted BIT(1) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_member_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS article (
    id BINARY(16) NOT NULL,
    title VARCHAR(255),
    category VARCHAR(50),
    background_color VARCHAR(255),
    author_category_seq BIGINT NOT NULL,
    weekly_workout_count INT,
    continuous_goal_weeks INT,
    member_id BINARY(16),
    created_at DATETIME(6),
    updated_at DATETIME(6),
    is_deleted BIT(1) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_article_member_id (member_id),
    KEY idx_article_category (category),
    CONSTRAINT fk_article_member FOREIGN KEY (member_id) REFERENCES member (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS article_photos (
    article_id BINARY(16) NOT NULL,
    photo_order INT NOT NULL,
    photo_url VARCHAR(255),
    PRIMARY KEY (article_id, photo_order),
    CONSTRAINT fk_article_photos_article FOREIGN KEY (article_id) REFERENCES article (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS question (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(255),
    content LONGTEXT,
    article_id BINARY(16),
    created_at DATETIME(6),
    updated_at DATETIME(6),
    is_deleted BIT(1) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_question_article_id (article_id),
    CONSTRAINT fk_question_article FOREIGN KEY (article_id) REFERENCES article (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS book (
    id BINARY(16) NOT NULL,
    member_id BINARY(16) NOT NULL,
    main_article_id BINARY(16),
    created_at DATETIME(6),
    updated_at DATETIME(6),
    is_deleted BIT(1) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_book_member_id (member_id),
    KEY idx_book_main_article_id (main_article_id),
    CONSTRAINT fk_book_member FOREIGN KEY (member_id) REFERENCES member (id) ON DELETE CASCADE,
    CONSTRAINT fk_book_main_article FOREIGN KEY (main_article_id) REFERENCES article (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS book_article (
    id BIGINT NOT NULL AUTO_INCREMENT,
    book_id BINARY(16) NOT NULL,
    article_id BINARY(16) NOT NULL,
    page_index INT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_book_page_index (book_id, page_index),
    KEY idx_book_article_book_id (book_id),
    KEY idx_book_article_article_id (article_id),
    CONSTRAINT fk_book_article_book FOREIGN KEY (book_id) REFERENCES book (id) ON DELETE CASCADE,
    CONSTRAINT fk_book_article_article FOREIGN KEY (article_id) REFERENCES article (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS cheering (
    id BIGINT NOT NULL AUTO_INCREMENT,
    content LONGTEXT,
    cheering_category VARCHAR(20),
    sender_id BINARY(16) NOT NULL,
    receiver_id BINARY(16) NOT NULL,
    article_id BINARY(16),
    is_read BIT(1) NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    is_deleted BIT(1) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_cheering_sender_category (sender_id, cheering_category),
    KEY idx_cheering_receiver_category (receiver_id, cheering_category),
    KEY idx_cheering_article_id (article_id),
    CONSTRAINT fk_cheering_sender FOREIGN KEY (sender_id) REFERENCES member (id) ON DELETE CASCADE,
    CONSTRAINT fk_cheering_receiver FOREIGN KEY (receiver_id) REFERENCES member (id) ON DELETE CASCADE,
    CONSTRAINT fk_cheering_article FOREIGN KEY (article_id) REFERENCES article (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS friendship (
    id BIGINT NOT NULL AUTO_INCREMENT,
    requester_id BINARY(16) NOT NULL,
    receiver_id BINARY(16),
    status VARCHAR(255),
    receiver_allows_post_push BIT(1) NOT NULL,
    requester_allows_post_push BIT(1) NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    is_deleted BIT(1) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_friendship_requester_id (requester_id),
    KEY idx_friendship_receiver_id (receiver_id),
    CONSTRAINT fk_friendship_requester FOREIGN KEY (requester_id) REFERENCES member (id) ON DELETE CASCADE,
    CONSTRAINT fk_friendship_receiver FOREIGN KEY (receiver_id) REFERENCES member (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS goals (
    id BINARY(16) NOT NULL,
    category VARCHAR(20) NOT NULL,
    text VARCHAR(200) NOT NULL,
    click_count INT NOT NULL,
    is_archived BIT(1) NOT NULL,
    member_id BINARY(16) NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    is_deleted BIT(1) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_goals_member_id (member_id),
    CONSTRAINT fk_goals_member FOREIGN KEY (member_id) REFERENCES member (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS member_category_counter (
    member_id BINARY(16) NOT NULL,
    category VARCHAR(20) NOT NULL,
    version BIGINT NOT NULL,
    size BIGINT NOT NULL,
    PRIMARY KEY (member_id, category),
    CONSTRAINT fk_member_category_counter_member FOREIGN KEY (member_id) REFERENCES member (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS push_token (
    id BINARY(16) NOT NULL,
    member_id BINARY(16) NOT NULL,
    expo_push_token VARCHAR(200) NOT NULL,
    device_info VARCHAR(255),
    is_active BIT(1) NOT NULL,
    last_used_at DATETIME(6),
    created_at DATETIME(6),
    updated_at DATETIME(6),
    is_deleted BIT(1) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_push_token_member_token (member_id, expo_push_token),
    KEY idx_push_token_member_id (member_id),
    KEY idx_push_token_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS link_token (
    id BINARY(16) NOT NULL,
    jti VARCHAR(36) NOT NULL,
    member_id BINARY(16) NOT NULL,
    used BIT(1) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    is_deleted BIT(1) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_link_token_jti (jti),
    KEY idx_link_token_jti (jti),
    KEY idx_link_token_expires_at (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS article_push_history (
    id BIGINT NOT NULL AUTO_INCREMENT,
    author_id BINARY(16) NOT NULL,
    receiver_id BINARY(16) NOT NULL,
    push_date DATE NOT NULL,
    article_id BINARY(16) NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    is_deleted BIT(1) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_author_receiver_date (author_id, receiver_id, push_date),
    KEY idx_author_receiver_date (author_id, receiver_id, push_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS moim (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    max_member INT NOT NULL,
    is_private BIT(1) NOT NULL,
    password VARCHAR(255),
    is_official BIT(1) NOT NULL,
    target_amount INT NOT NULL,
    poke_days INT NOT NULL,
    score INT NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    is_deleted BIT(1) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS moim_member (
    id BIGINT NOT NULL AUTO_INCREMENT,
    moim_id BIGINT NOT NULL,
    member_id BINARY(16) NOT NULL,
    role VARCHAR(20) NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    is_deleted BIT(1) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_moim_member (moim_id, member_id),
    KEY idx_moim_member_member_id (member_id),
    CONSTRAINT fk_moim_member_moim FOREIGN KEY (moim_id) REFERENCES moim (id),
    CONSTRAINT fk_moim_member_member FOREIGN KEY (member_id) REFERENCES member (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS moim_score (
    id BIGINT NOT NULL AUTO_INCREMENT,
    moim_id BIGINT NOT NULL,
    member_id BINARY(16) NOT NULL,
    score INT NOT NULL,
    left_at DATETIME(6),
    created_at DATETIME(6),
    updated_at DATETIME(6),
    is_deleted BIT(1) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_moim_score_moim_id (moim_id),
    KEY idx_moim_score_member_id (member_id),
    CONSTRAINT fk_moim_score_moim FOREIGN KEY (moim_id) REFERENCES moim (id) ON DELETE CASCADE,
    CONSTRAINT fk_moim_score_member FOREIGN KEY (member_id) REFERENCES member (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
