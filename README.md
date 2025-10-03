# LeagueHQ - Competition Management Platform

A full-stack web application for managing sports competitions, teams, and matches. Built with Spring Boot, React, and PostgreSQL.

## 🏗️ Architecture

- **Backend**: Spring Boot 3.2 + PostgreSQL 15
- **Frontend**: React 18 + TypeScript + Vite + Tailwind CSS v4
- **Database**: PostgreSQL 15 (Docker)
- **Authentication**: JWT

## 📋 Prerequisites

- **Java 17+** (backend)
- **Node.js 18+** and npm (frontend)
- **Docker** and Docker Compose (database)
- **Git**

## 🚀 Quick Start

### 1. Clone and Setup

```bash
git clone <repository-url>
cd fullstack-competition-hq

# Copy environment files
cp backend/.env.example backend/.env
cp frontend/.env.example frontend/.env
```

### 2. Start PostgreSQL

```bash
docker-compose up -d
```

This starts PostgreSQL on port 5432.

### 3. Start the Backend

stripe listen --forward-to localhost:8080/api/stripe/webhooks
stripe trigger account.updated

```bash
cd backend
./mvnw spring-boot:run
```

Backend runs on **http://localhost:8080**

### 4. Start the Frontend (in a new terminal)

```bash
cd frontend
npm install
npm run dev
```

Frontend runs on **http://localhost:5173**

### 5. Access the Application

Open your browser and navigate to **http://localhost:5173**

Sign up for an account and start creating competitions!

## 🎛️ Starting and Stopping Services

### Start All Services

**Option 1: Start Everything Separately**

```bash
# Terminal 1: Start Database
docker-compose up -d

# Terminal 2: Start Backend
cd backend
./mvnw spring-boot:run

# Terminal 3: Start Frontend
cd frontend
npm run dev
```

**Option 2: Start Database Only, Run Others in Background**

```bash
# Start database
docker-compose up -d

# Start backend in background (nohup on Linux/Mac, or start on Windows)
cd backend
./mvnw spring-boot:run &

# Start frontend
cd frontend
npm run dev
```

### Stop All Services

**Stop Frontend:**
- Press `Ctrl+C` in the terminal running `npm run dev`

**Stop Backend:**
- Press `Ctrl+C` in the terminal running `./mvnw spring-boot:run`
- Or find and kill the Java process:
  ```bash
  # Windows
  netstat -ano | findstr :8080
  taskkill /PID <PID> /F

  # Linux/Mac
  lsof -i :8080
  kill -9 <PID>
  ```

**Stop Database:**
```bash
docker-compose down
```

**Stop Database and Delete Data:**
```bash
docker-compose down -v
```

### Restart Services

**Restart Database:**
```bash
docker-compose restart
```

**Restart Backend:**
- Press `Ctrl+C` to stop
- Run `./mvnw spring-boot:run` again

**Restart Frontend:**
- Press `Ctrl+C` to stop
- Run `npm run dev` again

### Check Service Status

**Database:**
```bash
docker-compose ps
```

**Backend:**
```bash
curl http://localhost:8080/actuator/health
```

**Frontend:**
- Open http://localhost:5173 in your browser

## 📦 Development Setup (Detailed)

### Database Setup

The PostgreSQL database runs in Docker. Configuration is in `docker-compose.yml`:

```yaml
services:
  postgres:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: leaguehq
      POSTGRES_USER: leaguehq
      POSTGRES_PASSWORD: leaguehq123
    ports:
      - "5432:5432"
```

**Database migrations** are managed by Flyway and run automatically on backend startup.

### Backend Setup

1. **Navigate to backend directory:**
   ```bash
   cd backend
   ```

2. **Configure application properties** (already set in `application.yml`):
   - Database: `jdbc:postgresql://localhost:5432/leaguehq`
   - JWT Secret: `your-secret-key-here-change-in-production-minimum-256-bits`
   - Stripe API Key: Set in environment variables

3. **Run the application:**
   ```bash
   ./mvnw spring-boot:run
   ```

4. **Verify backend is running:**
   ```bash
   curl http://localhost:8080/actuator/health
   ```

