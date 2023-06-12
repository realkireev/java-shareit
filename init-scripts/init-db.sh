#!/bin/bash

set -e

echo "Creating '$DB_NAME' database..."

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
    CREATE DATABASE $DB_NAME;
    GRANT ALL PRIVILEGES ON DATABASE $DB_NAME TO "$POSTGRES_USER";
EOSQL

echo "Database '$DB_NAME' created successfully!"