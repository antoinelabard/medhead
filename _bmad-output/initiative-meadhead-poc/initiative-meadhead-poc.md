---
type: initiative
title: "Les intervenants d'urgence obtiennent en temps réel l'hôpital compétent le plus proche disposant d'un lit"
sources:
  - specs/Exigences_pour_le_developpement_de_la_PoC.md
  - specs/Mission.md
  - specs/Données_de_référence_sur_les_spécialités_NHS.md
  - specs/Principes_de_l_architecture.md
  - specs/Declaration_des_travaux_d_architecture.md
  - specs/Document_de_definition_de_l_architecture.md
companions:
  - specs/Données_de_référence_sur_les_spécialités_NHS.md
risk: medium
---

# Les intervenants d'urgence obtiennent en temps réel l'hôpital compétent le plus proche disposant d'un lit

## Description

Preuve de concept du système d'intervention d'urgence en temps réel, demandée par le comité d'architecture du consortium MedHead pour valider l'architecture cible sur son point le plus risqué : recommander à temps un lit dans le service hospitalier approprié. Une API REST Java, consommée par une page Angular, propose l'hôpital le plus proche par la route qui offre la spécialité demandée et un lit disponible, puis publie un événement de réservation. Le tout est testé selon la pyramide, éprouvé en charge, livré en conteneurs Docker par un pipeline GitHub, documenté pour servir de modèle aux équipes. Le périmètre est celui du document des exigences de la PoC ; les autres documents de `specs/` en fixent le contexte et les contraintes.

## Outcome

Le comité d'architecture dispose d'une preuve fonctionnelle et mesurée qu'un service conforme aux principes MedHead peut acheminer une urgence vers l'hôpital compétent le plus proche en moins de 200 ms sous 800 requêtes par seconde, au service de l'objectif de plus de 90 % d'urgences bien acheminées et d'un traitement ramené de 18,25 à 12 minutes.

## Requirements

