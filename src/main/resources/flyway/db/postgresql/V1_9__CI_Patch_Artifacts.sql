-- 2018-05-19

CREATE TABLE public.ci_job_artifacts (
  id bigint NOT NULL,
  project_id bigint,
  job_id bigint,
  file_type character varying(255) NOT NULL,
  file_size bigint NOT NULL,
  file_path character varying(2048) NOT NULL,
  is_deleted boolean,
  deleted_from_scheduler boolean,
  deleted_by character varying(255),
  deleted_at timestamp without time zone,
  expire_at timestamp without time zone,
  created_at timestamp without time zone NOT NULL,
  updated_at timestamp without time zone,
  CONSTRAINT ci_job_artifacts_pkey PRIMARY KEY (id),
  CONSTRAINT fk_ci_artifacts_proj FOREIGN KEY (project_id) REFERENCES public.projects (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_ci_artifacts_job FOREIGN KEY (job_id) REFERENCES public.ci_jobs (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE SEQUENCE public.seq_ci_artifacts
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;
ALTER TABLE public.seq_ci_artifacts OWNER TO teamcode;
