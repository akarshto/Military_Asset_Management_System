# Deploying MAMS

## 1. Push to GitHub
Push the `MAMS/` folder (contains `backend/`, `frontend/`, `render.yaml`) to a GitHub repo.

## 2. Backend + DB on Render
1. Render dashboard > New > Blueprint > select the repo (reads `render.yaml`).
2. It creates a Postgres DB (`mams-db`) and the Docker web service `mams-backend`.
3. When prompted, leave `CORS_ALLOWED_ORIGIN` blank for now (or put a placeholder).
4. After deploy, note the URL: `https://mams-backend-xxxx.onrender.com`.
   The seeded users are created on first boot (DataInitializer).

## 3. Frontend on Vercel (or Netlify)
- Root directory: `frontend`, build: `npm run build`, output: `dist`.
- Environment variable: `VITE_API_URL=https://mams-backend-xxxx.onrender.com/api`
- Deploy; note the URL, e.g. `https://mams.vercel.app`.

## 4. Close the loop (CORS)
In Render > mams-backend > Environment, set
`CORS_ALLOWED_ORIGIN=https://mams.vercel.app` (comma-separate multiple origins). Redeploy.

Note: Render's free tier sleeps after inactivity; first request can take ~50s.
