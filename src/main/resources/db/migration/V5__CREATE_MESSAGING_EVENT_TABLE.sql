create table messaging_events (
    id uuid,
    status varchar(32) not null,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone,
    constraint MESSAGING_EVENT_PK primary key (id),
    constraint VALID_MESSAGING_EVENT_STATUS check (status in (
        'CREATED', 'PROCESSING', 'PROCESSED', 'FAILED'
    ))
);
