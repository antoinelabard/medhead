---
title: 'La liste des spécialités NHS s''affiche dans la page via edge en HTTPS'
type: feature
ticket: '1'
created: '2026-11-24'
status: ready-for-dev
baseline_revision: 'f55999234b73f7bc078721a06522bff3ec8d8aa4'
route: full
route_source: auto
risk: medium
review: ''
review_source: ''
lenses_ran: []
review_loop_iteration: 0
context:
  - '{project-root}/_bmad-output/initiative-meadhead-plateforme/architecture-meadhead-plateforme/architecture-meadhead-plateforme.md'
  - '{project-root}/_bmad-output/initiative-meadhead-poc/tickets/epic-socle-plateforme/epic-socle-plateforme.md'
---

<frozen-after-approval reason="human-owned intent — do not modify unless human renegotiates">

## Intent

**Problem:** Le dépôt ne contient aucun code ; rien ne prouve encore la chaîne contrat OpenAPI → code généré → images → point d'entrée TLS → navigateur sur laquelle tous les epics vont s'appuyer (issue #2).

**Approach:** Monodépôt Maven (wrapper) et Angular 22 ; `recommendation-service` hexagonal sert `GET /api/v1/specialties` depuis `contracts/reference/specialties.json` via une interface générée ; la page Angular affiche la liste via un client généré ; `edge` nginx termine TLS et route `/` et `/api/` ; tout démarre par `docker compose --profile app up`.

## Boundaries & Constraints

**Always:** contrat d'abord (`contracts/openapi/medhead-api.yaml` source de vérité, code généré au build, jamais versionné) ; paquets `domain` / `application` / `adapters/in` / `adapters/out`, domaine sans framework ; Actuator et sondes liveness/readiness sur le port 8081, jamais routé par `edge` ; journaux ECS sur la sortie standard ; images multi-stage non root, configurées par variables `MEDHEAD_<SERVICE>_<CLÉ>` ; `edge` seul conteneur publiant un port (443), `/api/` routé sans réécriture ; image `frontend` = nginx non root statique en HTTP 8080 ; certificat de développement généré par script, non versionné ; versions exactes épinglées (Spring Boot 4.1 dernier correctif, openapi-generator ≥ 7.16 avec `useSpringBoot4` et `useJackson3`, Angular 22, nginx 1.30.x, Temurin 25) et, en propriétés du POM parent pour les stories suivantes, Cucumber-JVM, Testcontainers et Flyway ; Node ≥ 22.22.3 (exigence d'Angular CLI 22.3), `.nvmrc` à 24 ; endpoint public (décision d'epic du 2026-11-24) ; codes de spécialité stables en kebab-case anglais, libellés et groupes en français tirés de `specs/Données_de_référence_sur_les_spécialités_NHS.md`.

**Never:** Kafka, PostgreSQL, Keycloak, OSRM, `hospital-registry`, workflow CI, Prometheus (autres stories) ; étiquette `latest` ; DTO écrits à la main à la place du code généré ; secret ou certificat versionné ; visuel élaboré.

## I/O & Edge-Case Matrix

| Scenario | Input / State | Expected Output / Behavior | Error Handling |
|----------|--------------|---------------------------|----------------|
| Liste via API | `GET https://localhost/api/v1/specialties` | 200, tableau `[{code, group, label}]` de toutes les spécialités NHS, ordre du fichier | No error expected |
| Page | ouverture de `https://localhost/` | liste des spécialités regroupées par groupe | No error expected |
| API indisponible | `recommendation-service` arrêté, page ouverte | message « Impossible de charger les spécialités » | pas de page blanche ni d'erreur non gérée |
| Actuator depuis l'extérieur | `GET https://localhost/actuator/health` | 404 | non routé par `edge` |
| HTTP clair | `http://localhost:80` | connexion refusée | port non publié |

</frozen-after-approval>

## Code Map

- Dépôt vierge hors `_bmad*`, `specs/`, `.claude/`, `.agents/` : rien à réutiliser, rien à modifier hors des fichiers listés.
- `specs/Données_de_référence_sur_les_spécialités_NHS.md` -- table groupe / spécialité, source de `specialties.json`.
- Architecture (contexte) -- AD-2, AD-11, AD-12, AD-14, AD-15, Consistency Conventions, Structural Seed (arborescence).
- Poste : JDK 25, Docker 29, Compose 5.7, OpenSSL 3 ; pas de Maven (d'où le wrapper) ; Node 22.22.2 trop ancien pour Angular CLI 22.3.

## Tasks & Acceptance

**Execution:**
- [ ] `pom.xml`, `mvnw`, `.mvn/wrapper/maven-wrapper.properties` -- POM parent Spring Boot 4.1.x, Java 25, modules, versions de plugins et propriétés d'outillage épinglées, wrapper Maven -- build reproductible sans Maven installé.
- [ ] `contracts/openapi/medhead-api.yaml` -- OpenAPI 3.1 : `GET /api/v1/specialties`, schémas `Specialty` et Problem Details -- source de vérité.
- [ ] `contracts/reference/specialties.json` -- toutes les lignes de la table NHS en `{code, group, label}` -- référentiel partagé.
- [ ] `services/recommendation-service/` -- module Maven, génération de l'interface serveur, `domain/Specialty`, port `SpecialtyCatalog` et cas d'usage `ListSpecialties`, adaptateur entrant REST implémentant l'interface générée, adaptateur sortant lisant le JSON (chemin par variable d'environnement), `application.yml` (8081, ECS), tests unitaires du domaine et de l'application, test web contre le contrat, test vérifiant codes uniques et nombre d'entrées -- tranche back.
- [ ] `services/recommendation-service/Dockerfile` -- multi-stage, `eclipse-temurin:25-jre`, non root, référentiel copié dans l'image -- image du service.
- [ ] `frontend/` -- application Angular 22 autonome, client `typescript-angular` généré par script npm, composant listant les spécialités par groupe avec message d'erreur, test Vitest du composant (succès et erreur), `.nvmrc` -- tranche front.
- [ ] `frontend/Dockerfile`, `frontend/nginx.conf` -- build Node 24, service statique par nginx non root sur 8080 avec repli vers `index.html` -- image front.
- [ ] `infra/edge/nginx.conf`, `infra/edge/generate-dev-cert.sh` -- TLS 443, `/` vers `frontend:8080`, `/api/` vers `recommendation-service:8080` sans réécriture, rien d'autre ; certificat auto-signé avec SAN `localhost` -- point d'entrée.
- [ ] `compose.yaml`, `.env.example`, `.gitignore` -- services `edge`, `frontend`, `recommendation-service` en profil `app`, réseaux `edge` et `backend`, healthchecks sur readiness, `depends_on: service_healthy` ; ignorer `target/`, `node_modules/`, `dist/`, code généré, certificats, `.env`.
- [ ] `readme.md` -- section courte « Démarrer la PoC » (certificat, Compose, URL) ; le readme complet est la story 1.7.

**Acceptance Criteria:**
- Given le dépôt fraîchement cloné, when on lance `./mvnw -B verify` puis les tests du front, then tout passe sans Maven ni code généré versionné.
- Given le contrat modifié, when on rebuild, then le code serveur et le client Angular changent sans édition manuelle.
- Given la PoC démarrée, when on liste les conteneurs, then seul `edge` publie un port, et c'est 443.

## Implementation Notes

## Plan Change Log

## Review Triage Log

## Design Notes

Dépendances hexagonales attendues :

```text
adapters/in/web/SpecialtiesController implements <SpecialtiesApi généré>
  -> application/ListSpecialties (cas d'usage) -> application/port/SpecialtyCatalog
adapters/out/reference/JsonSpecialtyCatalog implements SpecialtyCatalog
domain/Specialty(code, group, label)   // record, sans annotation de framework
```

Le DTO généré est converti depuis `domain/Specialty` dans l'adaptateur entrant, jamais exposé au domaine.

## Verification

**Commands:**
- `./mvnw -B verify` -- expected: BUILD SUCCESS, tests verts
- `cd frontend && npm ci && npx ng test --watch=false && npx ng build` -- expected: tests verts, build OK
- `./infra/edge/generate-dev-cert.sh && docker compose --profile app up -d --build --wait` -- expected: conteneurs healthy
- `curl -sk https://localhost/api/v1/specialties | jq length` -- expected: égal au nombre d'entrées de `specialties.json`
- `curl -sk -o /dev/null -w '%{http_code}' https://localhost/actuator/health` -- expected: 404
- `docker compose ps --format '{{.Name}} {{.Ports}}'` -- expected: seul `edge` avec `0.0.0.0:443`

**Manual checks (if no CLI):**
- Ouvrir `https://localhost` après avoir accepté le certificat : la liste groupée s'affiche ; arrêter `recommendation-service` et recharger : le message d'erreur s'affiche.
