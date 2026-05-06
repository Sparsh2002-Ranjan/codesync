-- FIX 5: Add username column to session_participants so participant names
-- are visible in the UI. Run this once against your existing database.
-- (If you are starting fresh, Hibernate ddl-auto=update will create it automatically.)
ALTER TABLE session_participants
    ADD COLUMN IF NOT EXISTS username VARCHAR(100) NULL AFTER user_id;
