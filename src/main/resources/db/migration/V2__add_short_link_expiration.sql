-- V2__add_short_link_expiration.sql
-- Add nullable expires_at to short_links to support optional expiration

ALTER TABLE short_links
  ADD COLUMN expires_at TIMESTAMP WITH TIME ZONE NULL;

-- Index to support queries by expiration time
CREATE INDEX IF NOT EXISTS ix_short_links_expires_at ON short_links (expires_at);
