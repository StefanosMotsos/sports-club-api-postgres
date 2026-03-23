INSERT INTO capabilities (name, description)
VALUES ("VIEW_USER", "View user details");

INSERT INTO roles_capabilities(role_id, capability_id)
SELECT r.id, c.id
FROM roles r
JOIN capabilities c ON c.name = "VIEW_USER"
WHERE r.name = "ADMIN";