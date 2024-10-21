-- MySQL Dump to create five tables and insert some data


-- Create Table1
CREATE TABLE `Table1` (id INT AUTO_INCREMENT PRIMARY KEY,name VARCHAR(50),value INT);

-- Create Table2
CREATE TABLE `Table2` (
    id INT AUTO_INCREMENT PRIMARY KEY,
    description TEXT,
    is_active TINYINT(1)
    );

-- Create Table3
CREATE TABLE IF NOT EXISTS Table3 (
                                      id INT AUTO_INCREMENT PRIMARY KEY,
                                      user_id INT,
                                      amount DECIMAL(10,2)
    );

-- Create Table4
CREATE TABLE IF NOT EXISTS Table4 (
                                      id INT AUTO_INCREMENT PRIMARY KEY,
                                      timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                      status VARCHAR(20)
    );

-- Create Table5
CREATE TABLE IF NOT EXISTS Table5 (
                                      id INT AUTO_INCREMENT PRIMARY KEY,
                                      title VARCHAR(100),
    content TEXT
    );


-- Create Table for Cacheing
CREATE TABLE IF NOT EXISTS last_modified (
                                             tableName VARCHAR(255) PRIMARY KEY,
    localDateTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );




-- Insert data into Table1
INSERT INTO Table1 (name, value) VALUES
                                     ('Item1', 100),
                                     ('Item2', 200),
                                     ('Item3', 300);

-- Insert data into Table2
INSERT INTO Table2 (description, is_active) VALUES
                                                ('Description1', 1),
                                                ('Description2', 0),
                                                ('Description3', 1);

-- Insert data into Table3
INSERT INTO Table3 (user_id, amount) VALUES
                                         (1, 99.99),
                                         (2, 150.75),
                                         (3, 250.00);

-- Insert data into Table4
INSERT INTO Table4 (status) VALUES
                                ('Pending'),
                                ('Completed'),
                                ('Failed');

-- Insert data into Table5
INSERT INTO Table5 (title, content) VALUES
                                        ('Title1', 'Content1'),
                                        ('Title2', 'Content2'),
                                        ('Title3', 'Content3');

INSERT INTO last_modified (tableName) VALUES
                                          ('Table1'),
                                          ('Table2'),
                                          ('Table3'),
                                          ('Table4'),
                                          ('Table5');