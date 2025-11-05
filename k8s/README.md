# Kubernetes Deployment Guide

This directory contains Kubernetes manifests for deploying the Spring Boot backend application.

## Prerequisites

- Kubernetes cluster (v1.19+)
- `kubectl` CLI tool installed and configured
- Docker image built and pushed to registry (Docker Hub: `salimomrani/backend`)
- (Optional) Ingress controller installed (e.g., NGINX Ingress Controller)
- (Optional) Metrics Server for HPA (Horizontal Pod Autoscaler)

## Architecture

The deployment includes:

- **Deployment**: Runs 2 replicas of the backend application
- **Service**: ClusterIP service exposing the app on port 80
- **ConfigMap**: Application configuration (database, logging, etc.)
- **Ingress**: External access to the application (optional)
- **HPA**: Auto-scaling based on CPU/Memory usage (2-10 replicas)

## Quick Start

### 1. Build and Push Docker Image

```bash
# Build the Docker image
docker build -t salimomrani/backend:latest .

# Push to Docker Hub
docker push salimomrani/backend:latest
```

### 2. Deploy using Kustomize (Recommended)

```bash
# Deploy all resources
kubectl apply -k k8s/

# Verify deployment
kubectl get all -l app=backend

# Check logs
kubectl logs -l app=backend --tail=100 -f
```

### 3. Deploy using kubectl (Alternative)

```bash
# Deploy in order
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
kubectl apply -f k8s/ingress.yaml
kubectl apply -f k8s/hpa.yaml
```

## Access the Application

### Using Port-Forward (Development)

```bash
kubectl port-forward svc/backend 8080:80
```

Then access:
- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- H2 Console: http://localhost:8080/h2-console

### Using Ingress (Production)

Update `ingress.yaml` with your domain name:

```yaml
spec:
  rules:
  - host: backend.yourdomain.com  # Change this
```

Then access: http://backend.yourdomain.com

## Configuration

### Environment Variables

Configuration is managed via ConfigMap (`configmap.yaml`). To update:

```bash
# Edit the ConfigMap
kubectl edit configmap backend-config

# Restart pods to apply changes
kubectl rollout restart deployment/backend
```

### Update Docker Image

```bash
# Using kubectl
kubectl set image deployment/backend backend=salimomrani/backend:v2.0.0

# Using kustomize (edit kustomization.yaml first)
kubectl apply -k k8s/
```

## Scaling

### Manual Scaling

```bash
# Scale to 5 replicas
kubectl scale deployment/backend --replicas=5
```

### Auto-Scaling (HPA)

The HPA is configured to scale between 2-10 replicas based on:
- CPU utilization: 70%
- Memory utilization: 80%

```bash
# Check HPA status
kubectl get hpa backend

# Describe HPA for details
kubectl describe hpa backend
```

**Note**: Requires Metrics Server to be installed:

```bash
kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml
```

## Monitoring

### Check Pod Status

```bash
kubectl get pods -l app=backend
```

### View Logs

```bash
# All pods
kubectl logs -l app=backend --tail=100 -f

# Specific pod
kubectl logs backend-<pod-id> -f
```

### Check Events

```bash
kubectl get events --sort-by='.lastTimestamp' | grep backend
```

### Health Checks

The deployment includes three probes:

- **Liveness Probe**: Checks if the container is alive (restarts if failing)
- **Readiness Probe**: Checks if the container is ready to serve traffic
- **Startup Probe**: Allows slow-starting containers to fully initialize

All probes use `/actuator/health` endpoint.

## Troubleshooting

### Pods Not Starting

```bash
# Check pod status
kubectl describe pod backend-<pod-id>

# Check logs
kubectl logs backend-<pod-id>

# Check events
kubectl get events --field-selector involvedObject.name=backend-<pod-id>
```

### ImagePullBackOff Error

```bash
# Ensure image exists in registry
docker pull salimomrani/backend:latest

# Check image name in deployment.yaml
kubectl get deployment backend -o yaml | grep image:
```

### Service Not Accessible

```bash
# Check service endpoints
kubectl get endpoints backend

# Test service internally
kubectl run test-pod --rm -it --image=curlimages/curl -- /bin/sh
curl http://backend/actuator/health
```

### HPA Not Working

```bash
# Check metrics server
kubectl get deployment metrics-server -n kube-system

# Check pod metrics
kubectl top pods -l app=backend

# Check HPA conditions
kubectl describe hpa backend
```

## Cleanup

### Remove all resources

```bash
# Using kustomize
kubectl delete -k k8s/

# Using kubectl
kubectl delete -f k8s/
```

## Production Considerations

### 1. Enable SSL/TLS

Update `ingress.yaml` to use HTTPS with cert-manager:

```yaml
annotations:
  cert-manager.io/cluster-issuer: "letsencrypt-prod"
tls:
- hosts:
  - backend.yourdomain.com
  secretName: backend-tls
```

### 2. Use External Database

Replace H2 in-memory database with PostgreSQL/MySQL:

1. Update `configmap.yaml` with database credentials
2. Create Secret for sensitive data (password)
3. Update deployment to use Secret

Example:

```yaml
# Create Secret
kubectl create secret generic backend-db-secret \
  --from-literal=password='your-db-password'

# Update deployment.yaml
env:
- name: SPRING_DATASOURCE_PASSWORD
  valueFrom:
    secretKeyRef:
      name: backend-db-secret
      key: password
```

### 3. Configure Resource Limits

Adjust CPU/Memory limits in `deployment.yaml` based on your needs:

```yaml
resources:
  requests:
    memory: "512Mi"
    cpu: "250m"
  limits:
    memory: "1Gi"
    cpu: "500m"
```

### 4. Enable Monitoring

- Install Prometheus/Grafana for metrics
- Configure Spring Boot Actuator metrics endpoint
- Set up alerting rules

### 5. Set Up CI/CD

Integrate with GitHub Actions (already configured in `.github/workflows/`):

```yaml
- name: Deploy to Kubernetes
  run: |
    kubectl set image deployment/backend backend=salimomrani/backend:${{ github.sha }}
```

## Resources

- **ConfigMap**: Application configuration
- **Deployment**: 2-10 replicas (via HPA)
- **Service**: ClusterIP on port 80
- **Ingress**: NGINX ingress controller
- **HPA**: Auto-scaling based on CPU/Memory

## Default Resource Allocation

Per Pod:
- Memory Request: 512Mi
- Memory Limit: 1Gi
- CPU Request: 250m
- CPU Limit: 500m

With 2 replicas (default):
- Total Memory: ~2Gi
- Total CPU: ~1 core

## Support

For issues or questions:
- Check logs: `kubectl logs -l app=backend`
- Review events: `kubectl get events`
- Consult documentation: [Spring Boot](https://spring.io/projects/spring-boot)
