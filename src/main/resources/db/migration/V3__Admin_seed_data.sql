INSERT INTO users (
    uuid, email, username, hash_password, avatar_url,
    profile_detail, birthday, gender, is_admin, is_author,
    status, create_time, update_time, last_login, last_active
) VALUES
(gen_random_uuid(), 'admin@yushan.com', 'admin', '$2a$10$ho2svjNlaADthonvqDjOcuLHp0tHbQ6r282MtEchmpuX52JouDIxC', 'user_male.png', 'Admin', '1985-03-15', 0, true, true, 0, CURRENT_TIMESTAMP - INTERVAL '60 days', CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP - INTERVAL '2 hours', CURRENT_TIMESTAMP - INTERVAL '30 minutes'),
