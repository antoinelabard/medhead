#!/bin/sh
# Génère le certificat TLS auto-signé de développement d'edge (jamais versionné).
# Exécuté par le service Compose `edge-cert` dans le volume `edge-certs` ; aucun outil requis sur l'hôte.
# Usage : generate-dev-cert.sh [répertoire]   (défaut : /certs)
set -eu

cert_dir="${1:-/certs}"
crt="${cert_dir}/dev.crt"
key="${cert_dir}/dev.key"

if [ -s "${crt}" ] && [ -s "${key}" ]; then
  echo "Certificat de développement déjà présent : ${crt}"
  exit 0
fi

mkdir -p "${cert_dir}"
openssl req -x509 -nodes -newkey rsa:2048 -sha256 -days 3650 \
  -keyout "${key}" -out "${crt}" \
  -subj "/CN=localhost" \
  -addext "subjectAltName=DNS:localhost,IP:127.0.0.1" \
  -addext "keyUsage=digitalSignature,keyEncipherment" \
  -addext "extendedKeyUsage=serverAuth" 2>/dev/null

# Lisible par l'utilisateur non root (101) du conteneur edge : clé de développement uniquement.
chmod 644 "${key}" "${crt}"
echo "Certificat de développement généré : ${crt}"
