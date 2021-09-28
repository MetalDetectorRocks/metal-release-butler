-- Creation Date: 2021-03-27
-- Description: add boolean column for reissues/re-releases

alter table releases add column reissue boolean default false;
