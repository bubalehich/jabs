INSERT INTO user_accounts (id, email, password_hash, role)
SELECT CAST('7c1f3a52-8d4e-4b1a-9f6e-2a1d5c3b8e01' AS UUID), 'admin@events.local',
       '{bcrypt}$2a$10$29p8filgbv1Kjh9z.1W.ieoqYyDyezZxvu9qKdRxtFyodBNqmMW5C', 'ADMIN'
WHERE NOT EXISTS (SELECT 1 FROM user_accounts WHERE email = 'admin@events.local');

INSERT INTO user_accounts (id, email, password_hash, role)
SELECT CAST('3e9b6d14-5a2c-4f7e-8b1d-6c4a2f9e7d02' AS UUID), 'user@events.local',
       '{bcrypt}$2a$10$rofGPsxDYn3Ry7zy/aKpS.W6WCZEsINVjougpiXfH2Yvy4CbBRu8G', 'USER'
WHERE NOT EXISTS (SELECT 1 FROM user_accounts WHERE email = 'user@events.local');
