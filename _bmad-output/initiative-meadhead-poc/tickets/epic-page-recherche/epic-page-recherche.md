---
type: epic
title: "Une page Angular obtient la recommandation et l'affiche"
parent: initiative-meadhead-poc
covers: [R5, R6, R7]
sources: []
companions: []
after: []
refined: false
risk: medium
---

# Une page Angular obtient la recommandation et l'affiche

## Description

Une page Angular unique et sobre : l'utilisateur s'authentifie, choisit une spécialité NHS, saisit ses coordonnées GPS ou utilise la géolocalisation du navigateur, et voit l'hôpital recommandé ou le motif d'absence renvoyé par l'API. Le scénario de référence est vérifié de bout en bout.

## Outcome

Un utilisateur obtient la recommandation depuis son navigateur sans connaître l'API, et le comité voit le parcours complet fonctionner.

## Done when

- Depuis la page, un utilisateur authentifié obtient Fred Brooks pour la cardiologie près de Fred Brooks, en test E2E exécuté par la CI.
- Le motif est affiché quand aucun hôpital ne convient.
- Les tests unitaires du front passent en CI et l'image Docker du front démarre avec le reste de la PoC.

## Boundaries

- R6 : cet epic obtient et transmet le jeton côté navigateur ; la protection de l'API appartient à epic-recommandation-hopital.
- Pas de visuel élaboré (mission §Front-end).

## References

- parent — _bmad-output/initiative-meadhead-poc/initiative-meadhead-poc.md, Requirements R5, R6, R7
