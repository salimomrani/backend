# Kubernetes Deployment Guide - Backend Application

Ce guide explique comment déployer l'application backend Spring Boot sur un cluster Kubernetes (AWS EKS/kOps).

## 📋 Table des matières
- [Prérequis](#prérequis)
- [Architecture Kubernetes](#architecture-kubernetes)
- [Préparation de l'image Docker](#préparation-de-limage-docker)
- [Configuration des secrets](#configuration-des-secrets)
- [Déploiement](#déploiement)
- [Vérification du déploiement](#vérification-du-déploiement)
- [Configuration HTTPS/SSL](#configuration-httpsssl)
- [Monitoring et observabilité](#monitoring-et-observabilité)
- [Dépannage](#dépannage)

## 🔧 Prérequis

### Infrastructure AWS
- **Cluster Kubernetes** (EKS ou kOps) opérationnel
- **AWS Load Balancer Controller** installé ([guide d'installation](https://kubernetes-sigs.github.io/aws-load-balancer-controller/))
- **RDS PostgreSQL** instance accessible depuis le cluster
- **ACM Certificate** (optionnel, pour HTTPS)

### Outils locaux
- `kubectl` configuré pour accéder au cluster
- `docker` pour construire l'image
- `aws-cli` configuré avec les bonnes credentials
- `git` pour cloner le repository

### Permissions AWS IAM
Le cluster doit avoir les permissions pour:
- Créer/gérer des Application Load Balancers (ALB)
- Accéder à Amazon ECR (si vous utilisez ECR)
- Lire les certificats ACM

## 🏗 Architecture Kubernetes

Le déploiement comprend les ressources suivantes:

```
k8s/
├── namespace.yaml          # Namespace isolé "backend"
├── configmap.yaml          # Configuration non-sensible
├── secret.example.yaml     # Template pour les secrets
├── deployment.yaml         # 2+ pods avec health checks
├── service.yaml            # ClusterIP service
├── ingress.yaml            # ALB avec HTTPS
└── hpa.yaml                # Auto-scaling 2-10 pods
```

### Composants déployés
- **Namespace**: `backend` (isolation)
- **Deployment**: 2-10 replicas (auto-scaling)
- **Service**: ClusterIP sur port 80
- **Ingress**: AWS ALB avec support HTTPS
- **HPA**: Auto-scaling basé sur CPU (70%) et mémoire (80%)
- **Health checks**: Actuator endpoints `/actuator/health/*`

## 🐳 Préparation de l'image Docker

### Option 1: Docker Hub (public)

```bash
# Construire l'image
./mvnw clean package -DskipTests
docker build -t iconsultingdev/blog-backend:latest .

# Pousser vers Docker Hub
docker login
docker push iconsultingdev/blog-backend:latest

# Avec tag de version
docker tag iconsultingdev/blog-backend:latest iconsultingdev/blog-backend:v1.0.0
docker push iconsultingdev/blog-backend:v1.0.0
```

### Option 2: Amazon ECR (privé)

```bash
# Variables
AWS_REGION="us-east-1"
AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
ECR_REPO="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/backend"

# Créer le repository ECR (une seule fois)
aws ecr create-repository --repository-name backend --region ${AWS_REGION}

# Login à ECR
aws ecr get-login-password --region ${AWS_REGION} | \
  docker login --username AWS --password-stdin ${ECR_REPO}

# Construire et pousser
./mvnw clean package -DskipTests
docker build -t ${ECR_REPO}:latest .
docker push ${ECR_REPO}:latest
```

**Important**: Si vous utilisez ECR, mettez à jour `k8s/deployment.yaml`:
```yaml
spec:
  template:
    spec:
      containers:
        - name: backend
          image: YOUR_ECR_REPO:latest
```

## 🔐 Configuration des secrets

### Étape 1: Créer le namespace

```bash
kubectl apply -f k8s/namespace.yaml
```

### Étape 2: Générer une clé JWT forte

```bash
# Générer une clé secrète JWT (minimum 256 bits)
JWT_SECRET=$(openssl rand -base64 64 | tr -d '\n')
echo "JWT_SECRET_KEY: $JWT_SECRET"
```

### Étape 3: Créer le secret Kubernetes

**Option A: Depuis la ligne de commande (recommandé)**

```bash
kubectl create secret generic backend-secrets \
  --namespace backend \
  --from-literal=DB_URL='jdbc:postgresql://your-rds.region.rds.amazonaws.com:5432/backenddb' \
  --from-literal=DB_USERNAME='backend_user' \
  --from-literal=DB_PASSWORD='YOUR_STRONG_PASSWORD' \
  --from-literal=JWT_SECRET_KEY='YOUR_GENERATED_JWT_SECRET'
```

**Option B: Depuis un fichier .env**

```bash
# Créer un fichier .env.prod (ne PAS commiter!)
cat > .env.prod <<EOF
DB_URL=jdbc:postgresql://your-rds.region.rds.amazonaws.com:5432/backenddb
DB_USERNAME=backend_user
DB_PASSWORD=YOUR_STRONG_PASSWORD
JWT_SECRET_KEY=YOUR_GENERATED_JWT_SECRET
EOF

# Créer le secret
kubectl create secret generic backend-secrets \
  --namespace backend \
  --from-env-file=.env.prod

# Supprimer le fichier .env.prod
rm .env.prod
```

### Étape 4: Vérifier le secret

```bash
kubectl get secret backend-secrets -n backend
kubectl describe secret backend-secrets -n backend
```

## 🚀 Déploiement

### Déploiement complet (ordre recommandé)

```bash
# 1. Namespace (déjà fait si vous avez créé les secrets)
kubectl apply -f k8s/namespace.yaml

# 2. ConfigMap (configuration non-sensible)
kubectl apply -f k8s/configmap.yaml

# 3. Deployment (application)
kubectl apply -f k8s/deployment.yaml

# 4. Service (exposition interne)
kubectl apply -f k8s/service.yaml

# 5. Ingress (exposition externe via ALB)
kubectl apply -f k8s/ingress.yaml

# 6. HPA (auto-scaling)
kubectl apply -f k8s/hpa.yaml
```

### Déploiement en une commande

```bash
kubectl apply -f k8s/
```

## ✅ Vérification du déploiement

### Vérifier les pods

```bash
# Lister les pods
kubectl get pods -n backend

# Logs d'un pod
kubectl logs -f deployment/backend -n backend

# Décrire un pod (utile pour le debugging)
kubectl describe pod <pod-name> -n backend
```

### Vérifier les services

```bash
# Service ClusterIP
kubectl get service backend -n backend

# Endpoints (IPs des pods)
kubectl get endpoints backend -n backend
```

### Vérifier l'Ingress et l'ALB

```bash
# Ingress status
kubectl get ingress backend -n backend

# Obtenir l'URL du Load Balancer
kubectl get ingress backend -n backend -o jsonpath='{.status.loadBalancer.ingress[0].hostname}'
```

**Exemple de sortie**:
```
k8s-backend-backend-xxxxx-xxxxxxxxxx.us-east-1.elb.amazonaws.com
```

### Vérifier l'HPA

```bash
# Status HPA
kubectl get hpa backend -n backend

# Détails
kubectl describe hpa backend -n backend
```

### Tester les health checks

```bash
# Via port-forward (sans passer par l'ALB)
kubectl port-forward -n backend deployment/backend 8080:8080

# Dans un autre terminal
curl http://localhost:8080/actuator/health
curl http://localhost:8080/actuator/health/liveness
curl http://localhost:8080/actuator/health/readiness
```

### Tester l'API via l'ALB

```bash
# Récupérer l'URL de l'ALB
ALB_URL=$(kubectl get ingress backend -n backend -o jsonpath='{.status.loadBalancer.ingress[0].hostname}')

# Tester (remplacer par votre domaine si configuré)
curl http://$ALB_URL/actuator/health
curl http://$ALB_URL/api/v1/articles
```

## 🔒 Configuration HTTPS/SSL

### Prérequis
- Domaine configuré (ex: `api.kubevpro.i-consulting.shop`)
- Certificat ACM créé pour ce domaine

### Étape 1: Créer un certificat ACM

```bash
# Via AWS Console: ACM > Request Certificate
# OU via CLI:
aws acm request-certificate \
  --domain-name api.kubevpro.i-consulting.shop \
  --validation-method DNS \
  --region us-east-1
```

### Étape 2: Valider le certificat

Suivez les instructions AWS pour ajouter les enregistrements DNS de validation.

### Étape 3: Mettre à jour l'Ingress

Modifiez `k8s/ingress.yaml` ligne 15-16:

```yaml
# Décommenter et remplacer l'ARN du certificat
alb.ingress.kubernetes.io/certificate-arn: arn:aws:acm:us-east-1:123456789012:certificate/xxxxx
```

### Étape 4: Appliquer les changements

```bash
kubectl apply -f k8s/ingress.yaml
```

### Étape 5: Configurer le DNS

Créez un enregistrement CNAME dans votre DNS:

```
Type: CNAME
Name: api.kubevpro
Value: k8s-backend-backend-xxxxx-xxxxxxxxxx.us-east-1.elb.amazonaws.com
TTL: 300
```

### Étape 6: Tester HTTPS

```bash
curl https://api.kubevpro.i-consulting.shop/actuator/health
```

## 📊 Monitoring et observabilité

### Endpoints Actuator disponibles

- `/actuator/health` - Status global de l'application
- `/actuator/health/liveness` - Liveness probe (pod vivant?)
- `/actuator/health/readiness` - Readiness probe (prêt à recevoir du trafic?)
- `/actuator/info` - Informations sur l'application
- `/actuator/metrics` - Métriques de l'application

### Exposer Prometheus (optionnel)

Si vous avez Prometheus dans votre cluster:

1. Ajouter la dépendance Micrometer Prometheus dans `pom.xml`
2. Mettre à jour `configmap.yaml`:
```yaml
MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE: "health,info,metrics,prometheus"
```
3. Ajouter une annotation au Service pour le scraping:
```yaml
metadata:
  annotations:
    prometheus.io/scrape: "true"
    prometheus.io/path: "/actuator/prometheus"
    prometheus.io/port: "8080"
```

### Logs

```bash
# Logs en temps réel
kubectl logs -f deployment/backend -n backend

# Logs des 100 dernières lignes
kubectl logs --tail=100 deployment/backend -n backend

# Logs d'un pod spécifique
kubectl logs <pod-name> -n backend

# Logs précédents (si le pod a redémarré)
kubectl logs <pod-name> -n backend --previous
```

## 🔧 Dépannage

### Les pods ne démarrent pas

```bash
# Vérifier le statut des pods
kubectl get pods -n backend

# Décrire le pod pour voir les événements
kubectl describe pod <pod-name> -n backend

# Vérifier les logs
kubectl logs <pod-name> -n backend
```

**Problèmes courants**:
- ImagePullBackOff: L'image n'existe pas ou erreur d'authentification ECR
- CrashLoopBackOff: L'application crash au démarrage (vérifier les secrets/config)
- Pending: Pas assez de ressources dans le cluster

### Les health checks échouent

```bash
# Vérifier les probes
kubectl describe pod <pod-name> -n backend | grep -A 10 "Liveness\|Readiness"

# Tester manuellement via port-forward
kubectl port-forward <pod-name> -n backend 8080:8080
curl http://localhost:8080/actuator/health/liveness
```

### L'ALB n'est pas créé

```bash
# Vérifier les logs du Load Balancer Controller
kubectl logs -n kube-system deployment/aws-load-balancer-controller

# Vérifier l'Ingress
kubectl describe ingress backend -n backend
```

### Problèmes de connexion à la base de données

```bash
# Vérifier que le secret existe
kubectl get secret backend-secrets -n backend -o yaml

# Tester la connectivité depuis un pod
kubectl run -it --rm debug --image=postgres:15 --restart=Never -n backend -- bash
# Dans le pod:
psql "jdbc:postgresql://your-rds.region.rds.amazonaws.com:5432/backenddb" -U backend_user
```

### Mettre à jour l'application

```bash
# Nouvelle version de l'image
docker build -t iconsultingdev/blog-backend:v1.1.0 .
docker push iconsultingdev/blog-backend:v1.1.0

# Mettre à jour le deployment
kubectl set image deployment/backend backend=iconsultingdev/blog-backend:v1.1.0 -n backend

# OU éditer directement
kubectl edit deployment backend -n backend

# Vérifier le rollout
kubectl rollout status deployment/backend -n backend

# Rollback si nécessaire
kubectl rollout undo deployment/backend -n backend
```

### Scaling manuel

```bash
# Scaler à 5 replicas
kubectl scale deployment backend --replicas=5 -n backend

# Vérifier
kubectl get pods -n backend
```

## 📝 Notes importantes

### Sécurité
- ✅ Les pods tournent avec un utilisateur non-root (UID 1000)
- ✅ SecurityContext avec seccompProfile
- ✅ Secrets Kubernetes pour les données sensibles
- ✅ HTTPS recommandé en production
- ⚠️ Ne jamais commiter de secrets dans Git!

### Performance
- Resources requests/limits configurées pour éviter les OOM kills
- HPA avec métriques CPU et mémoire
- Rolling updates sans downtime (maxUnavailable: 0)
- Health checks optimisés avec startupProbe

### Coûts AWS
- ALB: ~$20-30/mois
- RDS PostgreSQL: variable selon l'instance
- EKS/kOps: coût des EC2 nodes
- Optimisez le nombre de replicas selon votre charge

## 📚 Ressources supplémentaires

- [AWS Load Balancer Controller](https://kubernetes-sigs.github.io/aws-load-balancer-controller/)
- [Kubernetes Best Practices](https://kubernetes.io/docs/concepts/configuration/overview/)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [Horizontal Pod Autoscaling](https://kubernetes.io/docs/tasks/run-application/horizontal-pod-autoscale/)

