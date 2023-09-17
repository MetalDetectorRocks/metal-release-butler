-- Creation Date: 2023-09-17
-- Description: increase size of url fields

alter table releases alter column artist_details_url type varchar(1000);
alter table releases alter column release_details_url type varchar(1000);
alter table releases alter column cover_url type varchar(1000);
