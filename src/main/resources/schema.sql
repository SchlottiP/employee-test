CREATE TABLE employee (
                          id UUID PRIMARY KEY,
                          email VARCHAR(255) UNIQUE NOT NULL,
                          full_name VARCHAR(255) NOT NULL,
                          birthday DATE NOT NULL
);

CREATE TABLE employee_hobbies (
                                  employee_id UUID REFERENCES employee(id),
                                  hobby VARCHAR(255)
);