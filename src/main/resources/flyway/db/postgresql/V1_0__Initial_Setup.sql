CREATE SEQUENCE public.seq_account_audit_events
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;
ALTER TABLE public.seq_account_audit_events OWNER TO teamcode;

CREATE SEQUENCE public.seq_events
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;
ALTER TABLE public.seq_events OWNER TO teamcode;

CREATE SEQUENCE public.seq_groups
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;
ALTER TABLE public.seq_groups OWNER TO teamcode;

CREATE SEQUENCE public.seq_proj_attachments
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;
ALTER TABLE public.seq_proj_attachments OWNER TO teamcode;

CREATE SEQUENCE public.seq_project_members
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;
ALTER TABLE public.seq_project_members OWNER TO teamcode;

CREATE SEQUENCE public.seq_projects
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;
ALTER TABLE public.seq_projects OWNER TO teamcode;

CREATE SEQUENCE public.seq_tc_users
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 2
  CACHE 1;
ALTER TABLE public.seq_tc_users OWNER TO teamcode;

CREATE SEQUENCE public.seq_user_emails
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;
ALTER TABLE public.seq_user_emails OWNER TO teamcode;

CREATE SEQUENCE public.seq_visang_rms_rec
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;
ALTER TABLE public.seq_visang_rms_rec OWNER TO teamcode;

CREATE SEQUENCE public.seq_visang_rms_rec_details
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;
ALTER TABLE public.seq_visang_rms_rec_details OWNER TO teamcode;