### Frontend Setup

1. **Navigate to frontend directory:**
   ```bash
   cd frontend
   ```

2. **Install dependencies:**
   ```bash
   npm install
   ```

3. **Start development server:**
   ```bash
   npm run dev
   ```

4. **Build for production:**
   ```bash
   npm run build
   ```

## 🐳 Docker Deployment (Production)

### Using Docker Compose (Recommended)

Run the entire stack with one command:

```bash
docker-compose -f docker-compose.prod.yml up -d
```

This starts:
- PostgreSQL database
- Spring Boot backend
- React frontend (nginx)

Access the application at **http://localhost**

### Manual Docker Setup

#### Build Backend Image

```bash
cd backend
docker build -t leaguehq-backend .
docker run -p 8080:8080 --env-file .env leaguehq-backend
```

#### Build Frontend Image

```bash
cd frontend
docker build -t leaguehq-frontend .
docker run -p 80:80 leaguehq-frontend
```

## 🔧 Configuration

### Environment Variables

#### Backend (.env or docker-compose.yml)

```bash
# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/leaguehq
SPRING_DATASOURCE_USERNAME=leaguehq
SPRING_DATASOURCE_PASSWORD=leaguehq123

# JWT
JWT_SECRET=your-secret-key-here-change-in-production-minimum-256-bits
JWT_EXPIRATION=86400000

# Stripe
STRIPE_API_KEY=sk_test_...
STRIPE_WEBHOOK_SECRET=whsec_...
```

#### Frontend (vite.config.ts)

The frontend uses Vite's proxy in development to forward API requests to the backend:

```typescript
server: {
  port: 5173,
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true,
    },
  },
}
```

In production, configure nginx to proxy `/api` to the backend.

## 🛠️ Common Issues & Solutions

### Issue 1: Port Already in Use

**Error:** `Port 8080 is already in use` or `Port 5173 is already in use`

**Solution:**
```bash
# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Linux/Mac
lsof -i :8080
kill -9 <PID>
```

### Issue 2: Tailwind CSS Errors

**Error:** `Cannot apply unknown utility class 'font-sans'`

**Cause:** Tailwind CSS v4 changed syntax - no longer uses `@apply` in the same way

**Solution:** Already fixed in `src/index.css` - uses `@import "tailwindcss"` and plain CSS

### Issue 3: CORS / 403 Errors

**Error:** `Failed to load resource: 403 Forbidden` or `CORS error`

**Cause:** Frontend is not using the correct port with proxy configuration

**Solution:**
1. Make sure frontend is running on port 5173 (default Vite port)
2. Kill all other Vite processes on ports 5174, 5175, etc.
3. Access app at `http://localhost:5173` (NOT 5177 or other ports)
4. Vite proxy will forward `/api` requests to backend on port 8080

### Issue 4: Database Connection Failed

**Error:** `Connection refused` or `Unknown database 'leaguehq'`

**Solution:**
1. Make sure Docker is running: `docker ps`
2. Start database: `docker-compose up -d`
3. Check logs: `docker-compose logs postgres`
4. Verify connection: `psql -h localhost -U leaguehq -d leaguehq`

### Issue 5: Flyway Migration Failed

**Error:** SQL syntax error in migration files

**Solution:**
1. Check migration files in `backend/src/main/resources/db/migration/`
2. Ensure SQL syntax is correct for PostgreSQL
3. Reset database if needed:
   ```bash
   docker-compose down -v
   docker-compose up -d
   ```

### Issue 6: Module Not Found Errors (Frontend)

**Error:** `Cannot find module` or `Module not found`

**Solution:**
```bash
cd frontend
rm -rf node_modules package-lock.json
npm install
npm run dev
```

## 📁 Project Structure

