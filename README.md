# 🎵 Music Streaming Application

This project is a scalable and modular **Music Streaming Platform** built using microservices architecture. It enables users to register, upload, and stream music seamlessly via a web client. The system ensures high availability, efficient media delivery, and secure user management.

---

## 📌 Architecture Overview

![Architecture Diagram](./Assets/MusicStreaming.excalidraw.png)

---

## 🧩 Components

### 1. **Client**
- Acts as the frontend interface for users.
- Sends requests and receives responses through the NGINX reverse proxy.

### 2. **NGINX + EC2**
- **NGINX** serves as the reverse proxy for handling HTTP requests.
- Deployed on an **AWS EC2** instance for secure entry into the system.

### 3. **API Gateway**
- Routes requests to appropriate backend services.
- Ensures scalability and centralized authentication/authorization.

### 4. **User Service**
- Handles user authentication and data management.
- Uses:
  - **PostgreSQL** for user data
  - **Redis** for session/JWT storage
- Communicates with:
  - API Gateway
  - Web Client

### 5. **Music Streaming Service**
- Manages audio file uploads, storage, and metadata.
- Features:
  - Stores audio files in **AWS S3**
  - Stores metadata in **PostgreSQL**
- Serves data to the frontend and manages streaming APIs.

### 6. **Web Client**
- Built using modern frontend frameworks.
- Interacts with both the **User Service** and **Music Service** through the API Gateway.

### 7. **Support Services**
- **Service Registry (Eureka)** – for service discovery
- **Config Server** – central configuration management for microservices
- **Docker** – all services are containerized for consistent deployment

---

## ⚙️ Technologies Used

- **Backend:** Spring Boot (Java)
- **Frontend:** Web (React/Next.js or similar)
- **API Gateway:** Spring Cloud Gateway
- **Database:** PostgreSQL, Redis
- **File Storage:** AWS S3
- **Containerization:** Docker
- **Deployment:** AWS EC2, NGINX
- **Service Discovery:** Eureka
- **Configuration Management:** Spring Cloud Config Server

---

## 🚀 How It Works

1. User sends request via web client.
2. NGINX on EC2 routes the request to the API Gateway.
3. API Gateway forwards the request to:
   - **User Service** for auth/user ops.
   - **Music Streaming Service** for streaming/audio ops.
4. Music files and metadata are served from S3 and PostgreSQL.

---

## 📁 Project Structure

/user-service
/music-service
/api-gateway
/server-registry
/config-server
/web-client
/docker-compose.yml

yaml
Copy
Edit

---

## 🏁 Getting Started

```bash
# Clone the repo
git clone https://github.com/sujaysharvesh/MusicService.git

# Navigate to project and start services
cd music-streaming-app
docker-compose up --build

