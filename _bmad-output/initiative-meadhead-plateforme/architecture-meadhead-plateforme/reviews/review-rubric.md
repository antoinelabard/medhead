# Revue — Architecture Spine Plateforme MedHead (rubrique + réconciliation)

- Document revu : `architecture-meadhead-plateforme.md` (status: draft, 2026-11-23)
- Entrées confrontées : `initiative-meadhead-plateforme.md` (R1–R6), `initiative-meadhead-poc.md` (R1–R15, Constraints, Boundaries, Notes), `initiative-socle-production.md`, `.memlog.md`
- Date de revue : 2026-11-23

## Verdict

Spine solide et terse : le cœur du domaine (propriété des données, événements, réservation, règles de décision, IAM, contrat REST) est bien verrouillé et toutes les questions ouvertes des initiatives sont tranchées. En revanche, plusieurs points de divergence réels restent ouverts pour les epics (exposition de Keycloak au navigateur en contradiction avec AD-11, publication de `bed-reservation-requested` sans base ni outbox, conservation des coordonnées dans Kafka, préparation des données OSRM, configuration à l'exécution du front), quelques décisions du memlog ne sont pas descendues dans le spine, et l'enveloppe opérationnelle est incomplète (secrets et certificat TLS, stockage de Keycloak, adressage du primaire PostgreSQL après bascule, acheminement des journaux vers Graylog). **Recommandation : à corriger avant de passer `status: ready`** (aucun bloquant de fond, mais deux findings « high » sur la cohérence de construction).

## Synthèse des findings

| # | Sévérité | Sujet | Correction proposée |
| --- | --- | --- | --- |
| F1 | high | Keycloak injoignable par le navigateur (AD-11, Compose) alors que le seed montre `Navigateur → Keycloak` en PKCE ; `iss` du JWT non fixé | Router Keycloak via le point d'entrée (`/auth` sur la même origine) et fixer un hostname/issuer unique (`KC_HOSTNAME`) que les services valident |
| F2 | high | `recommendation-service` n'a pas de base : AD-6 ne s'applique pas, et le comportement si l'envoi Kafka échoue n'est pas décidé (R2 vs R13 vs 200 ms) | Ajouter une règle : envoi `acks=all` avec délai borné ; sur échec, réponse 503 Problem Details `urn:medhead:problem:reservation-unavailable` (ou réponse marquée `reservationPublished: false`) — en choisir une |
| F3 | high | RGPD « sans conservation » (poc-R6) : rien n'interdit de mettre les coordonnées dans `bed-reservation-requested`, topic Kafka retenu ; pas de règle de rétention | Règle : les événements ne portent ni coordonnées ni donnée d'urgence au-delà de `hospitalId`, `specialty`, `recommendationId` ; rétention bornée des topics non compactés |
| F4 | medium | Données OSRM du Grand Londres : qui les prépare, où, quand (build d'image, démarrage, CI, Testcontainers) — non décidé | Décider : image `routing-service` dérivée, données prétraitées à la construction, publiée sur GHCR et réutilisée par CI, E2E et Testcontainers |
| F5 | medium | Décisions du memlog absentes : Kafka 3 nœuds dans le socle, seed Flyway des hôpitaux fictifs du Grand Londres, certificat TLS de dev, périmètre Graylog (alertes simples), « sans consommateur » pour confirmed/rejected | Les reporter dans AD-3, AD-13, AD-11, AD-15 et la carte des capacités |
| F6 | medium | Enveloppe opérationnelle : secrets (au-delà du `.env`), stockage de Keycloak vs AD-13, adressage du primaire après bascule, acheminement des journaux, Alertmanager, environnements | Voir §3.6 : une ligne par dimension, décidée, reportée ou question ouverte |
| F7 | medium | Contraintes silencieuses : Javadoc/Markdown, readme par epic, déclencheurs CI sur push/PR, rapports de tests archivés | Ajouter conventions « Documentation » et « CI » |

---

## 1. Réconciliation avec les entrées

### 1.1 Initiative PoC — exigences R1 à R15

| Exigence | Couverture dans le spine | Statut |
| --- | --- | --- |
| poc-R1 recommandation, scénario Fred Brooks | AD-1, AD-7, AD-8, AD-9 | Couvert. Le jeu de données du scénario (où vivent les trois hôpitaux, comment ils sont créés) n'est pas fixé — voir F5 (seed Flyway). |
| poc-R2 événement de réservation observable | AD-4, AD-5, AD-6 | Partiel : la publication côté `recommendation-service` (sans base) n'a pas de règle de fiabilité — **F2**. |
| poc-R3 distance réelle | AD-8 | Partiel : la métrique de classement n'est pas fixée. OSRM `table` renvoie par défaut des **durées** ; R3 exige une **distance** à parcourir. Fixer `annotations=distance` (ou assumer la durée et le justifier). Le délai OSRM et le coefficient du repli ne sont pas fixés (valeurs à mettre en configuration avec défaut). |
| poc-R4 spécialités NHS (groupe et spécialité) | AD-9, AD-12, convention Spécialités | Couvert. |
| poc-R5 page unique, géolocalisation | AD-11, AD-12 | Couvert sur le fond ; la configuration du front à l'exécution n'est pas décidée — voir §2, AD-14. |
| poc-R6 données minimales, sans conservation, HTTPS, authentification | AD-7, AD-10, AD-11, AD-12, AD-15 | Partiel : HTTPS et authentification couverts ; « sans conservation » non couvert pour Kafka et les traces — **F3**. Keycloak et HTTPS côté navigateur — **F1**. |
| poc-R7 pyramide, BDD en langage métier | AD-17 (Cucumber-JVM en français) | Couvert. |
| poc-R8 200 ms / 800 req/s par instance | AD-7, AD-16 | Couvert. Les limites CPU/mémoire de l'instance mesurée sont « fixées » sans valeur ni emplacement ; fixer qu'elles vivent dans le fichier Compose (profil `load`) et sont reprises dans le rapport. |
| poc-R9 CI/CD sur push et PR, étapes distinctes, rapports inspectables | AD-14, AD-17 | Partiel : déclencheurs push/PR et découpage en étapes non écrits ; « la CI publie les rapports » sans forme (artefacts GitHub Actions, rétention, rapport Cucumber HTML, JUnit XML). — **F7**. |
| poc-R10 readme | Carte : « Conventions » | **Non couvert** : aucune convention ne traite du readme. La note de l'initiative (« chaque epic écrit sa propre section du readme ») n'est pas reprise. |
| poc-R11 reporting, dépôt séparé | Carte : « dépôt d'architecture séparé » | Couvert (mention). |
| poc-R12 bloc de construction | AD-4, AD-12, AD-14 | Couvert. |
| poc-R13 service factice indisponible | AD-3, AD-7, AD-8 | Partiel : OSRM coupé → repli décidé ; `hospital-registry` coupé → la copie locale continue, implicite mais acceptable ; Kafka indisponible à la publication → non décidé (**F2**). |
| poc-R14 aucun hôpital, égalité | AD-5, AD-9 | Couvert. « À distance égale » mérite une précision (égalité au mètre près après arrondi) pour éviter deux lectures, et le classement en mode dégradé doit appliquer les mêmes départages. |
| poc-R15 supervision temps réel | AD-15, conventions Alertes | Couvert. |

### 1.2 Initiative parente — R1 à R6

| Exigence | Couverture | Statut |
| --- | --- | --- |
| R1 | Toute la partie PoC | Couvert. |
| R2 passerelle, TLS, limitation de débit | AD-11 + Deferred (produit) | Couvert ; produit reporté sans risque de divergence (rôle fixé). |
| R3 IAM OIDC, rôles et permissions | AD-10 | Couvert pour un rôle. Convention de nommage des rôles et du `client_id` par service pour le modèle R6 absente (bas). |
| R4 sauvegardes testées, survie à la perte d'un nœud, réplicas | AD-6, AD-13, Deferred | Partiel : « AD-13 rend le code prêt » est vrai pour la séparation lecture/écriture, mais **pas pour la bascule** : la manière dont le code adresse le primaire après promotion (point d'accès unique type proxy/HAProxy/PgBouncer, ou URL JDBC multi-hôtes `targetServerType=primary`) change la configuration de chaque service. À décider ou à lister comme question ouverte. Le Done when « perte du nœud primaire » en Compose mono-hôte = perte d'un conteneur : cette lecture (memlog, constraint) n'apparaît pas dans le spine, seule la HA hôte est reportée. |
| R5 Grafana + Graylog | AD-15 | Partiel : acheminement des journaux vers Graylog non décidé (pilote de journalisation Docker GELF, collecteur type Fluent Bit / Filebeat, ou appender applicatif). C'est un point de divergence pour le modèle R6. Le périmètre Graylog (alertes simples, pas de SIEM) du memlog n'est pas repris. |
| R6 modèle d'ajout de service (pipeline, image, routage, IAM, supervision) | AD-1, AD-2, AD-4, AD-14 | Partiel : la forme du pipeline pour un nouveau service dans le monodépôt (un workflow par service avec filtres de chemins, ou matrice) n'est pas fixée ; c'est précisément ce qu'un nouveau service doit copier. |

### 1.3 Initiative socle de production

Pas d'exigences propres (écrites plus tard à partir de R2–R6). Ses trois questions ouvertes sont tranchées : environnement = Compose (Structural Seed, Deferred), pipeline sans déploiement (AD-14), Graylog sans SIEM (Deferred). La décision Graylog « alertes simples » n'est toutefois pas écrite dans le spine (F5). Boundary « ne modifie pas la mesure 200 ms » : respectée implicitement (AD-16 cible l'instance directement, la passerelle ne change pas la mesure) — pourrait être explicitée en une phrase dans AD-16.

### 1.4 Contraintes et exigences silencieuses

| Contrainte | Statut |
| --- | --- |
| HTTPS | Couvert (AD-11, 443 TLS). Origine du certificat (dev auto-signé / mkcert, généré, jamais versionné) non décidée alors que Playwright et JMeter doivent lui faire confiance — F1/F6. |
| RGPD, aucune donnée patient conservée | Partiel — **F3** (Kafka, traces OpenTelemetry, journaux nginx d'accès). |
| Gitflow | Couvert (convention Git). Les préfixes de branches ne sont pas listés (`feature/`, `fix/`, `release/`, `hotfix/` ?) — bas. |
| Javadoc / Markdown | **Absent** — F7. |
| BDD en langage métier | Couvert (AD-17). |
| Rapports de tests inspectables en CI | Partiel — F7. |
| Dépôt d'architecture séparé | Couvert (carte des capacités). |
| Pas de déploiement cloud | Couvert (AD-14 « ne déploie rien »). |
| Données factices uniquement | **Implicite seulement** : « hôpitaux fictifs » apparaît dans AD-16 ; aucune règle générale, pas de mention des utilisateurs Keycloak fictifs, du seed Flyway, ni de la zone Grand Londres cohérente avec l'extrait OSRM. |
| Supervision dans ses propres conteneurs | Couvert de fait (profil `observability`, conteneurs Prometheus/Grafana séparés, limites sur l'API). Pourrait être une phrase explicite dans AD-15. |
| Principes B1–B4 non assouplis | Non mentionné ; acceptable à cette altitude. |
| B6 événements métier publiés | Couvert (AD-3, AD-4). |

### 1.5 Décisions du memlog non descendues ou contredites

| Entrée memlog | Constat |
| --- | --- |
| Kafka : un nœud en PoC, **cluster 3 nœuds dans le socle** | Absent : le seed socle dit seulement « Kafka ». Ajouter le facteur de réplication et `min.insync.replicas` à viser, sinon les topics créés en PoC (RF=1) ne seront pas conformes au socle. |
| Seed des hôpitaux fictifs du Grand Londres par migrations Flyway | Absent (AD-13 ne parle que du schéma). Divergence possible avec la génération des 2 000 hôpitaux de charge (AD-16) : par Flyway, par l'API, ou injection directe dans Kafka ? À fixer. |
| nginx termine TLS avec un **certificat de développement** | Absent. |
| Graylog : collecte centralisée + alertes simples (pic d'erreurs, échecs d'authentification) | Absent (seul le SIEM reporté apparaît). |
| `BedReservationConfirmed/Rejected` **sans consommateur en PoC** | Absent ; utile pour que l'epic front ne cherche pas à les consommer. |
| Limites CPU/mémoire fixées pour l'API | Présent seulement dans AD-16. OK. |
| recommendation-service **sans base**, index spatial | « En mémoire » présent ; « sans base » implicite. OK, mais c'est cette absence de base qui ouvre F2. |
| Hôpitaux fictifs placés dans la zone de l'extrait OSRM | Absent ; nécessaire pour éviter des positions hors extrait (OSRM renverrait `null`). |

Aucune contradiction franche entre spine et memlog, hormis la formulation d'AD-4 (« état complet de l'hôpital ») qui contredit la décision memlog de **deux topics distincts** profil / capacité : chaque événement porte l'état complet *de son aspect*, pas de l'hôpital — voir §2.

---

## 2. Rubrique — les AD et leurs règles

| AD | Règle applicable ? Empêche la divergence ? | Remarque |
| --- | --- | --- |
| AD-1 | Oui | Bonne règle. |
| AD-2 | Oui sur le papier | Rendre vérifiable : imposer un test ArchUnit par service (règle de couches) ; sinon la règle n'est contrôlée par personne. |
| AD-3 | Oui | Manque la création des topics : auto-création interdite ou non, qui déclare les topics (partitions, compaction, rétention) — fichier dans `infra/` ou `NewTopic` dans le service propriétaire. Deux epics peuvent sinon créer le même topic avec des réglages différents. |
| AD-4 | Partiellement | (a) « état complet de l'hôpital » vs deux topics : reformuler en « état complet de l'aspect porté par le topic ». (b) `version` : un compteur par hôpital partagé entre les deux topics, ou un par topic ? Le consommateur compare à quoi ? À trancher. (c) Idempotence sur l'`id` CloudEvents : pour la réservation, l'idempotence métier doit porter sur `recommendationId` (un renvoi avec un nouvel `id` décrémenterait deux fois). |
| AD-5 | Oui | « avec le nouvel état de capacité » : dans `confirmed` *et* dans `hospital-capacity-changed` (diagramme) ; préciser lequel fait foi pour la copie locale (le topic compacté). |
| AD-6 | Oui pour `hospital-registry` | Ne couvre pas `recommendation-service` (sans base) — **F2**. |
| AD-7 | Oui | Bonne règle, mesurable. |
| AD-8 | Partiellement | Distance vs durée OSRM, valeur du délai et du coefficient en configuration avec défaut, préparation des données OSRM — **F4**. |
| AD-9 | Oui | Préciser l'égalité (arrondi au mètre) et l'application en mode dégradé. |
| AD-10 | Partiellement | Contradiction avec AD-11 et la convention Compose (Keycloak non exposé) alors que le navigateur doit l'atteindre — **F1**. L'émetteur (`iss`) doit être identique vu du navigateur et des services ; sinon la validation locale du JWT échoue selon l'URL utilisée. |
| AD-11 | Partiellement | Voir F1 ; préciser que Keycloak est servi derrière le point d'entrée (ou exposé comme seconde exception). |
| AD-12 | Oui | Générateur non nommé (openapi-generator ? version ?) — bas, mais deux epics (back, front) choisiront chacun le leur. |
| AD-13 | Partiellement | Ne dit pas où Keycloak stocke ses données : en mode production Keycloak exige une base ; s'il partage l'instance PostgreSQL de `hospital-registry`, AD-13 est violé. Adressage du primaire après bascule — voir §1.2 R4. |
| AD-14 | Partiellement | (a) Le front est statique : « configuré uniquement par variables d'environnement » impose un mécanisme d'injection à l'exécution (ex. `config.json` généré par l'entrypoint nginx) — à décider, sinon l'image front diffère par environnement. (b) Secrets du socle : variables d'environnement seules, ou fichiers (Docker secrets, convention `*_FILE`) ? |
| AD-15 | Oui | Pas de destination des alertes (Alertmanager ?) ; acheminement des journaux vers Graylog non décidé ; traces : interdire coordonnées dans les attributs de span. |
| AD-16 | Oui | Préciser comment les 2 000 hôpitaux sont injectés (cf. §1.5). |
| AD-17 | Oui | Ajouter : ArchUnit (AD-2), forme des rapports CI, et qui fournit l'image OSRM de test (F4). |

### Conventions

- Bonne couverture (noms, topics, formats, erreurs, versionnage, Compose, images, alertes, Git).
- Manquent : **Documentation** (Javadoc sur les API publiques et ports, Markdown pour la doc projet, readme par sections d'epic), **CI** (déclencheurs push/PR, un job par étape, artefacts de rapports conservés N jours), **Topics** (partitions, RF, rétention), **Rôles IAM** (convention de nommage pour R6).
- Carte des capacités : poc-R10 renvoie à « Conventions » où rien ne le traite — lien pendant.

### Deferred — un report peut-il laisser deux unités diverger ?

| Report | Risque |
| --- | --- |
| Kubernetes / GitOps / hébergeur | Aucun pour les epics actuels (images portables, AD-14). OK. |
| HA hôte | OK. |
| Produit de passerelle | OK (rôle fixé). |
| Outils PostgreSQL de réplication/sauvegarde/bascule | **Risque** : l'adressage du primaire après bascule touche la configuration des services ; à sortir du report ou à garder comme question ouverte explicite. |
| Registre de schémas, Debezium | OK (JSON Schema dans le dépôt, outbox applicatif). |
| SIEM | OK. |
| Visualisation des traces | OK. |
| Granularité des lits par spécialité | OK (changement de schéma en `.v2`). |
| Découpage régional / stockage embarqué | OK (derrière port). |
| PostgreSQL 19 | OK. |
| Versions exactes (Prometheus, Cucumber, Testcontainers, Playwright, JMeter, tag OSRM) « dans l'epic socle » | **Risque** : ambigu (epic 1 `epic-socle-plateforme` de la PoC, ou initiative socle de production ?). Ces outils servent dès la PoC ; si l'épinglage est repoussé au socle de production, chaque epic PoC choisira sa version. Préciser « épinglées dans l'epic 1 de la PoC (socle plateforme) ». |

### Technologies nommées — actualité

Vérifications reprises du memlog (non refaites en ligne dans cette revue) :

- Java 25 LTS, Kafka 4.4.0, Keycloak 26.8.2, PostgreSQL 18, Grafana 13.x, Graylog 7.1.x, CloudEvents 1.0, `eclipse-temurin:25-jre` : cohérents et sourcés.
- **Spring Boot 4.1.x** : le memlog note 4.2 en RC au 2026-08-20 ; le rythme habituel place une GA en novembre. Revérifier si 4.2 est sortie avant d'épingler.
- **Angular 22.x** : le rythme semestriel d'Angular fait attendre une majeure vers novembre ; le memlog annonce « prochaine majeure juin 2027 ». Revérifier.
- **OSRM 26.x** : numérotation vue via le paquet npm, tag d'image non confirmé ; acceptable puisque l'épinglage est prévu, mais OSRM sert dès la PoC (voir report ci-dessus).
- Cucumber-JVM, Testcontainers, Playwright, JMeter, Prometheus 3.x : « dernières versions stables » n'est pas une version ; acceptable seulement si l'épinglage tombe dans l'epic 1 de la PoC.

---

## 3. Dimensions de l'altitude — décidé, reporté ou ouvert ?

| Dimension | Statut |
| --- | --- |
| Découpage, propriété des données | Décidé. |
| Communication (synchrone / asynchrone) | Décidé. |
| Contrats (REST, événements) | Décidé. |
| Sécurité (identité, autorisation, transport) | Décidé, avec F1. |
| Protection des données / RGPD | Partiel — F3. |
| Observabilité | Décidé, acheminement Graylog et routage des alertes ouverts. |
| Tests | Décidé. |
| CI | Partiel — F7. |
| Documentation | **Silencieux** — F7. |
| Données de référence et jeux de données factices | Partiel — F5. |

### 3.6 Enveloppe opérationnelle et environnementale

| Dimension | Statut | Proposition |
| --- | --- | --- |
| Déploiement et environnements | Partiel : Compose mono-hôte PoC et socle ; aucun CD. La liste des environnements n'est pas dite (« identique dans tous les environnements » : lesquels ? poste local, CI, hôte de démonstration du socle ?). | Une ligne : environnements = poste local et CI ; socle démontré sur un hôte unique désigné (question ouverte : quelle machine). |
| Infra / fournisseur | Reporté (hébergeur dans Deferred). OK. | — |
| Opérations (démarrage, mise à jour, rétention, procédures) | Partiel : démarrage en une commande implicite (profils). Rétention des topics non compactés et des journaux Graylog, procédure de montée de version non dites. | Rétention des topics non compactés (ex. 7 jours) et des journaux ; reste reportable au socle. |
| Gestion des secrets | Partiel : « sans secret embarqué », `.env` non versionné. Non dit : contenu de l'export du realm (utilisateurs fictifs avec mots de passe de démonstration ? acceptable mais à écrire), secrets GitHub Actions (GHCR via `GITHUB_TOKEN`), secrets du socle (Docker secrets vs variables), certificat TLS. | Ajouter une convention « Secrets ». |
| Sauvegarde | Reporté (outils) pour PostgreSQL. Keycloak (sa base) et Kafka (données rejouables ? source de vérité = PostgreSQL) non mentionnés. | Écrire : PostgreSQL est la seule source de vérité à sauvegarder ; Kafka se reconstruit depuis l'outbox / un rejeu ; base Keycloak sauvegardée ou realm réimporté. |
| Stockage de Keycloak | **Silencieux** (dev H2 en PoC ? PostgreSQL dédié dans le socle ?). | Décider : base propre à Keycloak, jamais celle de `hospital-registry` (cohérent avec AD-13). |

---

## 4. Concision

Le spine reste majoritairement terse : règles au présent, peu de justification. Écarts mineurs :

- Deferred : « dimensionnement national estimé à quelques dizaines de Mo » et les conditions de reprise sont de la justification ; acceptable mais pourrait rester au memlog.
- AD-5 « Une recommandation est une proposition ; la confirmation fait foi » : c'est une règle, à garder.
- Les lignes **Prevents** jouent bien leur rôle sans dériver en argumentaire.

Pas de réécriture nécessaire pour la concision.

---

## 5. Corrections proposées, par ordre de priorité

1. **AD-10 / AD-11 (F1)** : Keycloak servi derrière nginx sur la même origine (`/auth`), hostname/issuer unique configuré, mis à jour dans le seed et la convention Compose.
2. **Nouvelle règle dans AD-5 ou AD-6 (F2)** : publication de `bed-reservation-requested` sans base — `acks=all`, délai borné, comportement en cas d'échec fixé (code et `type` Problem Details).
3. **AD-4 / AD-15 (F3)** : charge utile des événements sans coordonnées ; rétention des topics non compactés ; pas de coordonnées dans les attributs de trace ni dans les journaux d'accès nginx (le corps n'y est pas, mais le dire).
4. **AD-8 (F4)** : distance (`annotations=distance`) vs durée ; préparation des données OSRM dans une image publiée, réutilisée en CI et Testcontainers ; délai et coefficient configurables avec défaut.
5. **AD-3, AD-13, AD-15 (F5)** : Kafka 3 nœuds et RF/min ISR visés dans le socle ; déclaration des topics ; seed Flyway et mode d'injection des 2 000 hôpitaux ; base propre à Keycloak ; acheminement des journaux vers Graylog et alertes simples.
6. **AD-4** : portée du champ `version` (par topic ou partagée) ; idempotence de la réservation sur `recommendationId` ; reformuler « état complet ».
7. **AD-14** : configuration à l'exécution du front ; secrets du socle.
8. **Conventions (F7)** : Documentation (Javadoc, Markdown, readme par sections d'epic), CI (push/PR, étapes, artefacts de rapports), Topics, Rôles IAM ; lier poc-R10.
9. **Deferred** : préciser que les versions d'outils sont épinglées dans l'epic 1 de la PoC ; sortir l'adressage du primaire PostgreSQL du report (ou en faire une question ouverte) ; revérifier Spring Boot 4.2 et Angular 23.
10. **AD-2 / AD-17** : test ArchUnit obligatoire par service.
