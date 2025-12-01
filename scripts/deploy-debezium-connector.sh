#!/bin/bash

echo "🚀 Deploying Debezium PostgreSQL Connector..."

until curl -f http://localhost:8083/ > /dev/null 2>&1; do
    echo "⏳ Waiting for Kafka Connect to be ready..."
    sleep 5
done

curl -X DELETE http://localhost:8083/connectors/postgres-product-connector 2>/dev/null


curl -X POST http://localhost:8083/connectors -H "Content-Type: application/json" -d @config/debezium-postgres-connector.json

echo ""
echo "✅ Connector deployed!"
echo "📊 Check status:"
echo "   curl http://localhost:8083/connectors/postgres-product-connector/status"
echo ""
echo "🌐 Kafka UI: http://localhost:8090"