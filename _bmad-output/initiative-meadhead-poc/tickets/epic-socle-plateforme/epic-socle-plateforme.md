---
type: epic
title: "Socle de la PoC livré en conteneurs par un pipeline GitHub"
parent: initiative-meadhead-poc
covers: [R9, R10, R12]
sources: []
companions: []
after: []
refined: false
risk: low
---

# Socle de la PoC livré en conteneurs par un pipeline GitHub

## Description

Ouvre l'initiative avec la base de plateforme : squelettes Spring Boot et Angular, images Docker, démarrage de l'ensemble en une commande, pipeline GitHub Actions déclenché sur chaque push et PR, workflow Gitflow et première version du readme. Les epics suivants s'y branchent ; le socle de production (`initiative-socle-production`) le prolongera, il doit donc rester compatible avec une architecture microservices.

## Outcome

Les développeurs de la PoC disposent dès le premier jour d'un dépôt qui se builde, se teste et se livre en conteneurs de façon reproductible.

## Done when

- Les squelettes back et front se buildent en images Docker et démarrent ensemble en une commande documentée.
- Un push ou une PR déclenche le pipeline (tests, build, images) et ses rapports de tests sont consultables dans GitHub.
- Le readme décrit le build, l'exécution, le pipeline et le workflow Gitflow.

## Boundaries

- N'implémente aucune règle métier de recommandation (epic-recommandation-hopital) ni la page de recherche (epic-page-recherche).
- Ne déploie rien en production ni sur un cloud.

## References

- parent — _bmad-output/initiative-meadhead-poc/initiative-meadhead-poc.md, Requirements R9, R10, R12
