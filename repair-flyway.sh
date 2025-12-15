#!/bin/bash

# Script para reparar o histórico do Flyway usando Spring Boot
# Executa uma query SQL diretamente no banco

DATABASE_URL="${DATABASE_URL:postgresql://localhost:5432/auth_db_dev}"
DATABASE_USER="${DATABASE_USER:postgres}"
DATABASE_PASSWORD="${DATABASE_PASSWORD:postgres}"

# Extrai host e database da URL
HOST=$(echo $DATABASE_URL | cut -d'/' -f3 | cut -d':' -f1)
PORT=$(echo $DATABASE_URL | cut -d':' -f4 | cut -d'/' -f1)
if [ -z "$PORT" ]; then PORT="5432"; fi
DATABASE=$(echo $DATABASE_URL | cut -d'/' -f4)

echo "Conectando ao banco: $DATABASE em $HOST:$PORT"
echo ""
echo "Executando comando para limpar histórico do Flyway..."
echo ""

# Se tiver psql, usa
if command -v psql &> /dev/null; then
    export PGPASSWORD=$DATABASE_PASSWORD
    psql -h $HOST -p $PORT -U $DATABASE_USER -d $DATABASE -c "DELETE FROM flyway_schema_history WHERE version = 1;"
    psql -h $HOST -p $PORT -U $DATABASE_USER -d $DATABASE -c "SELECT * FROM flyway_schema_history;"
else
    echo "AVISO: psql não disponível. Será necessário fazer o reset manualmente."
    echo ""
    echo "Execute este comando no seu banco de dados PostgreSQL:"
    echo "  DELETE FROM flyway_schema_history WHERE version = 1;"
    echo ""
fi
