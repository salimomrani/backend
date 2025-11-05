#!/bin/bash

# Script de diagnostic pour le déploiement Kubernetes du backend
# Usage: ./k8s/debug.sh

set -e

NAMESPACE="backend"

echo "=================================================="
echo "🔍 Diagnostic du déploiement backend sur Kubernetes"
echo "=================================================="
echo ""

# Couleurs
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 1. État des pods
echo -e "${YELLOW}1. État des pods${NC}"
echo "---"
kubectl get pods -n $NAMESPACE
echo ""

# 2. État du déploiement
echo -e "${YELLOW}2. État du déploiement${NC}"
echo "---"
kubectl get deployment backend -n $NAMESPACE
echo ""

# 3. Récupérer le nom du pod (premier pod trouvé)
POD_NAME=$(kubectl get pods -n $NAMESPACE -l app=backend -o jsonpath='{.items[0].metadata.name}' 2>/dev/null || echo "")

if [ -z "$POD_NAME" ]; then
    echo -e "${RED}❌ Aucun pod trouvé dans le namespace $NAMESPACE${NC}"
    echo ""
    echo -e "${YELLOW}Vérifiez les événements du déploiement :${NC}"
    kubectl describe deployment backend -n $NAMESPACE
    exit 1
fi

echo -e "${GREEN}✓ Pod trouvé: $POD_NAME${NC}"
echo ""

# 4. Détails du pod
echo -e "${YELLOW}3. Détails du pod $POD_NAME${NC}"
echo "---"
kubectl describe pod $POD_NAME -n $NAMESPACE
echo ""

# 5. Logs du pod
echo -e "${YELLOW}4. Logs du pod $POD_NAME (dernières 50 lignes)${NC}"
echo "---"
kubectl logs $POD_NAME -n $NAMESPACE --tail=50 || echo -e "${RED}❌ Impossible de récupérer les logs${NC}"
echo ""

# 6. Événements récents
echo -e "${YELLOW}5. Événements récents (derniers 20)${NC}"
echo "---"
kubectl get events -n $NAMESPACE --sort-by='.lastTimestamp' | tail -20
echo ""

# 7. Vérifier les secrets
echo -e "${YELLOW}6. Vérification des secrets${NC}"
echo "---"
if kubectl get secret backend-secrets -n $NAMESPACE &>/dev/null; then
    echo -e "${GREEN}✓ Secret backend-secrets existe${NC}"
    kubectl get secret backend-secrets -n $NAMESPACE
else
    echo -e "${RED}❌ Secret backend-secrets introuvable${NC}"
    echo "Créez-le avec :"
    echo "kubectl create secret generic backend-secrets -n $NAMESPACE \\"
    echo "  --from-literal=DB_URL='jdbc:h2:mem:backenddb' \\"
    echo "  --from-literal=DB_USERNAME='sa' \\"
    echo "  --from-literal=DB_PASSWORD='' \\"
    echo "  --from-literal=DB_DRIVER='org.h2.Driver' \\"
    echo "  --from-literal=JPA_DIALECT='org.hibernate.dialect.H2Dialect'"
fi
echo ""

# 8. Vérifier le ConfigMap
echo -e "${YELLOW}7. Vérification du ConfigMap${NC}"
echo "---"
if kubectl get configmap backend-config -n $NAMESPACE &>/dev/null; then
    echo -e "${GREEN}✓ ConfigMap backend-config existe${NC}"
    kubectl get configmap backend-config -n $NAMESPACE
else
    echo -e "${RED}❌ ConfigMap backend-config introuvable${NC}"
fi
echo ""

# 9. Vérifier l'Ingress
echo -e "${YELLOW}8. État de l'Ingress${NC}"
echo "---"
kubectl get ingress -n $NAMESPACE
echo ""
kubectl describe ingress backend -n $NAMESPACE | grep -A 5 "Events:" || echo "Pas d'événements"
echo ""

# 10. Vérifier le Service
echo -e "${YELLOW}9. État du Service${NC}"
echo "---"
kubectl get service backend -n $NAMESPACE
echo ""

# 11. Vérifier le HPA
echo -e "${YELLOW}10. État du HPA${NC}"
echo "---"
kubectl get hpa -n $NAMESPACE
echo ""

# 12. Test des health endpoints (si le pod est running)
POD_STATUS=$(kubectl get pod $POD_NAME -n $NAMESPACE -o jsonpath='{.status.phase}')
if [ "$POD_STATUS" == "Running" ]; then
    echo -e "${YELLOW}11. Test des endpoints Actuator${NC}"
    echo "---"

    echo "Test de /actuator/health :"
    kubectl exec -it $POD_NAME -n $NAMESPACE -- wget -qO- http://localhost:8080/actuator/health 2>/dev/null || echo -e "${RED}❌ Échec${NC}"
    echo ""

    echo "Test de /actuator/health/liveness :"
    kubectl exec -it $POD_NAME -n $NAMESPACE -- wget -qO- http://localhost:8080/actuator/health/liveness 2>/dev/null || echo -e "${RED}❌ Échec${NC}"
    echo ""

    echo "Test de /actuator/health/readiness :"
    kubectl exec -it $POD_NAME -n $NAMESPACE -- wget -qO- http://localhost:8080/actuator/health/readiness 2>/dev/null || echo -e "${RED}❌ Échec${NC}"
    echo ""
else
    echo -e "${YELLOW}11. Test des endpoints Actuator${NC}"
    echo "---"
    echo -e "${RED}❌ Pod non Running (statut: $POD_STATUS), impossible de tester les endpoints${NC}"
    echo ""
fi

# Résumé
echo "=================================================="
echo -e "${GREEN}✓ Diagnostic terminé${NC}"
echo "=================================================="
echo ""
echo "Pour plus d'informations :"
echo "  - Logs en temps réel: kubectl logs -f $POD_NAME -n $NAMESPACE"
echo "  - Shell interactif: kubectl exec -it $POD_NAME -n $NAMESPACE -- /bin/sh"
echo "  - Redémarrer: kubectl rollout restart deployment/backend -n $NAMESPACE"
echo ""
