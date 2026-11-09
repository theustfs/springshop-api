create table users_actions (
    id uuid,
    account_id uuid not null references accounts(id) on delete cascade,
    action_type varchar(64) not null,
    constraint USER_ACTION_PK primary key (id),
    constraint USER_ACTION_UNIQUE_ACCOUNT_ID unique (account_id),
    constraint USER_ACTION_VALID_ACTION_TYPE check (
        action_type in (
            'ACCOUNT_REGISTRATION',
            'ACCOUNT_LOGIN',
            'ACCOUNT_EMAIL_UPDATE',
            'ACCOUNT_EMAIL_RESET',
            'ACCOUNT_PASSWORD_UPDATE',
            'ACCOUNT_PASSWORD_RESET',
            'ACCOUNT_DELETION'
        )
    )
);