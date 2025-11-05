# Kubernetes Deployment Guide

This directory contains all Kubernetes manifests for deploying the backend application to a Kubernetes cluster.

## Architecture Overview

```
Browser (Angular: https://blog.kubevpro.i-consulting.shop)
    ↓ HTTPS
Ingress (api.kubevpro.i-consulting.shop) - AWS ALB
    ↓
Service (ClusterIP:80)
    ↓
Deployment (2-10 pods with HPA)
    ↓
Container (backend:8080)
```

## Prerequisites

1. **Kubernetes Cluster** with AWS Load Balancer Controller installed
2. **GitLab CI/CD Variables** configured:
   - `DOCKER_USERNAME`: Docker Hub username
   - `DOCKER_PASSWORD`: Docker Hub password
   - `KUBECONFIG_CONTENT`: Base64-encoded kubeconfig file
   - `KUBE_CONTEXT`: (Optional) Kubernetes context name

3. **ACM Certificate** for HTTPS (update in `ingress.yaml`)

## Manifests

| File | Description |
|------|-------------|
| `namespace.yaml` | Creates the `backend` namespace |
| `configmap.yaml` | Non-sensitive configuration (Spring profiles, logging, etc.) |
| `deployment.yaml` | Application deployment with 2 replicas, health probes |
| `service.yaml` | ClusterIP service exposing port 80 → 8080 |
| `ingress.yaml` | AWS ALB Ingress for `api.kubevpro.i-consulting.shop` |
| `hpa.yaml` | Horizontal Pod Autoscaler (2-10 pods based on CPU/Memory) |

## Setup Instructions

### 1. Create Kubernetes Secrets

Before deploying, create the required secrets:

```bash
kubectl create secret generic backend-secrets -n backend \
  --from-literal=DB_URL='jdbc:h2:mem:backenddb' \
  --from-literal=DB_USERNAME='sa' \
  --from-literal=DB_PASSWORD='' \
  --from-literal=DB_DRIVER='org.h2.Driver' \
  --from-literal=JPA_DIALECT='org.hibernate.dialect.H2Dialect'
```

**For production with PostgreSQL/MySQL:**

```bash
kubectl create secret generic backend-secrets -n backend \
  --from-literal=DB_URL='jdbc:postgresql://host:5432/dbname' \
  --from-literal=DB_USERNAME='your-db-user' \
  --from-literal=DB_PASSWORD='your-db-password' \
  --from-literal=DB_DRIVER='org.postgresql.Driver' \
  --from-literal=JPA_DIALECT='org.hibernate.dialect.PostgreSQLDialect'
```

### 2. Configure ACM Certificate (HTTPS)

Edit `ingress.yaml` and uncomment the certificate ARN:

```yaml
alb.ingress.kubernetes.io/certificate-arn: arn:aws:acm:us-east-1:123456789012:certificate/abc-123
```

### 3. Deploy via GitLab CI/CD

1. Push your code to the `main` branch
2. Go to GitLab CI/CD → Pipelines
3. Wait for `build` and `docker` stages to complete
4. Manually trigger the `deploy_production` job

### 4. Manual Deployment (without CI/CD)

```bash
# Apply all manifests
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/service.yaml
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/ingress.yaml
kubectl apply -f k8s/hpa.yaml

# Check deployment status
kubectl get all -n backend
kubectl get ingress -n backend
```

## Health Checks

The application exposes Spring Boot Actuator endpoints:

- **Liveness Probe**: `/actuator/health/liveness` (checks if app is running)
- **Readiness Probe**: `/actuator/health/readiness` (checks if app can receive traffic)
- **General Health**: `/actuator/health`

## Scaling

The HPA automatically scales based on:
- **CPU**: Scale up when > 70% utilization
- **Memory**: Scale up when > 80% utilization

Min replicas: 2
Max replicas: 10

## CORS Configuration

The backend allows requests from:
- `http://localhost:4200` (local dev)
- `http://localhost:3000` (local dev)
- `https://blog.kubevpro.i-consulting.shop` (production Angular app)

## Troubleshooting

### Pods not starting

```bash
kubectl get pods -n backend
kubectl describe pod <pod-name> -n backend
kubectl logs <pod-name> -n backend
```

### Health probes failing

```bash
# Check if Actuator is responding
kubectl exec -it <pod-name> -n backend -- wget -O- http://localhost:8080/actuator/health
```

### Ingress not working

```bash
kubectl describe ingress backend -n backend
kubectl get events -n backend --sort-by='.lastTimestamp'
```

### Secret not found error

```bash
# Verify secret exists
kubectl get secret backend-secrets -n backend

# Check secret contents (base64 encoded)
kubectl get secret backend-secrets -n backend -o yaml
```

## Accessing the Application

- **API Endpoint**: https://api.kubevpro.i-consulting.shop
- **Swagger UI**: https://api.kubevpro.i-consulting.shop/swagger-ui.html
- **Health Check**: https://api.kubevpro.i-consulting.shop/actuator/health
- **API Docs**: https://api.kubevpro.i-consulting.shop/api-docs

## Updating the Deployment

### Via GitLab CI/CD

Push to `main` branch, and the pipeline will:
1. Build the JAR
2. Build and push Docker image with tag `main-<commit-sha>`
3. Deploy to Kubernetes (manual trigger)

### Manual Update

```bash
kubectl set image deployment/backend backend=iconsultingdev/blog-backend:main-abc1234 -n backend
kubectl rollout status deployment/backend -n backend
```

## Rollback

```bash
# View rollout history
kubectl rollout history deployment/backend -n backend

# Rollback to previous version
kubectl rollout undo deployment/backend -n backend

# Rollback to specific revision
kubectl rollout undo deployment/backend -n backend --to-revision=2
```

## Clean Up

```bash
# Delete all resources
kubectl delete -f k8s/

# Or delete namespace (removes everything)
kubectl delete namespace backend
```
