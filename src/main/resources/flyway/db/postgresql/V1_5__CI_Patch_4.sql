-- 2017-08-07

ALTER TABLE ci_jobs ADD COLUMN artifacts_file text;
ALTER TABLE ci_jobs ADD COLUMN artifacts_expire_at timestamp without time zone;

