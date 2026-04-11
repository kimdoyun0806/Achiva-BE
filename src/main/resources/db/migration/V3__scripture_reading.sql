CREATE TABLE IF NOT EXISTS member_scripture_progress (
    id BINARY(16) NOT NULL,
    member_id BINARY(16) NOT NULL,
    scripture_id VARCHAR(50) NOT NULL,
    completed_chapters INT NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    is_deleted BIT(1) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_member_scripture_progress (member_id, scripture_id),
    KEY idx_member_scripture_progress_member_id (member_id),
    KEY idx_member_scripture_progress_updated_at (updated_at),
    CONSTRAINT fk_member_scripture_progress_member FOREIGN KEY (member_id) REFERENCES member (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS article_scripture_reading (
    article_id BINARY(16) NOT NULL,
    scripture_id VARCHAR(50) NOT NULL,
    start_chapter INT NOT NULL,
    end_chapter INT NOT NULL,
    completed_chapters INT NOT NULL,
    read_at DATE NOT NULL,
    PRIMARY KEY (article_id),
    KEY idx_article_scripture_reading_scripture_id (scripture_id),
    KEY idx_article_scripture_reading_read_at (read_at),
    CONSTRAINT fk_article_scripture_reading_article FOREIGN KEY (article_id) REFERENCES article (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
