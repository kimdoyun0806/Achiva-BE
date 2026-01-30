-- ============================================================================
-- Achiva Database Baseline Migration
-- Version: 1
-- Description: Initial schema for all existing tables
-- ============================================================================

-- ============================================================================
-- Table: member
-- Description: User account information with Cognito UUID as primary key
-- ============================================================================
CREATE TABLE IF NOT EXISTS member (
    id BINARY(16) NOT NULL,
    email VARCHAR(255) NOT NULL,
    nick_name VARCHAR(255) NOT NULL,
    profile_image_url VARCHAR(255) DEFAULT 'https://achivadata.s3.ap-northeast-2.amazonaws.com/default-profile-image.png',
    birth DATE,
    gender VARCHAR(10),
    region VARCHAR(255),
    description VARCHAR(500),
    role VARCHAR(20),
    push_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id),
    UNIQUE KEY uk_member_email (email),
    INDEX idx_member_email (email),
    INDEX idx_member_is_deleted (is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- Table: member_categories
-- Description: ElementCollection table for Member's categories
-- ============================================================================
CREATE TABLE IF NOT EXISTS member_categories (
    member_id BINARY(16) NOT NULL,
    categories VARCHAR(50),
    CONSTRAINT fk_member_categories_member
        FOREIGN KEY (member_id) REFERENCES member(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- Table: article
-- Description: User-generated articles with category and questions
-- ============================================================================
CREATE TABLE IF NOT EXISTS article (
    id BINARY(16) NOT NULL,
    title VARCHAR(255),
    category VARCHAR(50),
    background_color VARCHAR(50),
    author_category_seq BIGINT NOT NULL,
    member_id BINARY(16),
    created_at DATETIME(6),
    updated_at DATETIME(6),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id),
    INDEX idx_article_member_id (member_id),
    INDEX idx_article_category (category),
    INDEX idx_article_is_deleted (is_deleted),
    CONSTRAINT fk_article_member
        FOREIGN KEY (member_id) REFERENCES member(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- Table: article_photos
-- Description: ElementCollection table for Article photo URLs
-- ============================================================================
CREATE TABLE IF NOT EXISTS article_photos (
    article_id BINARY(16) NOT NULL,
    photo_url VARCHAR(500),
    photo_order INT NOT NULL,
    CONSTRAINT fk_article_photos_article
        FOREIGN KEY (article_id) REFERENCES article(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- Table: question
-- Description: Questions associated with articles
-- ============================================================================
CREATE TABLE IF NOT EXISTS question (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(255),
    content LONGTEXT,
    article_id BINARY(16),
    created_at DATETIME(6),
    updated_at DATETIME(6),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id),
    INDEX idx_question_article_id (article_id),
    INDEX idx_question_is_deleted (is_deleted),
    CONSTRAINT fk_question_article
        FOREIGN KEY (article_id) REFERENCES article(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- Table: book
-- Description: User-created books containing multiple articles
-- ============================================================================
CREATE TABLE IF NOT EXISTS book (
    id BINARY(16) NOT NULL,
    member_id BINARY(16) NOT NULL,
    main_article_id BINARY(16),
    created_at DATETIME(6),
    updated_at DATETIME(6),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id),
    INDEX idx_book_member_id (member_id),
    INDEX idx_book_is_deleted (is_deleted),
    CONSTRAINT fk_book_member
        FOREIGN KEY (member_id) REFERENCES member(id) ON DELETE CASCADE,
    CONSTRAINT fk_book_main_article
        FOREIGN KEY (main_article_id) REFERENCES article(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- Table: book_article
-- Description: Junction table for Book and Article with page ordering
-- ============================================================================
CREATE TABLE IF NOT EXISTS book_article (
    id BIGINT NOT NULL AUTO_INCREMENT,
    book_id BINARY(16) NOT NULL,
    article_id BINARY(16) NOT NULL,
    page_index INT NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_book_article_book_id (book_id),
    INDEX idx_book_article_article_id (article_id),
    CONSTRAINT uk_book_page_index UNIQUE (book_id, page_index),
    CONSTRAINT fk_book_article_book
        FOREIGN KEY (book_id) REFERENCES book(id) ON DELETE CASCADE,
    CONSTRAINT fk_book_article_article
        FOREIGN KEY (article_id) REFERENCES article(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- Table: cheering
-- Description: Encouragement messages between users
-- ============================================================================
CREATE TABLE IF NOT EXISTS cheering (
    id BIGINT NOT NULL AUTO_INCREMENT,
    content LONGTEXT,
    cheering_category VARCHAR(20),
    sender_id BINARY(16) NOT NULL,
    receiver_id BINARY(16) NOT NULL,
    article_id BINARY(16),
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id),
    INDEX idx_cheering_sender_category (sender_id, cheering_category),
    INDEX idx_cheering_receiver_category (receiver_id, cheering_category),
    INDEX idx_cheering_is_deleted (is_deleted),
    CONSTRAINT fk_cheering_sender
        FOREIGN KEY (sender_id) REFERENCES member(id) ON DELETE CASCADE,
    CONSTRAINT fk_cheering_receiver
        FOREIGN KEY (receiver_id) REFERENCES member(id) ON DELETE CASCADE,
    CONSTRAINT fk_cheering_article
        FOREIGN KEY (article_id) REFERENCES article(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- Table: friendship
-- Description: Friend relationships and requests
-- ============================================================================
CREATE TABLE IF NOT EXISTS friendship (
    id BIGINT NOT NULL AUTO_INCREMENT,
    requester_id BINARY(16) NOT NULL,
    receiver_id BINARY(16),
    status VARCHAR(20),
    created_at DATETIME(6),
    updated_at DATETIME(6),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id),
    INDEX idx_friendship_requester_id (requester_id),
    INDEX idx_friendship_receiver_id (receiver_id),
    INDEX idx_friendship_status (status),
    INDEX idx_friendship_is_deleted (is_deleted),
    CONSTRAINT fk_friendship_requester
        FOREIGN KEY (requester_id) REFERENCES member(id) ON DELETE CASCADE,
    CONSTRAINT fk_friendship_receiver
        FOREIGN KEY (receiver_id) REFERENCES member(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- Table: goals
-- Description: User goals categorized by type
-- ============================================================================
CREATE TABLE IF NOT EXISTS goals (
    id BINARY(16) NOT NULL,
    category VARCHAR(20) NOT NULL,
    text VARCHAR(200) NOT NULL,
    click_count INT NOT NULL DEFAULT 0,
    is_archived BOOLEAN NOT NULL DEFAULT FALSE,
    member_id BINARY(16) NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id),
    INDEX idx_goals_member_id (member_id),
    INDEX idx_goals_category (category),
    INDEX idx_goals_is_deleted (is_deleted),
    CONSTRAINT fk_goals_member
        FOREIGN KEY (member_id) REFERENCES member(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- Table: member_category_counter
-- Description: Tracks sequence numbers per member per category
-- ============================================================================
CREATE TABLE IF NOT EXISTS member_category_counter (
    member_id BINARY(16) NOT NULL,
    category VARCHAR(20) NOT NULL,
    size BIGINT NOT NULL DEFAULT 0,
    version BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (member_id, category),
    CONSTRAINT fk_member_category_counter_member
        FOREIGN KEY (member_id) REFERENCES member(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
