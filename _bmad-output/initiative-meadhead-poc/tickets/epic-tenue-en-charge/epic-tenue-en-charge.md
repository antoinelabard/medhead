---
type: epic
title: "La tenue en charge de l'API est prouvée et mesurée"
parent: initiative-meadhead-poc
covers: [R8, R15]
sources: []
companions: []
after: []
refined: false
risk: high
---

# La tenue en charge de l'API est prouvée et mesurée

## Description

Répond à la première préoccupation des parties prenantes : un plan JMeter versionné, joué hors CI contre l'image Docker d'une instance de l'API, prouve moins de 200 ms jusqu'à 800 requêtes par seconde. Prometheus et Grafana, dans leurs propres conteneurs, affichent en direct la latence p95/p99, le débit et le taux d'erreur face aux seuils ; un tableau de bord versionné est provisionné automatiquement. Les optimisations de l'API nécessaires pour atteindre les seuils font partie de cet epic.

## Outcome

Le comité dispose d'une mesure reproductible et observable de la tenue en charge, reprise telle quelle dans le reporting.

## Done when

- Le rapport JMeter montre moins de 200 ms de temps de réponse à 800 requêtes par seconde sur une instance aux ressources limitées.
- Pendant le test, le tableau de bord Grafana affiche latence p95/p99, débit et erreurs avec les seuils en référence.
- Le plan, la configuration de supervision et la procédure de lancement sont versionnés et décrits dans la section du readme propre à cet epic.

## Boundaries

- Le test de charge ne devient pas une étape de la CI (Decision 2026-11-23).
- La supervision de tous les services et la collecte des journaux appartiennent à initiative-socle-production.

## References

- parent — _bmad-output/initiative-meadhead-poc/initiative-meadhead-poc.md, Requirements R8, R15
