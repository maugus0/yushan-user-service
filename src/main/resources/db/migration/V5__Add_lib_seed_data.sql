INSERT INTO library (uuid, user_id, create_time, update_time) VALUES
    (gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440001', CURRENT_TIMESTAMP - INTERVAL '30 days', CURRENT_TIMESTAMP - INTERVAL '1 day'),
    (gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440002', CURRENT_TIMESTAMP - INTERVAL '30 days', CURRENT_TIMESTAMP - INTERVAL '1 day'),
    (gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440003', CURRENT_TIMESTAMP - INTERVAL '30 days', CURRENT_TIMESTAMP - INTERVAL '1 day'),
    (gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440004', CURRENT_TIMESTAMP - INTERVAL '30 days', CURRENT_TIMESTAMP - INTERVAL '1 day'),
    (gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440005', CURRENT_TIMESTAMP - INTERVAL '30 days', CURRENT_TIMESTAMP - INTERVAL '1 day'),
    (gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440006', CURRENT_TIMESTAMP - INTERVAL '30 days', CURRENT_TIMESTAMP - INTERVAL '1 day'),
    (gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440007', CURRENT_TIMESTAMP - INTERVAL '30 days', CURRENT_TIMESTAMP - INTERVAL '1 day'),
    (gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440008', CURRENT_TIMESTAMP - INTERVAL '30 days', CURRENT_TIMESTAMP - INTERVAL '1 day');

DELETE FROM users WHERE email = 'admin@yushan.com';

INSERT INTO users (
    uuid, email, username, hash_password, avatar_url,
    profile_detail, birthday, gender, is_admin, is_author,
    status, create_time, update_time, last_login, last_active
) VALUES
    ('5045b2cf-efc8-45de-ad6a-c7ef68e7db90', 'admin@yushan.com', 'admin', '$2a$10$ho2svjNlaADthonvqDjOcuLHp0tHbQ6r282MtEchmpuX52JouDIxC', 'user_male.png', 'Admin', '1985-03-15', 0, true, true, 0, CURRENT_TIMESTAMP - INTERVAL '60 days', CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP - INTERVAL '2 hours', CURRENT_TIMESTAMP - INTERVAL '30 minutes');

INSERT INTO library (uuid, user_id, create_time, update_time) VALUES
    (gen_random_uuid(), '5045b2cf-efc8-45de-ad6a-c7ef68e7db90', CURRENT_TIMESTAMP - INTERVAL '30 days', CURRENT_TIMESTAMP - INTERVAL '1 day');