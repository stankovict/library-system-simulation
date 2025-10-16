# Library System Simulation  
*A Java Swing application demonstrating object-oriented design, GUI development, and file-based data management.*

---

## Overview

**Library System Simulation** is an object-oriented Java project developed as part of a **university course on Object-Oriented Programming**.  
It simulates the operations of a library through an interactive **Swing GUI**, supporting multiple user roles, data persistence via CSV files, and full CRUD (Create, Read, Update, Delete) functionality.  

---

## Features

- **Multi-Role Functionality**
  - Separate interfaces and permissions for different user roles  
  - Examples: *Administrator*, *Librarian*, *Member/User*  

- **Book Management**
  - Add, edit, delete, and view books  
  - Manage genres, authors, availability, and prices  
  - CSV-based persistence for all book data  

- **Reservations & Borrowing**
  - Users can reserve or borrow available books  
  - Reservation queue and return tracking  

- **Finance Management**
  - Track and update financial transactions  
  - Handle late fees, and other costs  

- **Search & Filtering**
  - Search books by title, author, or category  
  - Filter available or borrowed books  

- **CRUD Operations**
  - Implemented across all major entities (books, users, transactions)  
  - Changes persist through CSV storage  

- **File-Based Data Persistence**
  - Reads and writes structured data to `.csv` files  
  - Enables easy data recovery between sessions  

---

## Technologies Used

- **Java 17**
- **Swing (javax.swing)** for GUI components  
- **File I/O** for reading and writing `.csv` files  
- **Collections Framework (ArrayList, HashMap)** for in-memory data handling  
- **MVC-inspired structure** for separation of logic and presentation  

---

