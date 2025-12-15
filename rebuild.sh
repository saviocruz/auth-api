#!/bin/bash

# Script para limpar e reconstruir o projeto
echo "==============================================="
echo "Limpando o projeto..."
echo "==============================================="

cd /home/savio/novo-1

# Limpar build anterior
mvn clean -q

echo ""
echo "==============================================="
echo "Reconstruindo o projeto..."
echo "==============================================="

# Rebuildar
mvn package -q -DskipTests

echo ""
echo "Build concluído!"
