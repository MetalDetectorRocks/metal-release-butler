-- Creation Date: 2020-12-20
-- Description: Creates the initial tables

create table releases (
    id bigserial not null constraint releases_pkey primary key,
    created_date timestamp,
    last_modified_date timestamp,
    additional_artists varchar(255),
    album_title varchar(255) not null,
    artist varchar(255) not null,
    estimated_release_date varchar(255),
    genre varchar(255),
    artist_details_url varchar(500),
    release_details_url varchar(500),
    release_date date,
    source varchar(255),
    state varchar(255) not null,
    type varchar(255),
    cover_url varchar(255)
);

create table import_jobs (
    id bigserial not null constraint import_jobs_pkey primary key,
    created_date timestamp,
    last_modified_date timestamp,
    end_time timestamp,
    job_id uuid not null,
    source varchar(255) not null,
    start_time timestamp not null,
    state varchar(255) not null,
    total_count_imported integer,
    total_count_requested integer
);