-- create tables...
CREATE TABLE public.tc_users (
  id bigint NOT NULL,
  name character varying(255) NOT NULL,
  password character varying(255) NOT NULL,
  user_role character varying(255) NOT NULL,
  full_name character varying(255),
  email character varying(255) NOT NULL,
  notification_email character varying(255),
  state character varying(255) NOT NULL,
  avatar_path character varying(255),
  bio character varying(1000),
  organization character varying(255),
  sign_in_count integer NOT NULL,
  layout character varying(255) NOT NULL,
  theme character varying(255) NOT NULL,
  current_sign_in_at timestamp without time zone,
  current_sign_in_ip character varying(255),
  last_sign_in_at timestamp without time zone,
  last_sign_in_ip character varying(255),
  created_by bigint,
  updated_by bigint,
  created_at timestamp without time zone NOT NULL,
  updated_at timestamp without time zone,
  CONSTRAINT tc_users_pkey PRIMARY KEY (id),
  CONSTRAINT fk_user_creator FOREIGN KEY (created_by) REFERENCES public.tc_users (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_user_updater FOREIGN KEY (updated_by) REFERENCES public.tc_users (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT uk_users_email UNIQUE (email),
  CONSTRAINT uk_users_name UNIQUE (name)
);
ALTER TABLE public.tc_users OWNER TO teamcode;

CREATE TABLE public.user_emails (
  id bigint NOT NULL,
  email character varying(255),
  user_id bigint NOT NULL,
  created_at timestamp without time zone NOT NULL,
  updated_at timestamp without time zone,
  CONSTRAINT user_emails_pkey PRIMARY KEY (id),
  CONSTRAINT fk_user_emails FOREIGN KEY (user_id) REFERENCES public.tc_users (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT uk_user_emails UNIQUE (email)
);
ALTER TABLE public.user_emails OWNER TO teamcode;

CREATE TABLE public.groups (
  id bigint NOT NULL,
  description character varying(255),
  name character varying(255) NOT NULL,
  path character varying(255) NOT NULL,
  type character varying(255) NOT NULL,
  visibility_level character varying(255) NOT NULL,
  owner_id bigint NOT NULL,
  created_at timestamp without time zone NOT NULL,
  updated_at timestamp without time zone,
  CONSTRAINT groups_pkey PRIMARY KEY (id),
  CONSTRAINT fk_groups_owner FOREIGN KEY (owner_id) REFERENCES public.tc_users (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION
);
ALTER TABLE public.groups OWNER TO teamcode;

CREATE TABLE public.projects (
  id bigint NOT NULL,
  name character varying(255) NOT NULL,
  path character varying(255) NOT NULL,
  uuid character varying(255),
  description character varying(1000),
  archived boolean NOT NULL,
  archived_at timestamp without time zone,
  avatar_path character varying(255),
  star_count integer,
  visibility_level character varying(255) NOT NULL,
  creator_id bigint NOT NULL,
  group_id bigint NOT NULL,
  last_activity_at timestamp without time zone NOT NULL,
  created_at timestamp without time zone NOT NULL,
  updated_at timestamp without time zone,
  CONSTRAINT projects_pkey PRIMARY KEY (id),
  CONSTRAINT fk_project_creator FOREIGN KEY (creator_id) REFERENCES public.tc_users (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_projects_group FOREIGN KEY (group_id) REFERENCES public.groups (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT uk_proj_repo_uuid UNIQUE (uuid)
);
ALTER TABLE public.projects OWNER TO teamcode;
CREATE INDEX idx_projects_name ON public.projects USING btree (name COLLATE pg_catalog."default");

CREATE TABLE public.project_members (
  id bigint NOT NULL,
  project_role character varying(255) NOT NULL,
  project_id bigint NOT NULL,
  user_id bigint NOT NULL,
  created_at timestamp without time zone NOT NULL,
  updated_at timestamp without time zone,
  CONSTRAINT project_members_pkey PRIMARY KEY (id),
  CONSTRAINT fk_project_members_project FOREIGN KEY (project_id) REFERENCES public.projects (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_project_members_project_user FOREIGN KEY (user_id) REFERENCES public.tc_users (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION
);
ALTER TABLE public.project_members OWNER TO teamcode;

CREATE TABLE public.project_attachments (
  id bigint NOT NULL,
  content_type character varying(255) NOT NULL,
  downloads integer NOT NULL,
  extension character varying(255),
  file_name character varying(255) NOT NULL,
  original_file_name character varying(255) NOT NULL,
  path character varying(255) NOT NULL,
  byte_size bigint NOT NULL,
  author_id bigint NOT NULL,
  project_id bigint,
  created_at timestamp without time zone NOT NULL,
  CONSTRAINT project_attachments_pkey PRIMARY KEY (id),
  CONSTRAINT fk_proj_attach_author FOREIGN KEY (author_id) REFERENCES public.tc_users (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_proj_attach_proj FOREIGN KEY (project_id) REFERENCES public.projects (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION
);
ALTER TABLE public.project_attachments OWNER TO teamcode;

CREATE TABLE public.events (
  id bigint NOT NULL,
  author_id bigint NOT NULL,
  project_id bigint NOT NULL,
  title character varying(255),
  event_action character varying(255) NOT NULL,
  entity_id bigint,
  entity_type character varying(255),
  created_at timestamp without time zone NOT NULL,
  updated_at timestamp without time zone,
  CONSTRAINT events_pkey PRIMARY KEY (id),
  CONSTRAINT fk_events_author FOREIGN KEY (author_id) REFERENCES public.tc_users (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_events_project FOREIGN KEY (project_id) REFERENCES public.projects (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION
);
ALTER TABLE public.events OWNER TO teamcode;

CREATE TABLE public.account_audit_events (
  id bigint NOT NULL,
  user_id bigint NOT NULL,
  ip_addr character varying(255),
  browser character varying(255),
  os character varying(255),
  user_agent_id integer,
  user_agent_string character varying(500),
  created_at timestamp without time zone NOT NULL,
  CONSTRAINT account_audit_events_pkey PRIMARY KEY (id),
  CONSTRAINT fk_account_audit_events_user FOREIGN KEY (user_id) REFERENCES public.tc_users (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION
);
ALTER TABLE public.account_audit_events OWNER TO teamcode;

CREATE TABLE public.visang_rms_record (
  id bigint NOT NULL,
  author_id character varying(255) NOT NULL,
  project_id bigint NOT NULL,
  revision bigint NOT NULL,
  rms_id character varying(255) NOT NULL,
  last_modified_at timestamp without time zone NOT NULL,
  CONSTRAINT visang_rms_record_pkey PRIMARY KEY (id),
  CONSTRAINT fk_visang_rms_rec_proj FOREIGN KEY (project_id) REFERENCES public.projects (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT uk_visang_rms_rec UNIQUE (project_id, revision)
);
ALTER TABLE public.visang_rms_record OWNER TO teamcode;

CREATE TABLE public.visang_rms_record_details (
  id bigint NOT NULL,
  visang_rms_record_id bigint NOT NULL,
  file_path character varying(255) NOT NULL,
  CONSTRAINT visang_rms_record_details_pkey PRIMARY KEY (id),
  CONSTRAINT fk_visang_rms_rec_rec FOREIGN KEY (visang_rms_record_id) REFERENCES public.visang_rms_record (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION
);
ALTER TABLE public.visang_rms_record_details OWNER TO teamcode;

