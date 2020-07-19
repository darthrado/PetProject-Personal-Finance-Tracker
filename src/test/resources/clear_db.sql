--drop tables
DROP TABLE IF EXISTS target_details;
DROP TABLE IF EXISTS targets;
DROP TABLE IF EXISTS movements;
DROP TABLE IF EXISTS movement_categories;
DROP TABLE IF EXISTS users;

--drop sequences
DROP SEQUENCE IF EXISTS seq_users;
DROP SEQUENCE IF EXISTS seq_categories;
DROP SEQUENCE IF EXISTS seq_movements;
DROP SEQUENCE IF EXISTS seq_targets;
DROP SEQUENCE IF EXISTS seq_target_details;