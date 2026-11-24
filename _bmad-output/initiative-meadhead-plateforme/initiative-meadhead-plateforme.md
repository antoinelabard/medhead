---
type: initiative
title: "La plateforme MedHead démarre avec une PoC validée et un socle prêt pour la production"
sources:
  - specs/Exigences_pour_le_developpement_de_la_PoC.md
  - specs/Principes_de_l_architecture.md
  - specs/Document_de_definition_de_l_architecture.md
companions: []
risk: medium
---

# La plateforme MedHead démarre avec une PoC validée et un socle prêt pour la production

## Description

Initiative parente de deux initiatives construites l'une après l'autre. La première, `initiative-meadhead-poc`, livre la preuve de concept demandée par le comité d'architecture, sans changement de périmètre. La seconde, `initiative-socle-production`, prolonge son socle (epic 1) en un environnement prêt pour la production : passerelle API, service IAM, base de données redondante avec sauvegardes et réplicas en lecture, supervision par Grafana et Graylog, et un modèle documenté pour ajouter des microservices. L'objectif est que les développements continuent après la validation de la PoC, avec de nouvelles fonctionnalités, dans une architecture microservices cadrée côté DevOps.

## Outcome

Les équipes MedHead peuvent ajouter de nouvelles fonctionnalités en microservices sur un socle sécurisé, redondant et supervisé, à partir d'une PoC validée par le comité d'architecture.

## Requirements

- R1 (exigences ; Decision 2026-11-16): la PoC du système d'intervention d'urgence est livrée et validée selon `initiative-meadhead-poc`.
- R2 (Decision 2026-11-16 ; dda §Architecture cible): un point d'entrée unique route le trafic web vers les services, termine TLS et limite le débit ; un service n'est joignable de l'extérieur que par lui.
- R3 (Decision 2026-11-16 ; principes C3): un service IAM OIDC authentifie les utilisateurs et porte rôles et permissions ; un appel sans le rôle requis est refusé par le service.
- R4 (Decision 2026-11-16 ; principes B1): les données ont des sauvegardes automatisées dont la restauration est testée, une redondance qui survit à la perte d'un nœud, et des réplicas en lecture seule qui servent les lectures sans charger le primaire.
- R5 (Decision 2026-11-16, 2026-11-23 ; principes B1, C3): les métriques système et applicatives de tous les services sont visibles dans Grafana, en étendant la supervision de performance livrée par la PoC (son R15), et les journaux de tous les services sont centralisés et consultables dans Graylog.
- R6 (Decision 2026-11-16 ; exigences §Exigences, bloc de construction ; dda §Objectifs): un nouveau microservice s'ajoute en suivant un modèle documenté (pipeline, image, routage, IAM, supervision) sans modifier les services existants.

## Constraints

- Stack et outillage communs : Java/Spring Boot, Angular, Docker, GitHub (issues, PR, Actions), Gitflow (Decision 2026-11-16).
- Données factices ou anonymisées uniquement (principes C4-II ; dda §Limites).
- La seconde initiative ne modifie pas le livrable PoC ni sa mesure de 200 ms par instance de service (Decision 2026-11-16).

## Done when

- La PoC est validée selon le `Done when` de `initiative-meadhead-poc`.
- L'API de recommandation tourne derrière la passerelle, authentifiée par l'IAM, sur une base redondante, et apparaît dans Grafana et Graylog.
- Une restauration de sauvegarde et la perte du nœud primaire sont démontrées sans perte de données validées.
- Un service d'exemple est ajouté en suivant le modèle documenté, sans modifier les services existants.

## Boundaries

- Hors périmètre : les fonctionnalités métier au-delà de la PoC (rendez-vous, planification du personnel, tri selon les symptômes, pré-réservation nationale, médicaments), le maillage de services et le lac de données de la cible.
- Hors périmètre : toute donnée patient réelle.
- Parcours traceur : utilisateur → passerelle → IAM → API de recommandation → base primaire et réplica en lecture → métriques dans Grafana et journaux dans Graylog.

## References

- exigences — specs/Exigences_pour_le_developpement_de_la_PoC.md, section Exigences
- principes — specs/Principes_de_l_architecture.md, B1, C3, C4
- dda — specs/Document_de_definition_de_l_architecture.md, Objectifs, Architecture cible
- initiative — _bmad-output/initiative-meadhead-poc/initiative-meadhead-poc.md

## Notes

- Decision: 2026-11-16 — la préparation à la production est une initiative séparée sous cette initiative parente ; la PoC reste inchangée.
- Decision: 2026-11-16 — passerelle API, IAM, base de données redondante, Grafana et Graylog sont réellement construits dans Docker.
- Assumption: la seconde initiative démarre une fois la PoC validée, sur ses fondations (socle Docker et CI de l'epic 1, API de recommandation comme premier service).
- Open question: environnement cible du socle de production — Docker Compose sur un hôte, Docker Swarm ou Kubernetes ? Attend l'architecture.
- Open question: le socle de production est-il déployé par le pipeline vers un environnement réel, et lequel ?
- Open question: Graylog sert-il seulement à la collecte des journaux ou aussi de SIEM (règles de détection, alertes de sécurité) ?
