-- 2017-10-14

ALTER TABLE project_services ADD COLUMN title character varying(255) NOT NULL;
ALTER TABLE project_services ADD COLUMN description character varying(255);