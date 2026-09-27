-- ============================================================
-- Event Publication Registry de Spring Modulith.
--
-- Guarda los eventos de dominio publicados y, para cada listener,
-- si ya se han procesado. Si un módulo falla, su publicación queda
-- pendiente y se puede reprocesar sin perder el evento.
--
-- Esquema tomado de Spring Modulith 2.1 (schemas/v2/schema-postgresql.sql,
-- el mismo que usa la implementación JPA: entidad EVENT_PUBLICATION).
-- Si en el futuro se cambia spring.modulith.events.completion-mode
-- a ARCHIVE, hará falta además la tabla event_publication_archive.
-- ============================================================

CREATE TABLE event_publication
(
    id                     UUID                     NOT NULL,
    listener_id            TEXT                     NOT NULL,
    event_type             TEXT                     NOT NULL,
    serialized_event       TEXT                     NOT NULL,
    publication_date       TIMESTAMP WITH TIME ZONE NOT NULL,
    completion_date        TIMESTAMP WITH TIME ZONE,
    status                 TEXT,
    completion_attempts    INT,
    last_resubmission_date TIMESTAMP WITH TIME ZONE,
    PRIMARY KEY (id)
);

CREATE INDEX event_publication_serialized_event_hash_idx ON event_publication USING hash (serialized_event);
CREATE INDEX event_publication_by_completion_date_idx ON event_publication (completion_date);
