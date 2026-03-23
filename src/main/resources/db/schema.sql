CREATE EXTENSION IF NOT EXISTS postgis;

CREATE TABLE IF NOT EXISTS food_stalls (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    address VARCHAR(500),
    description TEXT,
    audio_url VARCHAR(500),
    image_url VARCHAR(500),
    location GEOGRAPHY(Point, 4326),
    trigger_radius INTEGER DEFAULT 15,
    min_price INTEGER,
    max_price INTEGER,
    audio_duration INTEGER,
    featured_reviews JSONB,
    rating DOUBLE PRECISION,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);



CREATE INDEX IF NOT EXISTS idx_food_stalls_location 
ON food_stalls USING GIST(location);

CREATE TABLE IF NOT EXISTS food_stall_localizations (
    id BIGSERIAL PRIMARY KEY,
    food_stall_id BIGINT NOT NULL,
    language_code VARCHAR(10) NOT NULL,         -- vi, en, ja, ko, zh
    name VARCHAR(255),
    description TEXT,
    audio_url VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_localization_stall FOREIGN KEY (food_stall_id) REFERENCES food_stalls(id) ON DELETE CASCADE,
    CONSTRAINT uq_stall_lang UNIQUE (food_stall_id, language_code)
);

CREATE INDEX IF NOT EXISTS idx_localization_stall_lang 
ON food_stall_localizations (food_stall_id, language_code);

