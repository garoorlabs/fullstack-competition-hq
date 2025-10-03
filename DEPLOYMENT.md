# Deployment Guide

This guide covers deploying LeagueHQ to production using Railway (backend) and Vercel (frontend).

## Overview

- **Backend**: Deploy to Railway with PostgreSQL addon
- **Frontend**: Deploy to Vercel
- **Database**: Railway PostgreSQL addon
- **Cost**: Free tier available for both platforms

## Prerequisites

- GitHub repository with your code
- Railway account (https://railway.app)
- Vercel account (https://vercel.com)
- Stripe account for payment processing

---

## Backend Deployment (Railway)

### 1. Create Railway Project

1. Go to https://railway.app and sign in
2. Click **"New Project"**
3. Select **"Deploy from GitHub repo"**
4. Choose your repository
5. Railway will auto-detect Spring Boot

### 2. Add PostgreSQL Database

1. In your Railway project, click **"New"** → **"Database"** → **"Add PostgreSQL"**
2. Railway will provision a PostgreSQL database and provide connection details

### 3. Configure Environment Variables

In Railway project settings, add these environment variables:

```bash
# Database (Railway provides these automatically)
SPRING_DATASOURCE_URL=postgresql://...  # Auto-filled by Railway
SPRING_DATASOURCE_USERNAME=postgres      # Auto-filled by Railway
SPRING_DATASOURCE_PASSWORD=...          # Auto-filled by Railway

# JWT Configuration
JWT_SECRET=your-production-secret-key-minimum-256-bits-change-this
JWT_EXPIRATION=86400000

# Stripe Configuration
STRIPE_API_KEY=sk_live_...
STRIPE_WEBHOOK_SECRET=whsec_...

# CORS Configuration (add your Vercel frontend URL)
ALLOWED_ORIGINS=https://your-app.vercel.app
```

### 4. Deploy

1. Railway will automatically deploy on every push to main branch
2. Get your backend URL from Railway dashboard (e.g., `https://your-app.up.railway.app`)
3. Note this URL for frontend configuration

### 5. Verify Deployment

```bash
curl https://your-app.up.railway.app/actuator/health
# Should return: {"status":"UP"}
```

---

## Frontend Deployment (Vercel)

### 1. Create Vercel Project

1. Go to https://vercel.com and sign in
2. Click **"Add New Project"**
3. Import your GitHub repository
4. Vercel will auto-detect Vite

### 2. Configure Build Settings

Vercel should auto-detect these settings:

- **Framework Preset**: Vite
- **Root Directory**: `frontend`
- **Build Command**: `npm run build`
- **Output Directory**: `dist`

### 3. Configure Environment Variables

In Vercel project settings → Environment Variables, add:

```bash
# Backend API URL (your Railway backend URL)
VITE_API_URL=https://your-app.up.railway.app/api

# Stripe Publishable Key
VITE_STRIPE_PUBLISHABLE_KEY=pk_live_...
```

### 4. Deploy

1. Click **"Deploy"**
2. Vercel will build and deploy your frontend
3. Get your frontend URL (e.g., `https://your-app.vercel.app`)

### 5. Update Backend CORS

Go back to Railway and update the `ALLOWED_ORIGINS` environment variable with your Vercel URL:

```bash
ALLOWED_ORIGINS=https://your-app.vercel.app
```

Redeploy the backend for changes to take effect.

---

## Backend CORS Configuration

Add this configuration to your Spring Boot backend to allow frontend requests:

**File**: `backend/src/main/java/com/leaguehq/config/CorsConfig.java`

```java
package com.leaguehq.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Value("${allowed.origins:http://localhost:5173}")
    private String allowedOrigins;

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);

        return new CorsFilter(source);
    }
}
```

---

## Stripe Configuration

### 1. Stripe Account Setup

1. Create a Stripe account at https://stripe.com
2. Complete account verification
3. Enable Stripe Connect in your dashboard

### 2. Get API Keys

1. Go to **Developers** → **API Keys**
2. Copy your **Publishable Key** (starts with `pk_live_`)
3. Copy your **Secret Key** (starts with `sk_live_`)

### 3. Configure Webhooks

1. Go to **Developers** → **Webhooks**
2. Click **"Add endpoint"**
3. Enter your backend URL: `https://your-app.up.railway.app/api/webhooks/stripe`
4. Select events to listen to:
   - `checkout.session.completed`
   - `payment_intent.succeeded`
   - `payment_intent.payment_failed`
5. Copy the **Webhook Secret** (starts with `whsec_`)

### 4. Update Environment Variables

Add Stripe keys to both Railway (backend) and Vercel (frontend):

**Railway (Backend)**:
```bash
STRIPE_API_KEY=sk_live_...
STRIPE_WEBHOOK_SECRET=whsec_...
```

**Vercel (Frontend)**:
```bash
VITE_STRIPE_PUBLISHABLE_KEY=pk_live_...
```

---

## Environment Variables Checklist

### Backend (Railway)

- [ ] `SPRING_DATASOURCE_URL` (auto-provided by Railway PostgreSQL)
- [ ] `SPRING_DATASOURCE_USERNAME` (auto-provided by Railway PostgreSQL)
- [ ] `SPRING_DATASOURCE_PASSWORD` (auto-provided by Railway PostgreSQL)
- [ ] `JWT_SECRET` (generate a secure 256-bit key)
- [ ] `JWT_EXPIRATION` (86400000 = 24 hours)
- [ ] `STRIPE_API_KEY` (from Stripe dashboard)
- [ ] `STRIPE_WEBHOOK_SECRET` (from Stripe webhooks)
- [ ] `ALLOWED_ORIGINS` (your Vercel frontend URL)

### Frontend (Vercel)

- [ ] `VITE_API_URL` (your Railway backend URL + /api)
- [ ] `VITE_STRIPE_PUBLISHABLE_KEY` (from Stripe dashboard)

---

## Post-Deployment Checklist

### 1. Test Authentication
- [ ] Sign up with a new account
- [ ] Log in with existing account
- [ ] Verify JWT token is stored
- [ ] Test protected routes

### 2. Test Competition Creation
- [ ] Create a new competition
- [ ] Verify data persists in database
- [ ] Check competition appears in "My Competitions"

### 3. Test CORS
- [ ] Verify no CORS errors in browser console
- [ ] Test API requests from frontend
- [ ] Verify preflight OPTIONS requests work

### 4. Test Stripe (When Implemented)
- [ ] Test payment flow
- [ ] Verify webhook events are received
- [ ] Check payment status updates

### 5. Monitor Logs
- [ ] Check Railway logs for errors
- [ ] Check Vercel logs for build/runtime errors
- [ ] Set up error monitoring (optional: Sentry)

---

## Troubleshooting

### Issue 1: CORS Errors in Production

**Error**: `Access to XMLHttpRequest blocked by CORS policy`

**Solution**:
1. Verify `ALLOWED_ORIGINS` in Railway includes your Vercel URL
2. Make sure CorsConfig.java is present in backend
3. Redeploy backend after adding CORS configuration

### Issue 2: API Requests Failing

**Error**: `Failed to load resource: net::ERR_NAME_NOT_RESOLVED`

**Solution**:
1. Verify `VITE_API_URL` in Vercel matches your Railway backend URL
2. Make sure Railway backend is deployed and running
3. Test backend health endpoint: `https://your-app.up.railway.app/actuator/health`

### Issue 3: Database Connection Failed

**Error**: `Connection refused` or `Database connection failed`

**Solution**:
1. Verify PostgreSQL addon is added to Railway project
2. Check database environment variables are set correctly
3. Railway auto-injects database credentials - don't override them

### Issue 4: Stripe Webhooks Not Working

**Error**: Payments succeed but status not updated in app

**Solution**:
1. Verify webhook endpoint URL is correct in Stripe dashboard
2. Check webhook secret matches `STRIPE_WEBHOOK_SECRET` in Railway
3. Test webhook with Stripe CLI: `stripe trigger checkout.session.completed`

### Issue 5: JWT Authentication Failing

**Error**: `Invalid token` or `Token expired`

**Solution**:
1. Verify `JWT_SECRET` is set in Railway and is at least 256 bits
2. Check token expiration time is reasonable (86400000 = 24 hours)
3. Clear browser localStorage and login again

---

## Monitoring and Maintenance

### Railway Dashboard
- Monitor backend logs for errors
- Track database usage and storage
- Set up automatic backups for PostgreSQL

### Vercel Dashboard
- Monitor build and deployment logs
- Track function invocations and bandwidth
- Set up custom domains

### Database Backups
Railway provides automatic backups. To manually backup:
1. Go to Railway PostgreSQL addon
2. Click **"Backups"**
3. Download backup or restore from previous backup

---

## Scaling Considerations

### When to Scale

**Backend (Railway)**:
- Upgrade plan if you hit resource limits
- Consider adding Redis for session storage
- Enable connection pooling for database

**Frontend (Vercel)**:
- Vercel auto-scales based on traffic
- Consider CDN for static assets
- Enable Vercel Analytics for performance insights

**Database**:
- Monitor query performance
- Add indexes for frequently queried fields
- Consider read replicas for high traffic

---

## Security Checklist

- [ ] Use HTTPS for all production URLs
- [ ] Rotate JWT secret regularly
- [ ] Use Stripe live keys (not test keys)
- [ ] Enable rate limiting on backend
- [ ] Set up CSP headers
- [ ] Enable database backups
- [ ] Use environment variables for all secrets
- [ ] Never commit .env files to Git

---

## Rollback Strategy

### Backend Rollback (Railway)
1. Go to Railway project
2. Click **"Deployments"**
3. Find previous successful deployment
4. Click **"Redeploy"**

### Frontend Rollback (Vercel)
1. Go to Vercel project
2. Click **"Deployments"**
3. Find previous deployment
4. Click **"···"** → **"Promote to Production"**

---

## Cost Estimates

### Free Tier Limits

**Railway Free Tier**:
- $5 credit per month
- Good for small projects
- Upgrade to Hobby ($5/month) or Pro ($20/month) as needed

**Vercel Free Tier**:
- 100 GB bandwidth per month
- Unlimited deployments
- Good for most small to medium projects

**Total Estimated Cost**:
- Free tier: $0/month
- Small production app: $5-10/month (Railway Hobby)
- Medium app: $20-30/month (Railway Pro)

---

## Support

For deployment issues:
- Railway: https://railway.app/help
- Vercel: https://vercel.com/support
- Stripe: https://support.stripe.com

For application issues:
- Check backend logs in Railway
- Check frontend logs in Vercel
- Review this deployment guide
- Create an issue in the repository
