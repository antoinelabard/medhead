---
type: epic
title: "Socle de la PoC livré en conteneurs par un pipeline GitHub"
parent: initiative-meadhead-poc
covers: [R4, R9, R10, R12]
sources:
  - _bmad-output/initiative-meadhead-poc/initiative-meadhead-poc.md
companions:
  - _bmad-output/initiative-meadhead-plateforme/architecture-meadhead-plateforme/architecture-meadhead-plateforme.md
after: []
risk: medium
---

# Socle de la PoC livré en conteneurs par un pipeline GitHub

## Description

Ouvre l'initiative avec la base de plateforme : monodépôt, squelettes hexagonaux de `recommendation-service` et `hospital-registry`, application Angular servie en statique, point d'entrée `edge` en TLS, Kafka avec ses topics déclarés, démarrage de l'ensemble en une commande Compose, pipeline GitHub Actions sur chaque push et PR, publication des images dans GHCR, protections de branches Gitflow et première version du readme. Le parcours traceur — la liste des spécialités NHS affichée de bout en bout — prouve la chaîne contrat OpenAPI → code généré → images → proxy → navigateur. Les epics suivants s'y branchent ; le socle de production le prolongera.

## Outcome

Les développeurs de la PoC disposent dès le premier jour d'un dépôt qui se builde, se teste et se livre en conteneurs de façon reproductible, avec un premier parcours réel visible dans le navigateur.

## Requirements

- R1 (parent R9): un push ou une PR sur toute branche déclenche un pipeline GitHub Actions en étapes distinctes (tests, build, images) dont les rapports de tests sont consultables.
- R2 (parent R9, R12): sur `develop` et `main`, l'image de chaque service est publiée dans GHCR, étiquetée version et `sha-<court>`.
- R3 (parent R10): le readme décrit build et exécution, tests, pipeline et workflow Gitflow de façon réutilisable.
- R4 (parent R12, R4): depuis la page servie par `edge` en HTTPS, la liste des spécialités NHS s'affiche, obtenue par un client généré depuis le contrat OpenAPI auprès de `recommendation-service`.
- R5 (parent R12): une commande Compose démarre `edge`, `frontend`, `recommendation-service`, `hospital-registry` et Kafka avec ses topics déclarés ; seul `edge` est joignable depuis l'hôte.
- R6 (parent R12): chaque service Java suit la structure hexagonale, expose ses sondes et métriques sur 8081 et journalise en ECS sur la sortie standard.
- R7 (parent R9): `main` et `develop` n'acceptent de changement que par PR avec CI verte.

## Constraints

- Architecture AD-2 (hexagonal), AD-3 (topics déclarés dans `infra/kafka/topics.yaml`, créés par `kafka-init`), AD-11 (`edge`, image `frontend` statique, Actuator sur 8081), AD-12 (contrat d'abord), AD-14 (images, configuration, secrets, GHCR sans déploiement), AD-15 (journaux ECS), AD-17 (rapports de tests en CI).
- Stack et conventions de l'architecture ; les versions d'outillage non encore épinglées (Cucumber-JVM, Testcontainers, Playwright, nginx, Flyway) le sont ici, après vérification des pages de releases.

## Done when

- `docker compose --profile app up` démarre le socle et la page affiche la liste des spécialités via `https://localhost`.
- Un push et une PR déclenchent le pipeline ; ses rapports de tests sont consultables dans GitHub.
- Un merge sur `develop` publie les images dans GHCR avec leurs étiquettes.
- `main` et `develop` refusent un push direct et une PR à CI rouge.
- Le readme couvre build, exécution, tests, pipeline et Gitflow.

## Boundaries

- N'implémente aucune règle de recommandation, ni PostgreSQL, Keycloak ou OSRM (epic-recommandation-hopital).
- R4 de l'initiative : cet epic livre l'affichage de la liste des spécialités ; la validation des spécialités dans une recommandation appartient à epic-recommandation-hopital.
- Prometheus, Grafana et test de charge : epic-tenue-en-charge.
- Ne déploie rien en production ni sur un cloud.

## References

- parent — _bmad-output/initiative-meadhead-poc/initiative-meadhead-poc.md, Requirements R4, R9, R10, R12
- architecture — _bmad-output/initiative-meadhead-plateforme/architecture-meadhead-plateforme/architecture-meadhead-plateforme.md, AD-2, AD-3, AD-11, AD-12, AD-14, AD-15, AD-17, Consistency Conventions, Stack, Structural Seed

## Notes

- Decision: 2026-11-24 — le socle démarre les deux services, `frontend`, `edge`, Kafka avec `kafka-init` et les 5 topics ; PostgreSQL, Keycloak et OSRM arrivent avec epic-recommandation-hopital.
- Decision: 2026-11-24 — parcours traceur `GET /api/v1/specialties` de bout en bout, endpoint public (données de référence non sensibles).
- Decision: 2026-11-24 — protections de branches : story faite par l'utilisateur, préparée avec `gh`.
- Decision: 2026-11-24 — hors epic : Prometheus et Grafana, PostgreSQL et Keycloak, test de charge.
