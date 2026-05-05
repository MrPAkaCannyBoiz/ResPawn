#!/usr/bin/env bash
set -euo pipefail

# Usage: ./scripts/create-reseller.sh <name> <username> <password>
# Requires: python3 with bcrypt (`pip install bcrypt`) on VPS, running postgres_respawn container

NAME="${1:?Usage: $0 <name> <username> <password>}"
USERNAME="${2:?Usage: $0 <name> <username> <password>}"
PASSWORD="${3:?Usage: $0 <name> <username> <password>}"

# Hash using env var to avoid shell injection with special chars in password
HASH=$(PASS="$PASSWORD" python3 -c "
import bcrypt, os
print(bcrypt.hashpw(os.environ['PASS'].encode(), bcrypt.gensalt()).decode())
")

DB_USER="${DB_USERNAME:-respawn_user}"
DB_NAME="${POSTGRES_DB:-respawn}"

# Use psql variables to avoid SQL injection
docker exec -i postgres_respawn psql -U "$DB_USER" -d "$DB_NAME" \
  -v name="$NAME" -v uname="$USERNAME" -v pw="$HASH" \
  -c "INSERT INTO reseller(name, username, password) VALUES (:'name', :'uname', :'pw');"

echo "Reseller '${USERNAME}' created successfully."
