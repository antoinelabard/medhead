---
type: initiative
title: "Les nouveaux microservices s'ajoutent sur un socle sécurisé, redondant et supervisé"
sources:
  - _bmad-output/initiative-meadhead-plateforme/initiative-meadhead-plateforme.md
companions:
  - _bmad-output/initiative-meadhead-plateforme/architecture-meadhead-plateforme/architecture-meadhead-plateforme.md
risk: medium
---

# Les nouveaux microservices s'ajoutent sur un socle sécurisé, redondant et supervisé

## Description

Prolonge le socle de la PoC (epic-socle-plateforme de `initiative-meadhead-poc`) en un environnement prêt pour la production, construit dans Docker : passerelle API, service IAM OIDC avec rôles et permissions, base de données redondante avec sauvegardes et réplicas en lecture seule, supervision par Grafana (en étendant celle de la PoC) et Graylog, et un modèle documenté pour ajouter un microservice. L'API de recommandation de la PoC en est le premier service. Démarre une fois la PoC validée ; ses exigences seront écrites à partir des lignes R2 à R6 de l'initiative parente quand elle sera reprise.

## Outcome

Les équipes MedHead ajoutent de nouvelles fonctionnalités en microservices sans reconstruire l'infrastructure, avec sécurité, redondance et supervision déjà en place.

## Done when

- L'API de recommandation tourne derrière la passerelle, authentifiée par l'IAM, sur une base redondante, visible dans Grafana et Graylog.
- Une restauration de sauvegarde et la perte du nœud primaire sont démontrées sans perte de données validées.
- Un service d'exemple est ajouté en suivant le modèle documenté, sans modifier les services existants.

## Boundaries

- Ne modifie ni le livrable de la PoC ni sa mesure de 200 ms par instance de service.
- Hors périmètre : les fonctionnalités métier au-delà de la PoC, le maillage de services et le lac de données de la cible.

## References

- parent — _bmad-output/initiative-meadhead-plateforme/initiative-meadhead-plateforme.md, Requirements R2–R6
- initiative — _bmad-output/initiative-meadhead-poc/initiative-meadhead-poc.md
- architecture — _bmad-output/initiative-meadhead-plateforme/architecture-meadhead-plateforme/architecture-meadhead-plateforme.md, AD-1 à AD-18 et Deferred

## Notes

- Decision: 2026-11-23 — Docker Compose ; Kubernetes et GitOps reportés (architecture Deferred).
- Decision: 2026-11-23 — pas de déploiement réel par le pipeline pour l'instant (architecture AD-14).
- Decision: 2026-11-23 — Graylog sans SIEM complet (architecture AD-15).
