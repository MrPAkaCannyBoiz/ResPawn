#!/bin/bash

# convert_cert.sh
#
# This script converts Let's Encrypt PEM certificates to .p12 format.
#
# Usage: ./convert_cert.sh <domain_name> <password> <output_path>
#
# Parameters:
#   domain_name: The domain name for which the certificates are generated.
#   password: The password to protect the .p12 file.
#   output_path: The path where the .p12 file will be saved.
#
# Ensure you have OpenSSL installed to run this script.

# Function to display an error message and exit
function error_exit {
    echo "$1" >&2
    exit 1
}

# Check input parameters
if [ "$#" -ne 3 ]; then
    error_exit "Usage: $0 <domain_name> <password> <output_path>"
fi

DOMAIN_NAME="$1"
PASSWORD="$2"
OUTPUT_PATH="$3"

# Paths to the Let's Encrypt PEM certificate files
CERT_PATH="/etc/letsencrypt/live/$DOMAIN_NAME/fullchain.pem"
KEY_PATH="/etc/letsencrypt/live/$DOMAIN_NAME/privkey.pem"

# Check if the certificate files exist
if [ ! -f "$CERT_PATH" ]; then
    error_exit "Certificate file $CERT_PATH not found."
fi

if [ ! -f "$KEY_PATH" ]; then
    error_exit "Key file $KEY_PATH not found."
fi

# Convert PEM to .p12 format
openssl pkcs12 -export -out "$OUTPUT_PATH" -in "$CERT_PATH" -inkey "$KEY_PATH" -password pass:"$PASSWORD"

if [ $? -ne 0 ]; then
    error_exit "Failed to convert to .p12 format."
fi

echo "Successfully converted Let's Encrypt certificates to .p12 format and saved to $OUTPUT_PATH."