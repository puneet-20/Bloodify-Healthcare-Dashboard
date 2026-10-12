-- 1. Create the Database
CREATE DATABASE IF NOT EXISTS bloodify_db;
USE bloodify_db;

-- 2. Create the Donors Table (Matches typical Spring Boot Entity structure)
CREATE TABLE IF NOT EXISTS donors (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    age INT,
    gender VARCHAR(20),
    blood_group VARCHAR(5),
    city VARCHAR(100),
    contact_number VARCHAR(15),
    last_donation_date DATE,
    status VARCHAR(50) DEFAULT 'Eligible'
);

-- 3. Insert 25 Indian Donor Records
INSERT INTO donors (name, age, gender, blood_group, city, contact_number, last_donation_date, status) VALUES
('Aarav Sharma', 28, 'Male', 'O+', 'Nagpur', '9876543210', '2025-12-10', 'Eligible'),
('Ananya Iyer', 24, 'Female', 'A-', 'Mumbai', '9876543211', '2026-01-05', 'Eligible'),
('Vikram Deshmukh', 35, 'Male', 'B+', 'Pune', '9876543212', '2025-11-20', 'Eligible'),
('Sana Khan', 29, 'Female', 'O-', 'Hyderabad', '9876543213', '2026-02-14', 'Eligible'),
('Ishaan Verma', 31, 'Male', 'AB+', 'Delhi', '9876543214', '2025-10-05', 'Eligible'),
('Priya Kulkarni', 26, 'Female', 'B-', 'Nagpur', '9876543215', '2026-03-01', 'Eligible'),
('Rohan Joshi', 40, 'Male', 'A+', 'Bangalore', '9876543216', '2025-08-22', 'Eligible'),
('Aditi Rao', 22, 'Female', 'O+', 'Chennai', '9876543217', '2026-01-18', 'Eligible'),
('Karan Malhotra', 33, 'Male', 'B+', 'Lucknow', '9876543218', '2025-12-25', 'Eligible'),
('Sneha Patil', 27, 'Female', 'A+', 'Nagpur', '9876543219', '2026-02-10', 'Eligible'),
('Arjun Nair', 30, 'Male', 'O+', 'Kochi', '9876543220', '2025-11-30', 'Eligible'),
('Meera Reddy', 25, 'Female', 'AB-', 'Hyderabad', '9876543221', '2026-01-22', 'Eligible'),
('Rahul Gupta', 28, 'Male', 'B-', 'Indore', '9876543222', '2025-09-15', 'Eligible'),
('Tanvi Hegde', 23, 'Female', 'O-', 'Mangalore', '9876543223', '2026-03-12', 'Eligible'),
('Siddharth Singh', 37, 'Male', 'A-', 'Jaipur', '9876543224', '2025-12-01', 'Eligible'),
('Anjali Bose', 29, 'Female', 'B+', 'Kolkata', '9876543225', '2026-01-11', 'Eligible'),
('Sameer Wankhede', 32, 'Male', 'O+', 'Nagpur', '9876543226', '2025-10-20', 'Eligible'),
('Ritu Bharadwaj', 26, 'Female', 'A+', 'Gurgaon', '9876543227', '2026-02-28', 'Eligible'),
('Yash Vardhan', 34, 'Male', 'AB+', 'Ahmedabad', '9876543228', '2025-07-14', 'Eligible'),
('Kavita Rani', 31, 'Female', 'O+', 'Chandigarh', '9876543229', '2026-01-05', 'Eligible'),
('Pranav Sawant', 27, 'Male', 'B+', 'Nashik', '9876543230', '2026-03-05', 'Eligible'),
('Deepa Pillai', 24, 'Female', 'A-', 'Thiruvananthapuram', '9876543231', '2025-11-12', 'Eligible'),
('Manish Pandey', 38, 'Male', 'O-', 'Patna', '9876543232', '2025-12-18', 'Eligible'),
('Shreya Ghoshal', 28, 'Female', 'B-', 'Bhopal', '9876543233', '2026-02-20', 'Eligible'),
('Akash Mishra', 25, 'Male', 'AB-', 'Nagpur', '9876543234', '2026-01-30', 'Eligible');

-- 4. Verify the data
SELECT * FROM donors;