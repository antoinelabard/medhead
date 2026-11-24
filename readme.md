# MedHead

Plateforme MedHead : PoC d'intervention d'urgence.

## Démarrer la PoC

Prérequis : Docker avec Compose. Rien d'autre n'est nécessaire pour lancer la PoC.

```bash
docker compose --profile app up -d --build --wait
```

Ouvrir <https://localhost> et accepter le certificat auto-signé : la liste des spécialités NHS s'affiche.

- Le certificat TLS de développement est généré au premier démarrage par le service `edge-cert`, dans le volume `edge-certs` (jamais versionné). Pour le régénérer : `docker compose --profile app down -v`.
- Seul `edge` publie un port, 443 par défaut. Si ce port est pris ou interdit (Docker rootless), copier `.env.example` en `.env` et définir `MEDHEAD_EDGE_PORT=8443`, puis ouvrir <https://localhost:8443>.
- L'API est servie sous `/api/v1/…` ; Actuator n'est pas exposé ; les services n'ont aucune sortie hors de Docker.

Builder et tester hors conteneur (JDK 25, Node ≥ 22.22.3, `.nvmrc` : 24) :

```bash
./mvnw -B verify
cd frontend && npm ci && npm test -- --watch=false && npm run build
```

Le code serveur et le client Angular sont générés depuis `contracts/openapi/medhead-api.yaml` à chaque build (`./mvnw`, `npm test`, `npm run build`, `npm start`) ; ils ne sont pas versionnés.
