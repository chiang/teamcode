-- 2017-08-07

UPDATE projects SET build_timeout = 3600 WHERE build_timeout IS NULL;

CREATE SEQUENCE public.seq_ci_variables
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;
ALTER TABLE public.seq_ci_variables OWNER TO teamcode;

CREATE TABLE public.ci_variables (
  id bigint NOT NULL,
  variable_name character varying(255) NOT NULL,
  variable_value character varying(255) NOT NULL,
  is_secured boolean NOT NULL,
  project_id bigint,
  entity_state character varying(255) NOT NULL,
  created_by bigint,
  updated_by bigint,
  created_at timestamp without time zone NOT NULL,
  updated_at timestamp without time zone,
  CONSTRAINT ci_variables_pkey PRIMARY KEY (id),
  CONSTRAINT fk_ci_variables_proj FOREIGN KEY (project_id) REFERENCES public.projects (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_ci_variables_creator FOREIGN KEY (created_by) REFERENCES public.tc_users (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_ci_variables_updater FOREIGN KEY (updated_by) REFERENCES public.tc_users (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT uk_ci_variables UNIQUE (project_id, variable_name)
);
ALTER TABLE public.ci_variables OWNER TO teamcode;
CREATE INDEX idx_ci_variables ON public.ci_variables USING btree (variable_name COLLATE pg_catalog."default");
