#!/bin/bash

# Script de déploiement manuel pas-à-pas pour diagnostic
# Usage: ./k8s/deploy-manual.sh

set -e

NAMESPACE="backend"
IMAGE_TAG="${1:-iconsultingdev/blog-backend:latest}"

echo "=================================================="
echo "🚀 Déploiement manuel du backend sur Kubernetes"
echo "=================================================="
echo ""
echo "Image à déployer: $IMAGE_TAG"
echo "Namespace: $NAMESPACE"
echo ""

# Couleurs
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Fonction pour attendre une entrée utilisateur
wait_for_user() {
    echo ""
    read -p "Appuyez sur Entrée pour continuer..."
    echo ""
}

# 1. Créer le namespace
echo -e "${BLUE}Étape 1: Création du namespace${NC}"
kubectl apply -f k8s/namespace.yaml
kubectl get namespace $NAMESPACE
wait_for_user

# 2. Créer/vérifier les secrets
echo -e "${BLUE}Étape 2: Vérification des secrets${NC}"
if kubectl get secret backend-secrets -n $NAMESPACE &>/dev/null; then
    echo -e "${GREEN}✓ Secret backend-secrets existe${NC}"
    kubectl get secret backend-secrets -n $NAMESPACE
else
    echo -e "${YELLOW}⚠ Secret backend-secrets n'existe pas${NC}"
    echo "Création du secret avec H2 en mémoire..."
    kubectl create secret generic backend-secrets -n $NAMESPACE \
        --from-literal=DB_URL='jdbc:h2:mem:backenddb' \
        --from-literal=DB_USERNAME='sa' \
        --from-literal=DB_PASSWORD='' \
        --from-literal=DB_DRIVER='org.h2.Driver' \
        --from-literal=JPA_DIALECT='org.hibernate.dialect.H2Dialect'
    echo -e "${GREEN}✓ Secret créé${NC}"
fi
wait_for_user

# 3. Créer le ConfigMap
echo -e "${BLUE}Étape 3: Application du ConfigMap${NC}"
kubectl apply -f k8s/configmap.yaml
kubectl get configmap backend-config -n $NAMESPACE
wait_for_user

# 4. Créer le Service
echo -e "${BLUE}Étape 4: Application du Service${NC}"
kubectl apply -f k8s/service.yaml
kubectl get service backend -n $NAMESPACE
wait_for_user

# 5. Demander quelle version du deployment utiliser
echo -e "${BLUE}Étape 5: Choix du manifeste de déploiement${NC}"
echo "1) deployment.yaml (production - avec security context)"
echo "2) deployment-simple.yaml (debug - health probes simplifiées, 1 replica)"
read -p "Choisissez [1-2, défaut: 2]: " DEPLOY_CHOICE
DEPLOY_CHOICE=${DEPLOY_CHOICE:-2}

if [ "$DEPLOY_CHOICE" == "1" ]; then
    DEPLOY_FILE="k8s/deployment.yaml"
    echo -e "${GREEN}Utilisation de deployment.yaml${NC}"
else
    DEPLOY_FILE="k8s/deployment-simple.yaml"
    echo -e "${YELLOW}Utilisation de deployment-simple.yaml (mode debug)${NC}"
fi

kubectl apply -f $DEPLOY_FILE
kubectl set image deployment/backend backend=$IMAGE_TAG -n $NAMESPACE

echo "Attente du démarrage des pods (60 secondes)..."
sleep 60

echo -e "${BLUE}État des pods :${NC}"
kubectl get pods -n $NAMESPACE
wait_for_user

# 6. Vérifier les logs
echo -e "${BLUE}Étape 6: Vérification des logs${NC}"
POD_NAME=$(kubectl get pods -n $NAMESPACE -l app=backend -o jsonpath='{.items[0].metadata.name}' 2>/dev/null || echo "")

if [ -n "$POD_NAME" ]; then
    echo "Logs du pod $POD_NAME :"
    kubectl logs $POD_NAME -n $NAMESPACE --tail=30
else
    echo -e "${RED}❌ Aucun pod trouvé${NC}"
fi
wait_for_user

# 7. Tester les endpoints Actuator
echo -e "${BLUE}Étape 7: Test des endpoints Actuator${NC}"
if [ -n "$POD_NAME" ]; then
    POD_STATUS=$(kubectl get pod $POD_NAME -n $NAMESPACE -o jsonpath='{.status.phase}')
    if [ "$POD_STATUS" == "Running" ]; then
        echo "Test de /actuator/health :"
        kubectl exec $POD_NAME -n $NAMESPACE -- wget -qO- http://localhost:8080/actuator/health 2>/dev/null || echo -e "${RED}❌ Échec${NC}"

        echo ""
        echo "Test de /actuator/health/liveness :"
        kubectl exec $POD_NAME -n $NAMESPACE -- wget -qO- http://localhost:8080/actuator/health/liveness 2>/dev/null || echo -e "${RED}❌ Échec${NC}"

        echo ""
        echo "Test de /actuator/health/readiness :"
        kubectl exec $POD_NAME -n $NAMESPACE -- wget -qO- http://localhost:8080/actuator/health/readiness 2>/dev/null || echo -e "${RED}❌ Échec${NC}"
    else
        echo -e "${YELLOW}⚠ Pod non Running (statut: $POD_STATUS)${NC}"
    fi
else
    echo -e "${RED}❌ Aucun pod disponible pour les tests${NC}"
fi
wait_for_user

# 8. Créer l'Ingress
echo -e "${BLUE}Étape 8: Application de l'Ingress${NC}"
read -p "Voulez-vous créer l'Ingress ? (y/n, défaut: n): " CREATE_INGRESS
CREATE_INGRESS=${CREATE_INGRESS:-n}

if [ "$CREATE_INGRESS" == "y" ]; then
    kubectl apply -f k8s/ingress.yaml
    kubectl get ingress -n $NAMESPACE
else
    echo -e "${YELLOW}Ingress non créé${NC}"
fi
wait_for_user

# 9. Créer le HPA
echo -e "${BLUE}Étape 9: Application du HPA${NC}"
read -p "Voulez-vous créer le HPA ? (y/n, défaut: n): " CREATE_HPA
CREATE_HPA=${CREATE_HPA:-n}

if [ "$CREATE_HPA" == "y" ]; then
    kubectl apply -f k8s/hpa.yaml
    kubectl get hpa -n $NAMESPACE
else
    echo -e "${YELLOW}HPA non créé${NC}"
fi

# Résumé
echo ""
echo "=================================================="
echo -e "${GREEN}✓ Déploiement terminé${NC}"
echo "=================================================="
echo ""
echo "Commandes utiles :"
echo "  - Voir les pods: kubectl get pods -n $NAMESPACE"
echo "  - Voir les logs: kubectl logs -f <pod-name> -n $NAMESPACE"
echo "  - Décrire pod: kubectl describe pod <pod-name> -n $NAMESPACE"
echo "  - Redémarrer: kubectl rollout restart deployment/backend -n $NAMESPACE"
echo "  - Supprimer: kubectl delete -f k8s/"
echo ""
