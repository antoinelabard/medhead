---
type: epic
title: "L'API recommande l'hôpital compétent le plus proche par la route et réserve un lit"
parent: initiative-meadhead-poc
covers: [R1, R2, R3, R4, R6, R7, R12, R13, R14]
sources: []
companions: []
after: []
refined: false
risk: high
---

# L'API recommande l'hôpital compétent le plus proche par la route et réserve un lit

## Description

Le cœur de la PoC : une API REST Java, décrite en OpenAPI et protégée par JWT, reçoit une spécialité NHS et des coordonnées GPS, interroge des données hospitalières factices, calcule les distances routières et renvoie l'hôpital le plus proche disposant d'un lit, puis publie l'événement de réservation. Elle gère les cas où rien ne convient, les égalités, et la panne d'un service factice. Les critères métier sont écrits en scénarios BDD.

## Outcome

Un appelant authentifié obtient la recommandation correcte du scénario de référence et des cas limites, prouvée par des tests automatisés en CI.

## Done when

- Les scénarios BDD (scénario Fred Brooks, aucun hôpital convenable, égalité, distance routière contre vol d'oiseau, spécialité hors liste NHS) passent en CI.
- Un appel non authentifié est refusé ; un appel authentifié reçoit la recommandation.
- L'événement de réservation est observé par un consommateur de test à chaque recommandation, et jamais sans recommandation.
- Avec un service factice coupé, l'API répond dans le délai avec une erreur explicite ou un résultat dégradé.
- Le contrat OpenAPI est publié et l'image Docker de l'API démarre avec le reste de la PoC.

## Boundaries

- R6 : cet epic protège l'API et fournit le fournisseur d'identité factice ; l'obtention du jeton côté navigateur appartient à epic-page-recherche.
- R7 : tests unitaires, d'intégration et BDD de l'API ; les tests front et E2E appartiennent à epic-page-recherche.
- La preuve de tenue en charge appartient à epic-tenue-en-charge.

## References

- parent — _bmad-output/initiative-meadhead-poc/initiative-meadhead-poc.md, Requirements R1–R4, R6, R7, R12–R14
- nhs — specs/Données_de_référence_sur_les_spécialités_NHS.md
