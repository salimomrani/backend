# 🔴 TROUBLESHOOTING : Déploiement bloqué

## Problème actuel

Le déploiement GitLab CI/CD échoue avec :
```
Waiting for deployment "backend" rollout to finish: 1 out of 2 new replicas have been updated...
error: timed out waiting for the condition
```

Cela signifie qu'**un pod démarre mais ne devient jamais "Ready"**.

---

## 🔍 ÉTAPE 1 : Diagnostic immédiat

Connectez-vous à votre cluster Kubernetes et exécutez :

### 1. Voir l'état des pods
```bash
kubectl get pods -n backend -o wide
```

**Attendu :** Voir l'état des pods (Pending, Running, CrashLoopBackOff, etc.)

### 2. Identifier le pod problématique
```bash
# Récupérer le nom du pod qui ne démarre pas
POD_NAME=$(kubectl get pods -n backend -l app=backend --field-selector=status.phase!=Running -o jsonpath='{.items[0].metadata.name}')
echo "Pod problématique: $POD_NAME"
```

### 3. Voir les détails du pod
```bash
kubectl describe pod $POD_NAME -n backend
```

**Cherchez :**
- `Events:` à la fin (erreurs de pull d'image, problèmes de secret, etc.)
- `State:` du container (Waiting, CrashLoopBackOff, etc.)

### 4. Voir les logs du pod
```bash
kubectl logs $POD_NAME -n backend
```

**Erreurs communes :**
- `Cannot find main class` → Image Docker défectueuse
- `Failed to load application context` → Problème de configuration Spring
- `Connection refused` → Problème de base de données
- Aucun log → Le pod ne démarre même pas (problème d'image ou de permissions)

### 5. Vérifier les secrets
```bash
kubectl get secret backend-secrets -n backend
```

**Si absent :**
```bash
kubectl create secret generic backend-secrets -n backend \
  --from-literal=DB_URL='jdbc:h2:mem:backenddb' \
  --from-literal=DB_USERNAME='sa' \
  --from-literal=DB_PASSWORD='' \
  --from-literal=DB_DRIVER='org.h2.Driver' \
  --from-literal=JPA_DIALECT='org.hibernate.dialect.H2Dialect'
```

---

## 🚨 PROBLÈME PROBABLE : Image Docker périmée

**Le code a été mis à jour (avec config Actuator), mais l'image Docker n'a pas été reconstruite !**

### Solution : Reconstruire et pusher l'image

#### Option A : Via GitLab CI/CD (automatique)

Le stage `docker` doit s'exécuter AVANT `deploy_production`. Vérifiez dans GitLab CI/CD que :
1. Le job `build` est passé ✅
2. Le job `docker` est passé ✅ (construit et push l'image)
3. Ensuite lancez `deploy_production`

#### Option B : Build manuel local

```bash
# 1. Récupérer les dernières modifications
git pull origin main

# 2. Builder le JAR
./mvnw clean install

# 3. Builder l'image Docker
docker build -t iconsultingdev/blog-backend:latest .
docker tag iconsultingdev/blog-backend:latest iconsultingdev/blog-backend:main-$(git rev-parse --short HEAD)

# 4. Pusher sur Docker Hub
docker login
docker push iconsultingdev/blog-backend:latest
docker push iconsultingdev/blog-backend:main-$(git rev-parse --short HEAD)

# 5. Redémarrer le déploiement
kubectl rollout restart deployment/backend -n backend
kubectl rollout status deployment/backend -n backend --timeout=10m
```

---

## 🔧 FIX TEMPORAIRE : Désactiver les health probes

Si vous voulez débloquer rapidement, désactivez temporairement les health probes :

```bash
# Éditer le déploiement
kubectl edit deployment backend -n backend
```

Commentez ou supprimez les sections `startupProbe`, `readinessProbe`, `livenessProbe`, puis sauvegardez.

**⚠️ Attention :** Cette solution est temporaire. Les probes sont essentielles en production !

---

## 🐛 AUTRES CAUSES POSSIBLES

### Problème 1 : Permissions (runAsNonRoot)
**Symptôme :** Pods en `CreateContainerConfigError`

**Solution :**
```bash
kubectl edit deployment backend -n backend
```

Modifiez :
```yaml
securityContext:
  runAsNonRoot: false  # Temporairement
```

### Problème 2 : Ressources insuffisantes
**Symptôme :** Pods en `Pending` avec événement "Insufficient cpu/memory"

**Solution :**
```bash
kubectl edit deployment backend -n backend
```

Réduisez les ressources :
```yaml
resources:
  requests:
    cpu: "250m"
    memory: "512Mi"
  limits:
    cpu: "500m"
    memory: "768Mi"
```

### Problème 3 : Anciens pods en CrashLoop
**Symptôme :** Anciens pods bloqués empêchent le déploiement

**Solution :**
```bash
# Supprimer tous les pods (ils seront recréés)
kubectl delete pods -n backend -l app=backend

# Ou forcer le rollout
kubectl rollout restart deployment/backend -n backend
```

---

## ✅ VÉRIFICATION POST-DÉPLOIEMENT

Une fois déployé avec succès :

```bash
# 1. Vérifier que tous les pods sont Running et Ready
kubectl get pods -n backend

# 2. Tester les endpoints Actuator
POD_NAME=$(kubectl get pod -n backend -l app=backend -o jsonpath='{.items[0].metadata.name}')
kubectl exec -it $POD_NAME -n backend -- wget -qO- http://localhost:8080/actuator/health

# 3. Tester l'Ingress (si configuré avec certificat SSL)
curl https://api.kubevpro.i-consulting.shop/actuator/health

# 4. Tester l'API
curl https://api.kubevpro.i-consulting.shop/api/users
```

---

## 📞 SUPPORT

Si le problème persiste, exécutez le script de diagnostic :

```bash
./k8s/debug.sh
```

Et partagez la sortie pour analyse détaillée.

---

## 🎯 CHECKLIST DE DÉPLOIEMENT

Avant chaque déploiement, vérifiez :

- [ ] Le secret `backend-secrets` existe
- [ ] Le ConfigMap `backend-config` est appliqué
- [ ] L'image Docker a été construite avec les dernières modifications
- [ ] Le certificat SSL ACM est configuré (si HTTPS)
- [ ] Les ressources (CPU/Memory) sont suffisantes dans le cluster
- [ ] Le cluster Kubernetes est accessible
