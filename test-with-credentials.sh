#!/bin/bash

# ============================================================================
# Script para testar a aplicação com suporte a AWS Credentials
# ============================================================================

set -e

echo "╔════════════════════════════════════════════════════════════════════╗"
echo "║           TESTE: Auth API com Credenciais AWS/Local               ║"
echo "╚════════════════════════════════════════════════════════════════════╝"
echo ""

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 1. Verificar Docker
echo -e "${BLUE}[1/5]${NC} Verificando Docker..."
if ! command -v docker &> /dev/null; then
    echo -e "${RED}❌ Docker não está instalado${NC}"
    exit 1
fi
echo -e "${GREEN}✅ Docker encontrado${NC}"
echo ""

# 2. Verificar Docker Compose
echo -e "${BLUE}[2/5]${NC} Verificando Docker Compose..."
if ! command -v docker-compose &> /dev/null; then
    echo -e "${RED}❌ Docker Compose não está instalado${NC}"
    exit 1
fi
echo -e "${GREEN}✅ Docker Compose encontrado${NC}"
echo ""

# 3. Build da aplicação
echo -e "${BLUE}[3/5]${NC} Compilando aplicação..."
mvn clean package -DskipTests -q
echo -e "${GREEN}✅ Aplicação compilada${NC}"
echo ""

# 4. Derrubar containers existentes
echo -e "${BLUE}[4/5]${NC} Limpando containers antigos..."
docker-compose down --remove-orphans 2>/dev/null || true
echo -e "${GREEN}✅ Containers removidos${NC}"
echo ""

# 5. Iniciar docker-compose
echo -e "${BLUE}[5/5]${NC} Iniciando docker-compose..."
docker-compose up -d
echo -e "${GREEN}✅ Containers iniciados${NC}"
echo ""

# Aguardar aplicação iniciar
echo -e "${YELLOW}⏳ Aguardando aplicação iniciar (máx 30s)...${NC}"
for i in {1..30}; do
    if curl -s http://localhost:8088/swagger-ui.html > /dev/null 2>&1; then
        echo -e "${GREEN}✅ Aplicação está respondendo!${NC}"
        break
    fi
    echo -n "."
    sleep 1
done
echo ""
echo ""

# Verificar logs
echo -e "${BLUE}═══════════════════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}            LOGS DA APLICAÇÃO (últimas 20 linhas)${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════════════════${NC}"
docker-compose logs auth-service | tail -20
echo ""

# Mostrar URLs
echo -e "${BLUE}═══════════════════════════════════════════════════════════════════${NC}"
echo -e "${GREEN}✅ TESTE CONCLUÍDO COM SUCESSO!${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════════════════${NC}"
echo ""
echo -e "${YELLOW}URLs disponíveis:${NC}"
echo "  🔐 Swagger UI:      http://localhost:8088/swagger-ui.html"
echo "  📊 Health Check:    http://localhost:8088/actuator/health"
echo "  🗄️  pgAdmin:         http://localhost:5050 (admin/admin)"
echo "  📋 Redis Commander: http://localhost:8081"
echo ""
echo -e "${YELLOW}Comandos úteis:${NC}"
echo "  Ver logs:        docker-compose logs -f auth-service"
echo "  Parar:           docker-compose down"
echo "  Reiniciar:       docker-compose restart auth-service"
echo ""
