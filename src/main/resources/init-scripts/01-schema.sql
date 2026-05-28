-- Users
CREATE TABLE IF NOT EXISTS users (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(100) NOT NULL UNIQUE,
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- API Keys
CREATE TABLE IF NOT EXISTS api_keys (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT NOT NULL,
    api_key     VARCHAR(100) NOT NULL UNIQUE,
    name        VARCHAR(100),
    active      BOOLEAN DEFAULT TRUE,
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user (user_id),
    INDEX idx_api_key (api_key)
);

-- Wiki pages
CREATE TABLE IF NOT EXISTS wiki_pages (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    title           TEXT,
    arxiv_id        VARCHAR(20) NOT NULL,
    authors         TEXT,
    published       DATE,
    abstract_text   TEXT,
    wiki_md         LONGTEXT,
    tags            TEXT,
    embedding       VECTOR(1024),
    auto_generated_at DATETIME,
    manual_updated_at DATETIME,
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE INDEX idx_unique_arxiv (arxiv_id),
    FULLTEXT INDEX idx_fts_tags (tags) WITH PARSER ik,
    VECTOR INDEX idx_vec (embedding) WITH (DISTANCE=cosine, TYPE=hnsw, LIB=vsag)
);

-- Paper chunks (vector search granularity)
CREATE TABLE IF NOT EXISTS paper_chunk (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    arxiv_id    VARCHAR(20) NOT NULL,
    chunk_id    VARCHAR(50) NOT NULL,
    content     TEXT NOT NULL,
    embedding   VECTOR(1024),
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_chunk (arxiv_id, chunk_id),
    INDEX idx_arxiv (arxiv_id),
    VECTOR INDEX idx_vec (embedding) WITH (DISTANCE=cosine, TYPE=hnsw, LIB=vsag)
);

-- Annotations (paper-level notes, GitHub Issues style)
CREATE TABLE IF NOT EXISTS annotations (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    arxiv_id    VARCHAR(20) NOT NULL,
    user_id     BIGINT NOT NULL,
    content     TEXT NOT NULL,
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_arxiv (arxiv_id),
    INDEX idx_user (user_id)
);
