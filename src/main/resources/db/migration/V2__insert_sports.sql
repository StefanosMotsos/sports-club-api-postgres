INSERT INTO sports (id, name) VALUES
(1, 'Football'),
(2, 'Basketball'),
(3, 'Volleyball'),
(4, 'Swimming'),
(5, 'Tennis'),
(6, 'Athletics'),
(7, 'Cycling'),
(8, 'Boxing'),
(9, 'Wrestling'),
(10, 'Judo'),
(11, 'Gymnastics'),
(12, 'Handball'),
(13, 'Water Polo');
ALTER TABLE sports ALTER COLUMN id RESTART WITH 14;