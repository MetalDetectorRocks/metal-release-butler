create table if not exists releases (
    id bigint not null auto_increment,
    created_date datetime,
    last_modified_date datetime,
    additional_artists NVARCHAR(255),
    album_title NVARCHAR(255) not null,
    artist NVARCHAR(255) not null,
    estimated_release_date varchar(255),
    genre varchar(255),
    metal_archives_album_url varchar(500),
    metal_archives_artist_url varchar(500),
    release_date DATE,
    source varchar(255),
    state varchar(255) not null,
    type varchar(255),
    primary key (id)
)
engine=InnoDB character set utf8 collate utf8_general_ci;
