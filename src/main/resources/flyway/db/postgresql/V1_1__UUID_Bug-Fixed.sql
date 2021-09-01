-- #3: UUID 중복 오류 조치

ALTER TABLE projects DROP CONSTRAINT uk_proj_repo_uuid;
ALTER TABLE projects DROP COLUMN uuid RESTRICT;


