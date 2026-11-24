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
  - _bmad-output/initiative-meadhead-plateforme/initiative-meadhead-plateforme.md
companions:
  - specs/Données_de_référence_sur_les_spécialités_NHS.md
  - _bmad-output/initiative-meadhead-plateforme/architecture-meadhead-plateforme/architecture-meadhead-plateforme.md
risk: medium
---

# Les intervenants d'urgence obtiennent en temps réel l'hôpital compétent le plus proche disposant d'un lit

## Description

Preuve de concept du système d'intervention d'urgence en temps réel, demandée par le comité d'architecture du consortium MedHead pour valider l'architecture cible sur son point le plus risqué : recommander à temps un lit dans le service hospitalier approprié. Une API REST Java, consommée par une page Angular, propose l'hôpital le plus proche par la route qui offre la spécialité demandée et un lit disponible, puis publie un événement de réservation. Le tout est testé selon la pyramide, éprouvé en charge, livré en conteneurs Docker par un pipeline GitHub, documenté pour servir de modèle aux équipes. Le périmètre est celui du document des exigences de la PoC ; les autres documents de `specs/` en fixent le contexte et les contraintes.

## Outcome

Le comité d'architecture dispose d'une preuve fonctionnelle et mesurée qu'un service conforme aux principes MedHead peut acheminer une urgence vers l'hôpital compétent le plus proche en moins de 200 ms sous 800 requêtes par seconde, au service de l'objectif de plus de 90 % d'urgences bien acheminées et d'un traitement ramené de 18,25 à 12 minutes.

## Requirements

