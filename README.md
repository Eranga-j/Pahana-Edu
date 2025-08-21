# Pahana Edu — Online Billing System

Two NetBeans/Maven projects:
- **pahana-edu-service** — Jakarta EE 10 JAX-RS REST service (GlassFish 7).
- **pahana-edu-web** — JSP/Servlet web client that calls the REST service.

## Requirements
- Apache NetBeans IDE 21
- JDK 21
- GlassFish 7.x (Jakarta EE 10)
- XAMPP (MySQL 8.x on localhost:3306)

## Database
Create a MySQL database and user, then run `sql/pahanaedu.sql`.

JNDI: **jdbc/pahanaedu** (Pool: **PahanaEduPool**)

Copy `mysql-connector-j-8.x.x.jar` into your GlassFish domain `lib/` directory and restart.

## Service URL
`http://localhost:8080/pahana-edu-service/api`

## Default users
- admin / admin123 (ADMIN)
- cashier / cashier123 (CASHIER)

## Build & Run
1. Open both Maven projects in NetBeans.
2. Clean & Build.
3. Deploy service, then web.
4. Visit `http://localhost:8080/pahana-edu-web/`
