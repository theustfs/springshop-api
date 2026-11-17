create table mail_deliveries (
    id uuid,
    provider_id uuid not null,
    status VARCHAR(32) not null,
    created_at timestamp with time zone not null,
    status_changed_at timestamp with time zone,
    provider_status_changed_at timestamp with time zone,
    constraint MAIL_DELIVERY_PK primary key (id),
    constraint MAIL_DELIVERY_UNIQUE_PROVIDER_ID unique (provider_id),
    constraint MAIL_DELIVERY_VALID_STATUS check (status in (
        'SUBMITTED', 'SENT', 'DELIVERED', 'DELIVERY_DELAYED', 'SUPPRESSED', 'BOUNCED', 'FAILED'
    ))
);
