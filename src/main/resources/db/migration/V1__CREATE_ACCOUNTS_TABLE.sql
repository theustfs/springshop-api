create table accounts (
    id uuid,
    email varchar(254) not null,
    encoded_password VARCHAR(255) not null,
    role varchar(32) not null,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone,
    deletion_scheduled_at timestamp with time zone,
    verified boolean not null,

    constraint ACCOUNT_PK primary key (id),
    constraint ACCOUNT_UNIQUE_EMAIL unique (email),
    constraint ACCOUNT_VALID_ROLE check (role in ('USER', 'PRODUCT_MANAGER', 'ADMIN'))
);

create index IDX_ACCOUNT_NOT_VERIFIED
on accounts (created_at, id)
where verified IS FALSE;

create index IDX_ACCOUNT_DELETION_SCHEDULED
on accounts (deletion_scheduled_at, id)
where deletion_scheduled_at is not null;
