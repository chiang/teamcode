-- CI 관련 대량 업데이트

CREATE SEQUENCE public.seq_app_settings
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;
ALTER TABLE public.seq_app_settings OWNER TO teamcode;

CREATE SEQUENCE public.seq_proj_services
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;
ALTER TABLE public.seq_proj_services OWNER TO teamcode;

CREATE SEQUENCE public.seq_project_links
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;
ALTER TABLE public.seq_project_links OWNER TO teamcode;

CREATE SEQUENCE public.seq_ci_runners
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;
ALTER TABLE public.seq_ci_runners OWNER TO teamcode;

CREATE SEQUENCE public.seq_ci_pipelines
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;
ALTER TABLE public.seq_ci_pipelines OWNER TO teamcode;

CREATE SEQUENCE public.seq_ci_jobs
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;
ALTER TABLE public.seq_ci_jobs OWNER TO teamcode;

-------------------------------------------------------------------------------
-- Table creation -------------------------------------------------------------
-------------------------------------------------------------------------------

CREATE TABLE public.application_settings (
  id bigint NOT NULL,
  runners_registration_token character varying(255),
  created_at timestamp without time zone NOT NULL,
  updated_at timestamp without time zone,
  CONSTRAINT application_settings_pkey PRIMARY KEY (id)
);
ALTER TABLE public.application_settings OWNER TO teamcode;

CREATE TABLE public.project_services (
  id bigint NOT NULL,
  project_id bigint,
  service_key character varying(255) NOT NULL,
  is_active boolean NOT NULL,
  service_category character varying(255) NOT NULL,
  is_commit_events boolean,
  properties text,
  created_at timestamp without time zone NOT NULL,
  updated_at timestamp without time zone,
  CONSTRAINT project_services_pkey PRIMARY KEY (id),
  CONSTRAINT fk_proj_services_proj FOREIGN KEY (project_id) REFERENCES public.projects (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT uk_proj_services UNIQUE (project_id, service_key)
);
ALTER TABLE public.project_services OWNER TO teamcode;

CREATE TABLE public.project_links (
  id bigint NOT NULL,
  project_id bigint,
  link_title character varying(255) NOT NULL,
  link character varying(255) NOT NULL,
  created_at timestamp without time zone NOT NULL,
  updated_at timestamp without time zone,
  CONSTRAINT project_links_pkey PRIMARY KEY (id),
  CONSTRAINT fk_project_links_project FOREIGN KEY (project_id) REFERENCES public.projects (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION
);
ALTER TABLE public.project_links OWNER TO teamcode;

CREATE TABLE public.ci_runners (
  id bigint NOT NULL,
  name character varying(255),
  token character varying(20) NOT NULL,
  description character varying(255),
  version character varying(255),
  revision character varying(255),
  platform character varying(255),
  architecture character varying(255),
  is_active boolean NOT NULL,
  contacted_at timestamp without time zone NOT NULL,
  created_at timestamp without time zone NOT NULL,
  updated_at timestamp without time zone,
  CONSTRAINT ci_runners_pkey PRIMARY KEY (id),
  CONSTRAINT uk_runners_token UNIQUE (token)
);
ALTER TABLE public.ci_runners OWNER TO teamcode;

CREATE TABLE public.ci_pipelines (
  id bigint NOT NULL,
  project_id bigint,
  commit_revision bigint NOT NULL,
  yaml_errors character varying(2000),
  pipeline_status character varying(255) NOT NULL,
  duration bigint,
  started_at timestamp without time zone,
  finished_at timestamp without time zone,
  created_by bigint,
  created_at timestamp without time zone NOT NULL,
  updated_at timestamp without time zone,
  CONSTRAINT ci_pipelines_pkey PRIMARY KEY (id),
  CONSTRAINT fk_ci_pipelines_proj FOREIGN KEY (project_id) REFERENCES public.projects (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_ci_pipelines_creator FOREIGN KEY (created_by) REFERENCES public.tc_users (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION
);
ALTER TABLE public.ci_pipelines OWNER TO teamcode;

CREATE TABLE public.ci_jobs (
  id bigint NOT NULL,
  name character varying(255) NOT NULL,
  token character varying(20),
  pipeline_id bigint,
  runner_id bigint,
  stage_name character varying(255) NOT NULL,
  stage_idx smallint NOT NULL,
  job_status character varying(255) NOT NULL,
  commands text,
  options text,
  job_when character varying(255) NOT NULL,
  queued_at timestamp without time zone,
  started_at timestamp without time zone,
  finished_at timestamp without time zone,
  created_at timestamp without time zone NOT NULL,
  updated_at timestamp without time zone,
  CONSTRAINT ci_jobs_pkey PRIMARY KEY (id),
  CONSTRAINT fk_job_pipeline FOREIGN KEY (pipeline_id) REFERENCES public.ci_pipelines (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_job_runner FOREIGN KEY (runner_id) REFERENCES public.ci_runners (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION
);
ALTER TABLE public.ci_jobs OWNER TO teamcode;

-------------------------------------------------------------------------------
-- Table Alter ----------------------------------------------------------------
-------------------------------------------------------------------------------

ALTER TABLE projects ADD COLUMN programming_language character varying(255);
ALTER TABLE projects ADD COLUMN attachments_visibility character varying(255);
ALTER TABLE projects ADD COLUMN pipeline_visibility character varying(255);
ALTER TABLE projects ADD COLUMN build_timeout integer;

