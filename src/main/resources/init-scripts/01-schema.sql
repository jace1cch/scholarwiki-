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
    id              VARCHAR(50) PRIMARY KEY,
    type            VARCHAR(20) NOT NULL,
    title           TEXT,
    arxiv_id        VARCHAR(20),
    authors         TEXT,
    published       DATE,
    categories      TEXT,
    abstract_text   TEXT,
    report_q1       TEXT,
    report_q2       TEXT,
    report_q3       TEXT,
    report_q4       TEXT,
    report_q5       TEXT,
    concepts        TEXT,
    wiki_md         LONGTEXT,
    embedding       VECTOR(2048),
    tags            TEXT,
    manual_overrides TEXT,
    auto_generated_at DATETIME,
    manual_updated_at DATETIME,
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FULLTEXT INDEX idx_fts (title, wiki_md) WITH PARSER ik,
    VECTOR INDEX idx_vec (embedding) WITH (DISTANCE=cosine, TYPE=hnsw, LIB=vsag)
);

-- Paper chunks for fine-grained retrieval
CREATE TABLE IF NOT EXISTS paper_chunks (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    arxiv_id    VARCHAR(20),
    section     VARCHAR(50),
    content     TEXT,
    embedding   VECTOR(2048),
    INDEX idx_arxiv (arxiv_id),
    VECTOR INDEX idx_vec (embedding) WITH (DISTANCE=cosine, TYPE=hnsw, LIB=vsag)
);

-- Annotations
CREATE TABLE IF NOT EXISTS annotations (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    arxiv_id    VARCHAR(20),
    user_id     BIGINT,
    section_id  VARCHAR(50),
    content     TEXT,
    embedding   VECTOR(2048),
    merged      BOOLEAN DEFAULT FALSE,
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_arxiv (arxiv_id),
    INDEX idx_user (user_id),
    VECTOR INDEX idx_vec (embedding) WITH (DISTANCE=cosine, TYPE=hnsw, LIB=vsag)
);

-- Wiki nodes (3-level category tree)
CREATE TABLE IF NOT EXISTS wiki_nodes (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(100) NOT NULL,
    level           TINYINT NOT NULL,
    parent_id       BIGINT,
    arxiv_category  VARCHAR(20),
    description_md  LONGTEXT,
    embedding       VECTOR(2048),
    paper_count     INT DEFAULT 0,
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_parent (parent_id),
    INDEX idx_level (level),
    FULLTEXT INDEX idx_fts_name (name) WITH PARSER ik,
    VECTOR INDEX idx_vec (embedding) WITH (DISTANCE=cosine, TYPE=hnsw, LIB=vsag)
);

-- Paper-category associations
CREATE TABLE IF NOT EXISTS paper_categories (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    arxiv_id    VARCHAR(20) NOT NULL,
    node_id     BIGINT NOT NULL,
    assigned_by VARCHAR(20) DEFAULT 'auto',
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_paper_node (arxiv_id, node_id),
    INDEX idx_arxiv (arxiv_id),
    INDEX idx_node (node_id)
);
