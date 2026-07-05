#!/bin/bash
set -e

echo "Creating databases..."
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE DATABASE kyro_catalog;
    CREATE DATABASE kyro_order;
    CREATE DATABASE kyro_payment;
EOSQL

echo "Databases created successfully."