- R1 (exigences §Scénario, §Exigences): pour une spécialité et une localisation d'urgence, l'API REST renvoie l'hôpital le plus proche disposant d'au moins un lit disponible et offrant la spécialité ; le scénario Fred Brooks / Julia Crusher / Beverly Bashir propose Fred Brooks.
- R2 (exigences §Scénario): quand un hôpital est recommandé, un événement de réservation de lit est publié et observable par un consommateur.
- R3 (mission §Back-end, Points de vigilance): la proximité se mesure en distance réellement à parcourir, pas à vol d'oiseau ; un cas où le plus proche à vol d'oiseau n'est pas le plus proche par la route le démontre.
- R4 (nhs): les spécialités sélectionnables sont celles des Données de référence NHS (groupe et spécialité) ; une spécialité hors liste est refusée avec une erreur explicite.
- R5 (exigences §Exigences, interface): une page web unique permet de choisir une spécialité, de saisir la localisation et d'afficher l'hôpital recommandé renvoyé par l'API.
- R6 (exigences §Exigences, protection ; dda §Contraintes): les données du patient transmises sont protégées en transit et seuls des appelants authentifiés obtiennent une recommandation ; un appel non authentifié est refusé.
- R7 (exigences §Exigences, tests ; principes B4, C4-V): la PoC est validée par une pyramide de tests unitaires, d'intégration et E2E, les critères d'acceptation métier écrits en scénarios BDD dans le langage du domaine.
- R8 (exigences §Déclaration, §Exigences, tests): un test de stress montre un temps de réponse inférieur à 200 ms jusqu'à 800 requêtes par seconde par instance de service, avec un rapport de mesures.
- R9 (exigences §Exigences, CI/CD ; principes B3, C4-VI ; mission §CI/CD): un pipeline CI/CD se déclenche sur les push et les PR, découpé en étapes distinctes (tests, build, images), et conserve des rapports d'exécution des tests inspectables.
- R10 (exigences §Exigences, readme ; mission §CI/CD): un `readme.md` explique comment builder et exécuter la PoC, exécuter les tests, le fonctionnement du pipeline et le workflow Git, de façon réutilisable par les équipes.
- R11 (exigences §Exigences, reporting ; mission §Reporting): un document de reporting justifie les technologies, le respect des normes (RGPD) et des principes, et présente les résultats, les métriques de temps d'exécution de l'API et les enseignements.
- R12 (exigences §Exigences, bloc de construction ; déclaration §Continuum): la PoC sert de bloc de construction ou de modèle : contrat d'API publié en OpenAPI, code partageable, stratégie de test et pipeline réutilisables.
- R13 (déclaration §Contenu de l'architecture ; principes B1): quand un service auxiliaire factice est indisponible, l'API répond dans le délai avec une erreur explicite ou un résultat dégradé, démontré par un test avec le service coupé.

## Constraints

- Backend en Java, API RESTful capable de s'inscrire dans une architecture microservice (exigences §Exigences ; Decision 2026-11-16).
- Frontend Angular, parmi les frameworks imposés Angular, React, VueJS (exigences §Exigences ; Decision 2026-11-16).
- Déploiement par conteneurs Docker, infrastructure immuable et reproductible (principes C3 ; Decision 2026-11-16).
- Code, issues, PR et CI sur GitHub ; workflow Gitflow documenté (exigences §Exigences ; Decision 2026-11-16).
- Données factices ou anonymisées uniquement, PoC isolée de toute production (principes C4-II ; dda §Limites).
- Les principes B1 à B4 ne sont pas assouplis pour la PoC (principes C4-IV).
- Les composants publient leurs événements métier (principes B6).
- Contrat de service spécifié en OpenAPI ; Javadoc pour le code, Markdown pour la documentation projet (principes C3).
- Données patient traitées conformément au RGPD (dda §Contraintes ; principes A3).
- Les services auxiliaires peuvent être factices avec des contrats simplifiés, lacunes documentées (déclaration §Simplifications suggérées ; mission §Back-end).
- Le pipeline ne déploie pas la PoC en production sur un cloud (mission §CI/CD).

## Done when

- Le scénario de référence (cardiologie près de Fred Brooks → Fred Brooks proposé, événement de réservation publié) passe de bout en bout depuis la page Angular, en test E2E exécuté par la CI.
- Le rapport de test de stress montre moins de 200 ms de temps de réponse à 800 requêtes par seconde sur une instance du service.
- Le pipeline GitHub Actions est vert sur `develop` et `main` : tests unitaires, d'intégration et E2E, build, images Docker, rapports de tests archivés.
- La PoC complète démarre localement à partir des images Docker en une commande documentée.
- Le `readme.md` et le document de reporting contiennent toutes les rubriques exigées.

## Boundaries

- Hors périmètre : la plateforme cible MedHead au-delà de la PoC (maillage de services, lac de données, passerelle API, intégration OIDC avec les fournisseurs d'identité de l'État), la prise de rendez-vous, la planification du personnel, le tri selon les symptômes, la pré-réservation nationale, la gestion des médicaments.
- Hors périmètre : toute donnée patient réelle et tout déploiement en production ou sur un cloud.
- Hors périmètre : un visuel élaboré ; la page reste simple et fonctionnelle (mission §Front-end).
- Les systèmes auxiliaires (disponibilité des lits, spécialités des hôpitaux) sont factices.
- Parcours traceur : l'utilisateur choisit une spécialité et saisit une localisation dans la page Angular → l'API REST interroge les données hospitalières factices et calcule les distances routières → l'hôpital recommandé s'affiche et un événement de réservation de lit est publié.

## References

- exigences — specs/Exigences_pour_le_developpement_de_la_PoC.md, sections Déclaration, Scénario, Exigences
- mission — specs/Mission.md, étapes Back-end, Front-end, CI/CD, Reporting et Livrables
- nhs — specs/Données_de_référence_sur_les_spécialités_NHS.md, table des spécialités (adopté comme compagnon)
- principes — specs/Principes_de_l_architecture.md, A3, B1 à B6, C3, C4
- déclaration — specs/Declaration_des_travaux_d_architecture.md, Simplifications suggérées, Contenu de l'architecture, Prise en charge du continuum
- dda — specs/Document_de_definition_de_l_architecture.md, Contraintes, Limites, Analyse de l'écart

## Notes

- Decision: 2026-11-16 — périmètre limité à la PoC décrite dans le document des exigences ; les autres documents de `specs/` sont contexte et contraintes.
- Decision: 2026-11-16 — stack : Java (API REST), Angular, Docker, GitHub (issues, PR, CI).
- Decision: 2026-11-16 — workflow Git : Gitflow (`main`, `develop`, branches de travail préfixées issues de `develop`).
- Assumption: backend Spring Boot, recommandé par la mission.
- Assumption: jeu de données d'hôpitaux fictifs situés au Royaume-Uni, incluant les trois hôpitaux du scénario.
- Assumption: les objectifs métier (plus de 90 % d'acheminements corrects, 18,25 → 12 min) ne sont pas mesurables par la PoC ; elle démontre le comportement qui y contribue et le reporting en discute.
- Assumption: tests de stress avec JMeter contre l'image Docker, rapport versionné.
- Assumption: la présentation au comité d'architecture est hors backlog.
- Open question: source des distances routières — service de routage externe ou service factice à distances pré-calculées ? Attend l'architecture.
- Open question: format de la localisation saisie — adresse, code postal ou coordonnées GPS ?
- Open question: quelles données patient la PoC traite-t-elle, et quel mécanisme d'authentification (OIDC simulé, jeton, clé API) ?
- Open question: que renvoie l'API quand aucun hôpital ne convient (aucun lit, spécialité absente) et en cas d'égalité de distance ?
- Open question: le dépôt d'architecture demandé par la mission (documents et reporting) est-il un dépôt séparé ou un dossier de ce dépôt ?
- Open question: bus d'événements réel (Kafka, RabbitMQ) ou publication simulée pour l'événement de réservation ? Attend l'architecture.
- Open question: quelle est l'échéance du délai imparti ?
