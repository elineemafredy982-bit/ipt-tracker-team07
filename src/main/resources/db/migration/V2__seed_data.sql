-- All seed accounts use the password: Password123!   (BCrypt hashes, cost 10)
INSERT INTO users (full_name, email, password_hash, role, phone) VALUES
 ('System Admin',        'admin@ipt.test',    '$2b$10$8HJ3ZXJcabh6h.Ce6Zw51ubfoxlHTXmseyxeJE/hagR/PTplPWvXG', 'ADMIN',                 '+255700000001'),
 ('Ibrahim Industrial',  'industrial@ipt.test','$2b$10$8HJ3ZXJcabh6h.Ce6Zw51ubfoxlHTXmseyxeJE/hagR/PTplPWvXG', 'INDUSTRIAL_SUPERVISOR', '+255700000002'),
 ('Dr. Amina Academic',  'academic@ipt.test', '$2b$10$8HJ3ZXJcabh6h.Ce6Zw51ubfoxlHTXmseyxeJE/hagR/PTplPWvXG', 'ACADEMIC_SUPERVISOR',   '+255700000003'),
 ('Asha Mussa',          'student1@ipt.test', '$2b$10$8HJ3ZXJcabh6h.Ce6Zw51ubfoxlHTXmseyxeJE/hagR/PTplPWvXG', 'STUDENT',               '+255700000011'),
 ('Juma Bakari',         'student2@ipt.test', '$2b$10$8HJ3ZXJcabh6h.Ce6Zw51ubfoxlHTXmseyxeJE/hagR/PTplPWvXG', 'STUDENT',               '+255700000012'),
 ('Neema Joseph',        'student3@ipt.test', '$2b$10$8HJ3ZXJcabh6h.Ce6Zw51ubfoxlHTXmseyxeJE/hagR/PTplPWvXG', 'STUDENT',               '+255700000013');

INSERT INTO organisations (name, sector, region, address, contact_email) VALUES
 ('TTCL',            'ICT',      'Dar es Salaam', 'Samora Avenue, Dar es Salaam', 'hr@ttcl.test'),
 ('Arusha Tech Hub', 'ICT',      'Arusha',        'Sakina, Arusha',               'info@arushahub.test');

-- Demo placement for student1 so the workflow can be tried immediately.
INSERT INTO placements (student_id, organisation_id, industrial_supervisor_id, academic_supervisor_id,
                        start_date, end_date, department, status)
SELECT s.id, o.id, i.id, a.id, CURRENT_DATE - 30, CURRENT_DATE + 60, 'Software', 'ACTIVE'
FROM users s, organisations o, users i, users a
WHERE s.email = 'student1@ipt.test' AND o.name = 'TTCL'
  AND i.email = 'industrial@ipt.test' AND a.email = 'academic@ipt.test';
