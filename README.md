# Fabflix

Fabflix is a dynamic web application designed to handle movie information efficiently, utilizing advanced web technologies like servlets, JDBC connection pooling, and Kubernetes for deployment. It provides functionalities like movie browsing, searching, and user authentication with robust back-end management of database operations.

## Demo Links

Watch how Fabflix works:

- [Part 1 - Overview](https://www.youtube.com/watch?v=IEztD_Pa1SI)
- [Part 2 - Throughput Demonstration](https://youtu.be/YycSJR589V8)
- [Part 3 - Features Walkthrough](https://youtu.be/2BsrhG8gSts)
- [Part 4 - Advanced Configurations](https://youtu.be/VST8KezGy4I)
- [Part 5 - Final Overview](https://youtu.be/MwGkIWP9q8w)

## Features

- **Movie Browsing and Searching**: Browse by genre or use full-text search to find movies.
- **User Authentication**: Secure login process with reCAPTCHA and session management.
- **High Availability Setup**: Using AWS for cloud deployment with a master-slave database setup ensuring read and write are efficiently handled.
- **Load Balancing and Auto-scaling**: Kubernetes deployment with auto-scaling based on load.
- **Security**: HTTPS setup and encryption for secure data transmission.
- **Connection Pooling**: Utilize JDBC pooling to manage database connections effectively.
- **Data Integrity**: XML parser integration to manage data consistency and integrity.

## Installation and Setup

### Prerequisites

- AWS Account
- Docker
- Kubernetes
- MySQL Database

### Configuration Files

- **Connection Pooling Configuration**: `WebContent/META-INF/context.xml`
- **Servlet and Database Routing Configuration**: `WebContent/WEB-INF/web.xml`

### Setting Up the Application

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/fabflix.git
   cd fabflix
   ```
2. Configure AWS settings according to your AWS setup documentation.
3. Set up Kubernetes pods and services as defined in your Kubernetes configuration files.
4. Deploy the application using Docker:
   ```bash
   docker build -t fabflix .
   docker run -p 8080:8080 fabflix
   ```

## Usage

After deployment, visit `http://localhost:8080/fabflix-war` to access the Fabflix dashboard. Use the provided credentials to log in and interact with the application.

## Connection Pooling

Fabflix utilizes connection pooling to manage database connections. Based on the operation (read or write), requests are routed to either the master (read and write) or slave (read-only) SQL server.

- **Get Connections**: `DatabaseUtil.getMasterConnection()` for writes or `DatabaseUtil.getSlaveConnection()` for reads.

## Optimization Strategy

- **SAX Parsing**: Used for XML parsing to improve memory efficiency.
- **Batch Processing**: SQL inserts are batch processed and wrapped as a transaction.
- **Data Validation**: Prior to insertion, data is validated to omit inconsistent and duplicate movies.

## Inconsistency Report

Refer to the inconsistency report at [Demo Part 3 at 8:06](https://youtu.be/2BsrhG8gSts?t=486) for detailed insights.

## Throughput Numbers

- **Initial Setup**: 917.204 requests/minute.
- **Optimized Setup**: 1,002.194 requests/minute after scaling up the Kubernetes nodes.

