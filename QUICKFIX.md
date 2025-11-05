# ⚡ QUICKFIX : Résolution immédiate du problème de déploiement

## 🔴 Erreur actuelle
```
Waiting for deployment "backend" rollout to finish: 1 out of 2 new replicas have been updated...
error: timed out waiting for the condition
```

---

## 🎯 SOLUTION RAPIDE (5 minutes)

### Étape 1 : Vérifier si l'image Docker est à jour

**Le problème :** L'image Docker ne contient pas les modifications Actuator.

```bash
# Vérifier quelle image est déployée
kubectl get deployment backend -n backend -o jsonpath='{.spec.template.spec.containers[0].image}'
```

**Si l'image n'est pas la dernière :**

GitLab doit d'abord **rebuild l'image** avant de déployer. Vérifiez que :
1. Le job `build` est ✅ passé
2. Le job `docker` est ✅ passé (construit + push l'image)
3. **Seulement après**, lancez `deploy_production`

---

### Étape 2 : Diagnostic rapide des pods

```bash
# 1. Voir l'état des pods
kubectl get pods -n backend

# 2. Voir les détails du pod qui ne démarre pas
kubectl describe pod <pod-name> -n backend | grep -A 10 "Events:"

# 3. Voir les logs
kubectl logs <pod-name> -n backend
```

**Erreurs fréquentes :**
| Erreur dans les logs | Cause | Solution |
|---------------------|-------|----------|
| `Secret "backend-secrets" not found` | Secret manquant | Voir Étape 3 |
| `Failed to load ApplicationContext` | Config Spring incorrecte | Rebuild image |
| `404 on /actuator/health` | Image périmée | Rebuild image |
| Aucun log | Pod ne démarre pas | Voir Étape 4 |

---

### Étape 3 : Créer le secret (si manquant)

```bash
kubectl create secret generic backend-secrets -n backend \
  --from-literal=DB_URL='jdbc:h2:mem:backenddb' \
  --from-literal=DB_USERNAME='sa' \
  --from-literal=DB_PASSWORD='' \
  --from-literal=DB_DRIVER='org.h2.Driver' \
  --from-literal=JPA_DIALECT='org.hibernate.dialect.H2Dialect'
```

Puis redémarrez :
```bash
kubectl rollout restart deployment/backend -n backend
```

---

### Étape 4 : Utiliser le déploiement simplifié

Si le problème persiste, utilisez le déploiement simplifié (sans security context, 1 replica, probes simplifiées) :

```bash
# Appliquer le déploiement simplifié
kubectl apply -f k8s/deployment-simple.yaml

# Attendre 2 minutes
sleep 120

# Vérifier
kubectl get pods -n backend
kubectl logs -f deployment/backend -n backend
```

---

### Étape 5 : Tester manuellement le pod

Si un pod tourne mais n'est pas "Ready" :

```bash
POD_NAME=$(kubectl get pod -n backend -l app=backend -o jsonpath='{.items[0].metadata.name}')

# Entrer dans le pod
kubectl exec -it $POD_NAME -n backend -- /bin/sh

# Tester les endpoints
wget -qO- http://localhost:8080/actuator/health
wget -qO- http://localhost:8080/actuator/health/liveness
wget -qO- http://localhost:8080/actuator/health/readiness
```

**Si 404 :** L'image ne contient pas les modifications Actuator → Rebuild obligatoire

---

## 🚀 REBUILD DE L'IMAGE (méthode manuelle)

Si vous ne pouvez pas attendre GitLab CI/CD :

```bash
# 1. Cloner le repo (si pas déjà fait)
git clone <repo-url>
cd backend

# 2. Récupérer les dernières modifications
git checkout main
git pull

# 3. Builder le JAR
./mvnw clean install -DskipTests

# 4. Builder l'image Docker
docker build -t iconsultingdev/blog-backend:latest .

# 5. Se connecter à Docker Hub
docker login

# 6. Pusher l'image
docker push iconsultingdev/blog-backend:latest

# 7. Forcer le redémarrage avec pull de la nouvelle image
kubectl rollout restart deployment/backend -n backend
kubectl rollout status deployment/backend -n backend --timeout=10m
```

---

## 🛠️ DÉPLOIEMENT MANUEL PAS-À-PAS

Pour un contrôle total et un diagnostic facile :

```bash
./k8s/deploy-manual.sh
```

Ce script interactif vous guide étape par étape et vous permet de tester chaque composant.

---

## 🔍 SCRIPT DE DIAGNOSTIC AUTOMATIQUE

```bash
./k8s/debug.sh
```

Affiche :
- État des pods, deployment, ingress, HPA
- Logs détaillés
- Événements Kubernetes
- Test des endpoints Actuator
- Vérification des secrets/ConfigMap

---

## ✅ VÉRIFICATION FINALE

Une fois déployé avec succès, testez :

```bash
# 1. Tous les pods sont Running et Ready
kubectl get pods -n backend

# Attendu :
# NAME                       READY   STATUS    RESTARTS
# backend-xxxxx-xxxxx        1/1     Running   0

# 2. Endpoint health fonctionne
kubectl exec <pod-name> -n backend -- wget -qO- http://localhost:8080/actuator/health

# Attendu : {"status":"UP"}

# 3. L'Ingress est créé (si certificat SSL configuré)
kubectl get ingress -n backend

# 4. L'API est accessible depuis l'extérieur
curl https://api.kubevpro.i-consulting.shop/actuator/health
```

---

## 📞 SI RIEN NE FONCTIONNE

### Option 1 : Supprimer et recréer tout

```bash
# Supprimer le namespace (⚠️ supprime tout)
kubectl delete namespace backend

# Attendre 30 secondes
sleep 30

# Redéployer depuis zéro
kubectl apply -f k8s/namespace.yaml
kubectl create secret generic backend-secrets -n backend \
  --from-literal=DB_URL='jdbc:h2:mem:backenddb' \
  --from-literal=DB_USERNAME='sa' \
  --from-literal=DB_PASSWORD=''
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/service.yaml
kubectl apply -f k8s/deployment-simple.yaml
```

### Option 2 : Déploiement local avec Minikube

```bash
# Tester localement avec Minikube
minikube start
eval $(minikube docker-env)
docker build -t backend:latest .
kubectl apply -f k8s/
minikube service backend -n backend
```

---

## 🎯 CHECKLIST DE PRÉ-DÉPLOIEMENT

Avant chaque tentative de déploiement :

- [ ] Image Docker construite avec les dernières modifications
- [ ] Image pushée sur Docker Hub
- [ ] Secret `backend-secrets` existe dans le cluster
- [ ] ConfigMap `backend-config` appliqué
- [ ] Cluster Kubernetes accessible (kubectl cluster-info)
- [ ] Certificat SSL configuré (si HTTPS requis)
- [ ] Variables GitLab CI/CD configurées :
  - [ ] DOCKER_USERNAME
  - [ ] DOCKER_PASSWORD
  - [ ] KUBECONFIG_CONTENT

---

## 📚 DOCUMENTATION COMPLÈTE

- `TROUBLESHOOTING.md` : Guide détaillé de dépannage
- `k8s/README.md` : Documentation Kubernetes complète
- `k8s/debug.sh` : Script de diagnostic automatique
- `k8s/deploy-manual.sh` : Déploiement interactif pas-à-pas
