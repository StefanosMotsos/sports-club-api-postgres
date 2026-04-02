-- Insert roles
INSERT INTO roles (name)
VALUES ('ADMIN'), ('EMPLOYEE'), ('MEMBER');

-- Insert capabilities
INSERT INTO capabilities (name, description)
VALUES
    ('INSERT_MEMBER', 'Create a new member'),
    ('VIEW_MEMBERS', 'View member list and details'),
    ('VIEW_MEMBER', 'View member'),
    ('EDIT_MEMBER', 'Modify existing member'),
    ('DELETE_MEMBER', 'Remove a member'),
    ('VIEW_ONLY_MEMBER', 'View only own member details');

-- Assign ALL capabilities to ADMIN
INSERT INTO roles_capabilities (role_id, capability_id)
SELECT r.id, c.id
FROM roles r
CROSS JOIN capabilities c
WHERE r.name = 'ADMIN';

-- Assign specific capabilities to EMPLOYEE
INSERT INTO roles_capabilities (role_id, capability_id)
SELECT r.id, c.id
FROM roles r
CROSS JOIN capabilities c
WHERE r.name = 'EMPLOYEE'
  AND c.name IN ('VIEW_MEMBERS', 'VIEW_MEMBER');

-- Assign specific capabilities to MEMBER
INSERT INTO roles_capabilities (role_id, capability_id)
SELECT r.id, c.id
FROM roles r
CROSS JOIN capabilities c
WHERE r.name = 'MEMBER'
  AND c.name = 'VIEW_ONLY_MEMBER';