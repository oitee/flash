CREATE DATABASE chat;
\connect chat;

CREATE TABLE users (
    id UUID PRIMARY KEY,
    name TEXT UNIQUE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE chatroom (
    id UUID PRIMARY KEY,
    name TEXT UNIQUE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE chatroom_members (
    member UUID REFERENCES users (id),
    chatroom UUID REFERENCES chatroom (id),
    PRIMARY KEY (member, chatroom)
);



CREATE TABLE messages (
    id UUID PRIMARY KEY, 
    contents TEXT NOT NULL,
    user_id UUID REFERENCES users (id),
    chat_room UUID REFERENCES chatroom (id),
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE
);


-- inserting new users
INSERT INTO users (id, name, created_at, updated_at) VALUES ('33a6fa88-15c8-4a52-aaed-bf1ac1c40c12', 'john', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP), ('702335e9-e0b0-4f9e-9f07-6ace7beaea6d', 'Alice', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP), ('178dcdf9-e6fa-4a05-899d-3f067702750e', 'Bob', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- creating a new chatroom
INSERT INTO chatroom (id, name, created_at, updated_at) VALUES ('2db98b67-88db-4902-971a-7128fa12e34b', 'Office Only', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- adding members to a chatroom
INSERT INTO chatroom_members (member, chatroom) VALUES ('33a6fa88-15c8-4a52-aaed-bf1ac1c40c12', '2db98b67-88db-4902-971a-7128fa12e34b');

-- inserting new messages
INSERT INTO messages (id, contents, user_id, chat_room, created_at, updated_at) VALUES 
('3e5ea1e3-bbef-4725-95db-0516e5b5b317', 'Hi World', '33a6fa88-15c8-4a52-aaed-bf1ac1c40c12', '2db98b67-88db-4902-971a-7128fa12e34b', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP), 
('0d28f206-831b-4acb-b29a-8e538dc8a15c', 'Hello!', '702335e9-e0b0-4f9e-9f07-6ace7beaea6d', '2db98b67-88db-4902-971a-7128fa12e34b', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP), 
('bc5c63f8-b3dd-474d-b988-66dfbdfa02cf', 'Hello, Alice!', '33a6fa88-15c8-4a52-aaed-bf1ac1c40c12', '2db98b67-88db-4902-971a-7128fa12e34b', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP); 

-- Finding all messages sent by alice

SELECT * FROM messages WHERE user_id=2;
SELECT contents FROM messages WHERE user_id=2;


-- Finding all messages sent by John
SELECT contents from messages where user_id=1;


-- Last 50 messages from a chatroom
SELECT m.id as message_id, m.contents, c.name as chatroom, u.name as username, m.created_at  from 
(messages m JOIN users u ON m.user_id=u.id) 
JOIN chatroom c ON c.id=m.chat_room
WHERE m.chat_room='2db98b67-88db-4902-971a-7128fa12e34b' AND m.created_at > '2022-02-05 12:19:49.060958+00'
ORDER BY m.created_at ASC LIMIT 50;