- R1 (exigences §Scénario, §Exigences): pour une spécialité et une localisation d'urgence, l'API REST renvoie l'hôpital le plus proche disposant d'au moins un lit disponible et offrant la spécialité ; le scénario Fred Brooks / Julia Crusher / Beverly Bashir propose Fred Brooks.
- R2 (exigences §Scénario ; Decision 2026-11-23): quand un hôpital est recommandé, un événement de réservation de lit est publié et observable par un consommateur ; si le bus n'en accuse pas réception dans le délai, la recommandation est quand même renvoyée, marquée réservation non demandée, et une alerte est levée.
- R3 (mission §Back-end, Points de vigilance): la proximité se mesure en distance réellement à parcourir, pas à vol d'oiseau ; un cas où le plus proche à vol d'oiseau n'est pas le plus proche par la route le démontre.
- R4 (nhs): les spécialités sélectionnables sont celles des Données de référence NHS (groupe et spécialité) ; une spécialité hors liste est refusée avec une erreur explicite.
- R5 (exigences §Exigences, interface ; Decision 2026-11-16): une page web unique permet de choisir une spécialité, de saisir la localisation en coordonnées GPS (ou via la géolocalisation du navigateur) et d'afficher l'hôpital recommandé renvoyé par l'API.
- R6 (exigences §Exigences, protection ; dda §Contraintes ; Decision 2026-11-16): seules la spécialité et la localisation de l'urgence sont transmises, sans identité du patient ni conservation ; elles sont protégées en transit et seuls des appelants authentifiés obtiennent une recommandation ; un appel non authentifié est refusé.
- R7 (exigences §Exigences, tests ; principes B4, C4-V): la PoC est validée par une pyramide de tests unitaires, d'intégration et E2E, les critères d'acceptation métier écrits en scénarios BDD dans le langage du domaine.
- R8 (exigences §Déclaration, §Exigences, tests): un test de stress montre un temps de réponse inférieur à 200 ms jusqu'à 800 requêtes par seconde par instance de service, avec un rapport de mesures.
- R9 (exigences §Exigences, CI/CD ; principes B3, C4-VI ; mission §CI/CD): un pipeline CI/CD se déclenche sur les push et les PR, découpé en étapes distinctes (tests, build, images), et conserve des rapports d'exécution des tests inspectables.
- R10 (exigences §Exigences, readme ; mission §CI/CD): un `readme.md` explique comment builder et exécuter la PoC, exécuter les tests, le fonctionnement du pipeline et le workflow Git, de façon réutilisable par les équipes.
- R11 (exigences §Exigences, reporting ; mission §Reporting): un document de reporting justifie les technologies, le respect des normes (RGPD) et des principes, et présente les résultats, les métriques de temps d'exécution de l'API et les enseignements.
- R12 (exigences §Exigences, bloc de construction ; déclaration §Continuum): la PoC sert de bloc de construction ou de modèle : contrat d'API publié en OpenAPI, code partageable, stratégie de test et pipeline réutilisables.
- R13 (déclaration §Contenu de l'architecture ; principes B1): quand un service auxiliaire factice est indisponible, l'API répond dans le délai avec une erreur explicite ou un résultat dégradé, démontré par un test avec le service coupé.
- R14 (Decision 2026-11-16 ; principes A4): quand aucun hôpital n'offre la spécialité avec un lit disponible, l'API renvoie une réponse explicite avec son motif et ne publie aucun événement ; à distance égale, l'hôpital ayant le plus de lits disponibles l'emporte, puis l'ordre alphabétique.
- R15 (exigences §Déclaration ; principes B1 ; Decision 2026-11-23): pendant un test de charge, la latence (p95, p99), le débit et le taux d'erreur de l'API sont visibles en temps réel sur un tableau de bord et comparés aux seuils de 200 ms et 800 requêtes par seconde.

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
- Authentification par jeton JWT émis par un fournisseur d'identité factice conteneurisé, préfigurant l'intégration OIDC de la cible ; transport HTTPS (principes C3 ; Decision 2026-11-16).
- Le document de reporting et les documents d'architecture vivent dans un dépôt GitHub d'architecture séparé ; ce dépôt-ci contient le code, la CI, les tests et le readme (mission §Livrables ; Decision 2026-11-16).
- Backend Spring Boot ; tests de charge JMeter contre l'image Docker (mission §Back-end ; Decision 2026-11-16).
- La supervision tourne dans ses propres conteneurs, séparés de l'API dont les ressources sont limitées, pour ne pas fausser la mesure par instance (Decision 2026-11-23).

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
- Touch point: dépôt GitHub d'architecture séparé — reçoit les documents de `specs/` et le document de reporting (R11) ; owner : epic-documentation-reporting.
- Hors backlog : la présentation au comité d'architecture.
- Parcours traceur : l'utilisateur choisit une spécialité et saisit une localisation dans la page Angular → l'API REST interroge les données hospitalières factices et calcule les distances routières → l'hôpital recommandé s'affiche et un événement de réservation de lit est publié.

## References

- exigences — specs/Exigences_pour_le_developpement_de_la_PoC.md, sections Déclaration, Scénario, Exigences
- mission — specs/Mission.md, étapes Back-end, Front-end, CI/CD, Reporting et Livrables
- nhs — specs/Données_de_référence_sur_les_spécialités_NHS.md, table des spécialités (adopté comme compagnon)
- principes — specs/Principes_de_l_architecture.md, A3, B1 à B6, C3, C4
- déclaration — specs/Declaration_des_travaux_d_architecture.md, Simplifications suggérées, Contenu de l'architecture, Prise en charge du continuum
- dda — specs/Document_de_definition_de_l_architecture.md, Contraintes, Limites, Analyse de l'écart
- architecture — _bmad-output/initiative-meadhead-plateforme/architecture-meadhead-plateforme/architecture-meadhead-plateforme.md, AD-1 à AD-18 (adopté comme compagnon)

## Notes

- Decision: 2026-11-16 — périmètre limité à la PoC décrite dans le document des exigences ; les autres documents de `specs/` sont contexte et contraintes.
- Decision: 2026-11-16 — stack : Java (API REST), Angular, Docker, GitHub (issues, PR, CI).
- Decision: 2026-11-16 — workflow Git : Gitflow (`main`, `develop`, branches de travail préfixées issues de `develop`).
- Decision: 2026-11-16 — localisation saisie en coordonnées GPS, manuelle ou via la géolocalisation du navigateur ; pas de géocodage dans le chemin critique.
- Decision: 2026-11-16 — données minimales (spécialité et localisation, sans identité ni conservation) ; API protégée par JWT émis par un fournisseur d'identité factice ; HTTPS.
- Decision: 2026-11-16 — aucun hôpital convenable : réponse explicite, aucun événement ; égalité départagée par le nombre de lits puis l'ordre alphabétique.
- Decision: 2026-11-16 — dépôt d'architecture séparé pour les documents et le reporting.
- Decision: 2026-11-16 — pas d'échéance fixe.
- Decision: 2026-11-16 — confirmés : Spring Boot ; hôpitaux fictifs incluant les trois du scénario ; objectifs métier (90 %, 18,25 → 12 min) non mesurés par la PoC, discutés dans le reporting ; tests de charge JMeter contre l'image Docker ; présentation au comité hors backlog.
- Decision: 2026-11-23 — la PoC s'inscrit sous `initiative-meadhead-plateforme` ; supervision de performance (Prometheus, Grafana) ajoutée (R15), la preuve reste le rapport JMeter.
- Decision: 2026-11-23 — découpage en 5 epics approuvé ; sécurité intégrée aux epics 2 et 3 ; tenue en charge en epic séparé (R8, R15) ; dépôt d'architecture rattaché à l'epic 5.
- Decision: 2026-11-23 — le test de charge se lance hors CI ; chaque epic écrit sa propre section du readme, pour éviter les collisions entre les epics 3 et 4.
- Decision: 2026-11-23 — hôpitaux fictifs situés dans le Grand Londres, zone de l'extrait OSRM (architecture AD-8) ; remplace « au Royaume-Uni ».
- Decision: 2026-11-23 — distances routières par OSRM auto-hébergé (architecture AD-8) ; bus d'événements Apache Kafka (architecture AD-3).
- Decision: 2026-11-23 — sans accusé du bus dans le délai, la recommandation est renvoyée marquée réservation non demandée, avec alerte (R2).
- Decision: 2026-11-24 — architecture adoptée comme compagnon.
