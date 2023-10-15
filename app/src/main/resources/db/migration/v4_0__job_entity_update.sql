-- Creation Date: 2023-10-12
-- Description: start_time can be null in job entity

alter table import_jobs alter column start_time drop not null;
