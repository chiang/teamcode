-- 2018-06-08

ALTER TABLE ci_jobs ADD COLUMN executed_by bigint;

ALTER TABLE ci_jobs ADD CONSTRAINT fk_ci_jobs_executed FOREIGN KEY (executed_by) REFERENCES tc_users (id);
