Insert Into users(login, password)
Values
    ('admin@cloud.ru', '$2a$12$UEAOlqJnLEdWgHjpVI57auRrib2YYvx116zuG2w/TrmwTRt7J9i06'),
    ('user@cloud.ru', '$2a$12$MKbXxPZO7Xb5LhDifRk1EOY4Tt31NsyKjg4hID8JCMtQSGQz3HxqC');

Insert Into roles(code) Values ('ROLE_ADMIN'), ('ROLE_USER');

Insert Into users_and_roles(user_id, role_id)
Select
    (Select id From users Where login = 'admin@cloud.ru'),
    (Select  id From roles Where code = 'ROLE_ADMIN');

Insert Into users_and_roles(user_id, role_id)
Select
    (Select id From users Where login = 'user@cloud.ru'),
    (Select  id From roles Where code = 'ROLE_USER');