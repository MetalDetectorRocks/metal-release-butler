create table if not exists releases (
    id bigserial primary key,
    created_date timestamp,
    last_modified_date timestamp,
    additional_artists varchar(255),
    album_title varchar(255) not null,
    artist varchar(255) not null,
    estimated_release_date varchar(255),
    genre varchar(255),
    metal_archives_album_url varchar(500),
    metal_archives_artist_url varchar(500),
    release_date date,
    source varchar(255),
    state varchar(255) not null,
    type varchar(255)
);
