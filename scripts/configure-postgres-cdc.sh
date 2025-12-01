#!/bin/bash

echo "🔧 Configuring PostgreSQL for CDC..."

DB_HOST="localhost"
DB_PORT="5432"
DB_NAME="mydb"
DB_USER="user"
DB_PASSWORD="user"
CONTAINER_NAME="4a04425b3a8b28c60dcf7c836829b41907fa09a6a8c6e20dcd43699d55876cba"


SQL_COMMAND="
DO \$\$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname='debezium_user') THEN
        CREATE ROLE debezium_user WITH REPLICATION LOGIN PASSWORD 'debezium_password';
    END IF;
END
\$\$;

GRANT SELECT ON ALL TABLES IN SCHEMA public TO debezium_user;
GRANT USAGE ON SCHEMA public to debezium_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON TABLES TO debezium_user;

DROP PUBLICATION IF EXISTS dbz_publication;
CREATE PUBLICATION dbz_publication FOR ALL TABLES;


SELECT * FROM pg_publication;
SELECT slot_name, plugin,slot_type,database FROM pg_replication_slots;
"

# PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -c "$SQL_COMMAND"
docker exec -i "$CONTAINER_NAME" psql -U "$DB_USER" -d "$DB_NAME" -v ON_ERROR_STOP=1 -c "$SQL_COMMAND"



echo "✅ PostgreSQL CDC configuration completed!"
echo "📌 Make sure postgresql.conf has the following settings:"
echo "   wal_level = logical"
echo "   max_replication_slots = 10"
echo "   max_wal_senders = 10"