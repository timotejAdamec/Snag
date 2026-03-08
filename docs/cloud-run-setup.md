# Cloud Run Setup Guide

This guide walks through the one-time GCP and GitHub setup required before the CD workflow (`deploy.yml`) can deploy the Snag backend to Cloud Run.

## Prerequisites

- A GCP project with billing enabled
- `gcloud` CLI installed and authenticated (`gcloud auth login`)
- GitHub repository admin access

## 1. Enable GCP APIs

```bash
export PROJECT_ID="your-gcp-project-id"
gcloud config set project $PROJECT_ID

gcloud services enable \
  run.googleapis.com \
  artifactregistry.googleapis.com \
  iamcredentials.googleapis.com \
  cloudresourcemanager.googleapis.com
```

## 2. Create Artifact Registry Repository

The workflow pushes Docker images here before deploying to Cloud Run.

```bash
gcloud artifacts repositories create snag \
  --repository-format=docker \
  --location=europe-west3 \
  --description="Snag server Docker images"
```

## 3. Create GCS Buckets

One bucket per environment for file storage.

```bash
gcloud storage buckets create gs://snag-bucket-dev --location=europe-west3
gcloud storage buckets create gs://snag-bucket-demo --location=europe-west3
```

## 4. Set Up Workload Identity Federation

This lets GitHub Actions authenticate to GCP without a service account key.

### Create a service account

```bash
gcloud iam service-accounts create snag-github-deploy \
  --display-name="Snag GitHub Actions Deploy"

export SA_EMAIL="snag-github-deploy@${PROJECT_ID}.iam.gserviceaccount.com"
```

### Grant roles to the service account

```bash
# Deploy to Cloud Run
gcloud projects add-iam-policy-binding $PROJECT_ID \
  --member="serviceAccount:${SA_EMAIL}" \
  --role="roles/run.admin"

# Push images to Artifact Registry
gcloud projects add-iam-policy-binding $PROJECT_ID \
  --member="serviceAccount:${SA_EMAIL}" \
  --role="roles/artifactregistry.writer"

# Cloud Run needs to pull images
gcloud projects add-iam-policy-binding $PROJECT_ID \
  --member="serviceAccount:${SA_EMAIL}" \
  --role="roles/iam.serviceAccountUser"

# Access GCS buckets (the Cloud Run service runs as this SA)
gcloud projects add-iam-policy-binding $PROJECT_ID \
  --member="serviceAccount:${SA_EMAIL}" \
  --role="roles/storage.objectAdmin"
```

### Create the Workload Identity Pool and Provider

```bash
gcloud iam workload-identity-pools create github-pool \
  --location="global" \
  --display-name="GitHub Actions Pool"

gcloud iam workload-identity-pools providers create-oidc github-provider \
  --location="global" \
  --workload-identity-pool="github-pool" \
  --display-name="GitHub Provider" \
  --attribute-mapping="google.subject=assertion.sub,attribute.repository=assertion.repository" \
  --issuer-uri="https://token.actions.githubusercontent.com"
```

### Allow the GitHub repo to impersonate the service account

Replace `OWNER/REPO` with your GitHub repository (e.g., `timotejAdamec/Snag`).

```bash
export REPO="OWNER/REPO"

gcloud iam service-accounts add-iam-policy-binding $SA_EMAIL \
  --role="roles/iam.workloadIdentityUser" \
  --member="principalSet://iam.googleapis.com/projects/$(gcloud projects describe $PROJECT_ID --format='value(projectNumber)')/locations/global/workloadIdentityPools/github-pool/attribute.repository/${REPO}"
```

### Get the WIF provider resource name

You'll need this for the GitHub secret in the next step.

```bash
gcloud iam workload-identity-pools providers describe github-provider \
  --location="global" \
  --workload-identity-pool="github-pool" \
  --format="value(name)"
```

This outputs something like:
```
projects/123456789/locations/global/workloadIdentityPools/github-pool/providers/github-provider
```

## 5. Configure GitHub Repository

### Secrets (Settings > Secrets and variables > Actions > Secrets)

These are shared across all environments.

| Secret | Value |
|---|---|
| `GCP_PROJECT_ID` | Your GCP project ID |
| `WIF_PROVIDER` | The full provider resource name from step 4 |
| `WIF_SERVICE_ACCOUNT` | `snag-github-deploy@<PROJECT_ID>.iam.gserviceaccount.com` |

### Environments (Settings > Environments)

Create two environments: **dev** and **demo**.

For each environment, add these **variables** (not secrets):

| Variable | `dev` | `demo` |
|---|---|---|
| `GCS_BUCKET_NAME` | `snag-bucket-dev` | `snag-bucket-demo` |
| `CORS_ALLOWED_HOSTS` | Frontend URL once deployed | Frontend URL once deployed |
| `SEED_DATA` | `true` | `true` |
| `LOG_LEVEL` | `debug` | `info` |

## 6. Deploy

1. Go to **Actions** > **Deploy to Cloud Run**
2. Click **Run workflow**
3. Select the environment (`dev` or `demo`)
4. Click **Run workflow**

The workflow builds the Docker image, pushes it to Artifact Registry, and deploys it to Cloud Run. The service URL is printed in the workflow output.

## Environment Variables Reference

These are read by the application at startup with local-dev defaults:

| Variable | Default | Purpose |
|---|---|---|
| `PORT` | `8081` | Server port (set automatically by Cloud Run) |
| `GCS_BUCKET_NAME` | `snag-bucket-dev` | GCS bucket for file storage |
| `CORS_ALLOWED_HOSTS` | `localhost:8080` | Comma-separated CORS origins (e.g., `https://app.example.com,https://admin.example.com`) |
| `SEED_DATA` | `true` | Seed sample data on startup |
| `LOG_LEVEL` | `info` | Logback root log level (`trace`, `debug`, `info`, `warn`, `error`) |

## Troubleshooting

**Deployment fails with "Permission denied"**
- Verify the service account has all roles from step 4
- Verify the WIF provider attribute mapping matches your repository

**Container starts but crashes**
- Check Cloud Run logs: `gcloud run services logs read snag-server-dev --region=europe-west3`
- Ensure `PORT` is not overridden — Cloud Run sets it automatically

**CORS errors in the browser**
- Verify `CORS_ALLOWED_HOSTS` includes the full origin with scheme (e.g., `https://app.example.com`)
- Origins without a scheme default to `http` and `https`