```
fullstack-competition-hq/
├── backend/                    # Spring Boot backend
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/leaguehq/
│   │   │   │   ├── controller/    # REST controllers
│   │   │   │   ├── service/       # Business logic
│   │   │   │   ├── repository/    # Data access
│   │   │   │   ├── model/         # JPA entities
│   │   │   │   ├── dto/           # Data transfer objects
│   │   │   │   ├── security/      # JWT auth
│   │   │   │   └── exception/     # Exception handlers
│   │   │   └── resources/
│   │   │       ├── db/migration/  # Flyway migrations
│   │   │       └── application.yml
│   │   └── test/
│   ├── pom.xml
│   └── Dockerfile
│
├── frontend/                   # React frontend
│   ├── src/
│   │   ├── components/        # Reusable components
│   │   │   ├── common/        # Common components (Spinner, Badge, etc.)
│   │   │   └── layout/        # Layout components (Header, etc.)
│   │   ├── pages/             # Page components
│   │   ├── hooks/             # Custom React hooks
│   │   ├── services/          # API services
│   │   ├── types/             # TypeScript types
│   │   ├── index.css          # Global styles + Tailwind
│   │   ├── App.tsx            # Root component
│   │   └── main.tsx           # Entry point
│   ├── public/
│   ├── package.json
│   ├── vite.config.ts
│   ├── tailwind.config.js
│   ├── postcss.config.js
│   └── Dockerfile
│
├── docs/                      # Documentation
│   ├── build_plan.md          # Week-by-week development plan
│   ├── db_schema.md           # Database schema
│   ├── CODING_GUIDELINES_BACKEND.md
│   ├── CODING_GUIDELINES_FRONTEND.md
│   ├── UI_DESIGN_SPECS.md
│   ├── WIREFRAMES.md
│   └── DESIGN_SYSTEM.md
│
├── docker-compose.yml         # Development database
├── docker-compose.prod.yml    # Production full stack
└── README.md                  # This file
```

## 🧪 Testing

### Backend Tests

```bash
cd backend
./mvnw test
```

### Frontend Tests

```bash
cd frontend
npm test
```

## 📚 API Documentation

Once the backend is running, access Swagger UI at:

**http://localhost:8080/swagger-ui.html**

Or view OpenAPI spec at:

**http://localhost:8080/v3/api-docs**

## 🎨 Design System

The frontend uses a custom design system built with Tailwind CSS v4:

- **Primary Color:** Indigo (indigo-600)
- **Font:** Inter
- **Components:** Header, StatusBadge, Spinner, etc.

See `docs/DESIGN_SYSTEM.md` for full specifications.

## 🔐 Authentication Flow

1. User signs up → Backend creates user with hashed password
2. User logs in → Backend returns JWT token
3. Frontend stores token in localStorage
4. All subsequent requests include token in `Authorization` header
5. Backend validates token and returns user data

## 📈 Development Workflow

1. **Create a new branch:**
   ```bash
   git checkout -b feature/your-feature-name
   ```

2. **Make changes** following coding guidelines in `docs/`

3. **Test your changes:**
   ```bash
   # Backend
   cd backend && ./mvnw test

   # Frontend
   cd frontend && npm test
   ```

4. **Commit and push:**
   ```bash
   git add .
   git commit -m "feat: add your feature"
   git push origin feature/your-feature-name
   ```

5. **Create a Pull Request**

## 🚢 Production Deployment

### Option 1: Docker Compose (Simplest)

```bash
docker-compose -f docker-compose.prod.yml up -d
```

### Option 2: Cloud Platforms

- **Backend:** Deploy to Heroku, AWS Elastic Beanstalk, or Google Cloud Run
- **Frontend:** Deploy to Vercel, Netlify, or AWS S3 + CloudFront
- **Database:** Use managed PostgreSQL (AWS RDS, Heroku Postgres, etc.)

## 📞 Support

For issues or questions, please:
1. Check the **Common Issues** section above
2. Review the documentation in `/docs`
3. Create an issue in the repository

## Current Status

**Completed:** Weeks 1-2 (Authentication, Competition CRUD, UI Implementation)
**Next:** Week 3 (Stripe Connect integration)

## 📄 License

MIT License - see LICENSE file for details

## 🙏 Acknowledgments

- Spring Boot
- React
- PostgreSQL
- Tailwind CSS
- Stripe

---

**Built with ❤️ for the sports community**
