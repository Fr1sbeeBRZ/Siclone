-- ============================================================
-- Inicialización de PostgreSQL (solo se ejecuta la PRIMERA vez,
-- cuando el volumen de datos está vacío).
--
-- Aquí solo va lo que Flyway no debe gestionar: extensiones,
-- esquemas, roles o bases de datos adicionales.
-- Las TABLAS de la aplicación se crean con migraciones Flyway en
-- src/main/resources/db/migration (V1__estructura_base.sql, ...).
-- ============================================================

-- Extensiones útiles para la aplicación
CREATE EXTENSION IF NOT EXISTS pgcrypto;   -- gen_random_uuid(), hashes
CREATE EXTENSION IF NOT EXISTS unaccent;   -- búsquedas sin tildes
CREATE EXTENSION IF NOT EXISTS pg_trgm;    -- búsquedas parciales (LIKE '%...%') con índices

-- Base de datos para Keycloak (cuando lo añadas al compose)
-- CREATE DATABASE keycloak;
