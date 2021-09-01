-- V1_2 에서 추가한 컬럼에 대한 기본 값을 채우는 스크립트

UPDATE projects SET pipeline_visibility = 'DISABLED' WHERE pipeline_visibility IS NULL;
UPDATE projects SET attachments_visibility = 'ENABLED' WHERE attachments_visibility IS NULL;