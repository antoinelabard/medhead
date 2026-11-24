---
type: review
lens: adversarial (unités conformes mais incompatibles)
target: _bmad-output/initiative-meadhead-plateforme/architecture-meadhead-plateforme/architecture-meadhead-plateforme.md
units: epic-socle-plateforme (1), epic-recommandation-hopital (2), epic-page-recherche (3), epic-tenue-en-charge (4), epic-documentation-reporting (5), initiative-socle-production
date: '2026-11-23'
---

# Revue adverse — spine d'architecture de la plateforme MedHead

## Verdict

Le paradigme tient (propriété des données, bus, outbox, hexagonal), mais la spine laisse encore ouvertes plusieurs coutures où deux epics peuvent respecter chaque AD à la lettre et produire un système qui ne fonctionne pas. Les trois plus graves sont les suivantes. Le cycle de réservation (AD-5) vide les lits pendant le test de charge et les tests E2E répétés. Un compteur `version` unique pour deux topics compactés fait perdre des profils d'hôpitaux au rejeu. Rien ne dit qu'une instance lit *toutes* les partitions sans groupe de consommateurs. Suivent l'identité (émetteur/audience du jeton), la publication de `bed-reservation-requested` par un service sans base, et la création des topics. Chaque constat ci-dessous décrit deux unités conformes qui s'opposent, puis le texte minimal d'AD ou de convention qui ferme la faille.

Échelle : **Critique** = le Done when d'un epic ou de l'initiative devient inatteignable ; **Haute** = intégration cassée découverte tard ; **Moyenne** = divergence probable, réparable localement ; **Basse** = ambiguïté à fixer par prudence.

---

## C1 — Critique — L'amorçage des données et la décrémentation des lits rendent le test de charge et les tests répétés incohérents

**Unités en conflit :** epic 2 (amorçage du scénario, cycle de réservation) × epic 4 (2 000 hôpitaux, 800 req/s) × epic 3 (E2E rejoué à chaque CI).

**Construction conforme mais incompatible :**

1. *Chemin d'amorçage.* L'epic 2 amorce les trois hôpitaux du scénario par une migration Flyway `INSERT INTO hospital …`. C'est conforme à AD-13 (Flyway) et à AD-1 (seul `hospital-registry` écrit). Mais un `INSERT` SQL n'écrit aucune ligne `outbox`, donc aucun événement ne part : les topics compactés restent vides et la copie locale aussi. AD-7 laisse l'instance devenir prête quand même, puisque « relire » un topic vide se termine immédiatement. L'epic 4, lui, génère ses 2 000 hôpitaux dans `tests/load/`, comme le dit l'arborescence, et les publie directement sur `medhead.hospitals.*` avec des UUID qu'il fabrique lui-même. Rien n'interdit explicitement à un outil de test de produire sur un topic, mais le registre n'a alors aucune ligne pour ces hôpitaux, et chaque `bed-reservation-requested` sur l'un d'eux est rejeté ou fait planter le consommateur. La convention « `hospitalId` généré par `hospital-registry` » est violée sans qu'aucune AD ne nomme le chemin légitime.
2. *Épuisement des lits.* AD-5 impose qu'une recommandation réussie publie une réservation que le registre décrémente. À 800 req/s, un hôpital est vidé en quelques secondes. La copie locale suit (`hospital-capacity-changed`), puis la réponse devient `no-hospital-available`. JMeter mesure alors des Problem Details rapides : le p95 ne prouve plus rien, ou bien le taux d'erreur dépasse 1 %. Si l'epic 4 « optimise » avec une décrémentation locale provisoire dans la copie, il crée un second écrivain de l'état des lits, contraire à l'esprit d'AD-1, sans qu'aucune AD ne l'interdise noir sur blanc.
3. *Tests répétés.* Fred Brooks a 2 lits (`specs/Exigences…`, scénario). Le troisième passage du scénario BDD (epic 2) ou E2E (epic 3) sur le même environnement renvoie « aucun hôpital ». Chaque epic peut choisir sa propre parade (reset de la base, restart, lits gonflés), et elles se contredisent.

**Texte proposé (nouvelle AD-18 « Données factices et amorçage ») :**

> Tout hôpital, y compris factice, entre dans le système par `hospital-registry`, au moyen d'un port d'import unique (`HospitalImportPort`) qui écrit hôpital et ligne `outbox` dans la même transaction. Aucun `INSERT` d'hôpital hors de ce port, aucun outil ne produit sur `medhead.hospitals.*` ou `medhead.beds.*` hormis `hospital-registry`. Les jeux de données sont des fichiers versionnés dans `contracts/fixtures/` (`scenario.json` : les trois hôpitaux du scénario ; `load-2000.json` : produit par le générateur de `tests/load/`, avec graine fixe), sans `hospitalId` (le registre l'attribue, de façon déterministe à partir d'une clé métier `code` pour être rejouable). Le jeu chargé au démarrage est choisi par `MEDHEAD_HOSPITAL_REGISTRY_FIXTURES` (`scenario` par défaut, `scenario,load-2000` pour le profil `load`). En profil `load` et en test, `MEDHEAD_HOSPITAL_REGISTRY_BED_REPLENISH=true` remet la capacité initiale de chaque hôpital après chaque confirmation, si bien que la décrémentation et ses événements ont bien lieu mais que l'état reste stable. Les tests BDD et E2E rechargent le jeu `scenario` (endpoint d'administration réservé au profil test, ou conteneurs neufs) avant chaque scénario qui consomme un lit. La copie locale n'est jamais modifiée hors des événements reçus.

Il faut aussi resserrer AD-7 : « prête » suppose d'avoir relu les topics *et* qu'ils ne soient pas vides quand des fixtures sont configurées (voir C3).

---

## C2 — Critique — Un `version` « monotone par hôpital » partagé entre deux topics compactés efface des profils au rejeu

**Unités en conflit :** producteur `hospital-registry` × consommateur `recommendation-service` (epic 2 au départ, mais l'epic 4 réécrira le consommateur pour la performance, et l'initiative socle ajoutera des instances).

**Construction conforme mais incompatible :** AD-4 dit « événements d'état portent l'état complet de l'hôpital et un champ `version` monotone par hôpital ; un consommateur ignore toute version ≤ à celle qu'il détient ». Les conventions séparent pourtant l'état en deux topics, `hospital-profile-changed` (nom, position, spécialités) et `hospital-capacity-changed`. Le producteur, conforme, prend la colonne `@Version` JPA de la ligne hôpital : un seul compteur pour le profil et la capacité. Le consommateur, conforme, garde une version par hôpital. Au démarrage, il relit les deux topics en parallèle, sans ordre entre topics : il reçoit `capacity v7`, puis `profile v3`, et ignore le profil. L'hôpital n'a plus ni position ni spécialité, et la recommandation l'écarte en silence. À l'inverse, un producteur qui tient deux compteurs indépendants face à un consommateur qui n'en garde qu'un écrase la capacité par un profil plus « récent ». De plus, « état complet de l'hôpital » contredit le découpage en deux topics : est-ce que `capacity-changed` porte aussi le nom ? Le choix de l'horodatage comme version (horloge murale) casse dès que le socle met deux instances de registre.

**Texte proposé (remplacer la phrase d'AD-4) :**

> Chaque topic d'état porte l'état complet *de son agrégat* : `hospital-profile-changed` = `{hospitalId, version, name, latitude, longitude, specialties[]}` ; `hospital-capacity-changed` = `{hospitalId, version, availableBeds}`. `version` est un entier 64 bits strictement croissant **par (hôpital, topic)**, issu d'une colonne de base dédiée (`profile_version`, `capacity_version`) incrémentée dans la transaction métier, jamais d'une horloge. Le consommateur conserve une version par (hôpital, topic) et fusionne les deux vues ; un hôpital n'est candidat qu'une fois ses deux vues reçues. Une suppression est un tombstone (valeur nulle) sur les deux topics.

Ajouter dans `contracts/events/` une règle de nommage des champs : camelCase, mêmes noms que l'OpenAPI pour `latitude`, `longitude`, `availableBeds` et `specialties` (codes de `specialties.json`).

---

## C3 — Critique — « Relire les topics compactés depuis le début » ne dit ni quelles partitions, ni quand on est « à jour »

**Unités en conflit :** epic 2 (une instance) × initiative socle (plusieurs instances derrière la passerelle) × epic 4 (redémarrages pendant la mesure).

**Construction conforme mais incompatible :** l'epic 2 utilise naturellement `@KafkaListener` avec un `group.id` fixe, `recommendation-service`, et `auto.offset.reset=earliest`. C'est conforme à AD-3 au premier démarrage. Au redémarrage, les offsets validés font repartir de la fin : la copie est vide alors que l'instance est « prête », puisqu'elle n'a plus rien à relire. Dans le socle, deux instances du même groupe se *partagent* les partitions, et chacune ne voit qu'une partie des hôpitaux : les recommandations diffèrent selon l'instance qui répond, ce qui contredit directement l'intention d'AD-9. La readiness d'AD-7 (« une fois les topics relus ») peut se lire de plusieurs façons : premier `poll` vide, délai fixe, offsets de fin atteints. Chaque lecture est conforme, et seule la troisième est correcte.

**Texte proposé (ajouter à AD-7) :**

> La copie locale lit les topics compactés par assignation manuelle de **toutes** leurs partitions (`assign`, pas `subscribe`), sans `group.id` partagé ni validation d'offsets, en partant de l'offset le plus ancien à chaque démarrage. « À jour » signifie que, pour chaque partition, la position lue a atteint l'offset de fin relevé au démarrage (`endOffsets`) ; la readiness passe alors à `UP`. Le topic des demandes de réservation est, lui, consommé par `hospital-registry` en groupe `hospital-registry` avec validation d'offsets après commit en base. Le retard de consommation alerté est `endOffset − position` par partition, publié en `medhead_local_view_lag_records`.

---

## H1 — Haute — Émetteur, audience, rôle et clients Keycloak non fixés : 401 sur tous les appels au moment d'intégrer

**Unités en conflit :** epic 2 (protection de l'API, Keycloak factice) × epic 3 (jeton navigateur) × epic 4 (jeton JMeter sur le réseau `backend`) × epic 1 (nginx, seul port publié).

**Construction conforme mais incompatible :**
- *Émetteur.* Le navigateur obtient un jeton à `https://localhost/…/realms/medhead`, donc `iss` public. JMeter, « directement sur le réseau backend » (AD-16), l'obtient à `http://keycloak:8080/realms/medhead`, donc `iss` interne. L'epic 2 valide `iss` contre une seule URL, et l'une des deux populations reçoit toujours 401. Le Structural Seed montre en outre le navigateur → Keycloak en direct, ce qui suppose un port publié, alors qu'AD-11 n'en autorise qu'un, celui du point d'entrée.
- *Rôle.* L'epic 2 lit `realm_access.roles` ; le realm exporté par un autre epic définit `emergency-dispatcher` comme rôle *client* (`resource_access.<client>.roles`). Résultat : 403 partout.
- *Audience.* Keycloak met `aud: account` par défaut. Un service qui vérifie `aud = medhead-api`, ce qui est une bonne pratique, rejette tous les jetons.
- *Durée de vie.* « Un jeton obtenu une fois en début de test » : avec la durée de 5 min par défaut, toute mesure plus longue s'effondre en 401.

**Texte proposé (compléter AD-10, et la convention Keycloak) :**

> Realm `medhead`, versionné dans `infra/keycloak/medhead-realm.json`, seul fichier de configuration IAM (propriété de l'epic 2). Keycloak est servi derrière le point d'entrée sous `/auth`, avec `KC_HOSTNAME=https://<hôte>/auth`, si bien que `iss` vaut toujours l'URL publique, y compris pour un jeton demandé sur le réseau `backend`. Les services vérifient `iss` contre `MEDHEAD_<SERVICE>_JWT_ISSUER` (URL publique) et récupèrent les JWKS par `MEDHEAD_<SERVICE>_JWT_JWKS_URI` (URL interne). Clients : `medhead-frontend` (public, PKCE S256, redirections vers l'origine du point d'entrée) et `medhead-load-test` (confidentiel, `client_credentials`, compte de service portant le rôle, profil `load` seulement). Audience `medhead-api` ajoutée par un mapper et vérifiée par chaque service. Rôles = rôles de realm, lus dans `realm_access.roles`. Durée du jeton `medhead-load-test` ≥ durée maximale d'une campagne JMeter (30 min).

---

## H2 — Haute — `recommendation-service` n'a pas de base, donc pas d'outbox : la publication de `bed-reservation-requested` n'a pas de règle

**Unités en conflit :** epic 2 (« événement observé à chaque recommandation, jamais sans ») × epic 4 (« les optimisations nécessaires font partie de cet epic ») × R13 (panne d'un service factice).

**Construction conforme mais incompatible :** AD-6 ne vise que « un service qui possède une base ». L'epic 2 publie en synchrone avec `acks=all` et attend l'accusé avant de répondre : correct, mais il ajoute la latence Kafka au chemin critique. L'epic 4, pour tenir 200 ms, passe à un `send` asynchrone sans attente. Une recommandation est alors renvoyée alors que l'événement peut être perdu, ce qui viole R2 sans violer aucune AD. Quand Kafka est coupé (R13), l'un renvoie 503, l'autre 200 sans événement, et les deux se disent conformes. Le contrat de réponse qui en découle (code, `type` Problem Details) diverge entre l'OpenAPI et le front.

**Texte proposé (nouvelle règle dans AD-5) :**

> `recommendation-service` publie `bed-reservation-requested` avec un producteur idempotent (`enable.idempotence=true`, `acks=all`) et attend l'accusé dans un budget borné (`MEDHEAD_RECOMMENDATION_SERVICE_PUBLISH_TIMEOUT`, 50 ms par défaut) avant de répondre. En cas d'échec ou de dépassement, la réponse est `503` Problem Details `urn:medhead:problem:reservation-unavailable` et aucune recommandation n'est renvoyée. Une recommandation renvoyée implique un événement accusé par Kafka. Aucun envoi « fire-and-forget ».

---

## H3 — Haute — Création, partitionnement et compaction des topics sans propriétaire

**Unités en conflit :** epic 1 (Compose, Kafka) × epic 2 (producteurs, consommateurs, Testcontainers) × epic 4 × socle.

**Construction conforme mais incompatible :** l'epic 2 laisse l'auto-création, activée par défaut sur l'image Apache. Le premier producteur crée `hospital-capacity-changed` en `cleanup.policy=delete` avec 1 partition. Au bout de 7 jours de rétention, la copie locale ne se reconstruit plus : AD-3 est violée par la configuration, pas par le code. Autre cas : l'epic 2 déclare des beans `NewTopic` (compactés, 3 partitions) dans le registre, tandis que l'epic 1 crée les topics avec un script d'init (6 partitions). Le premier arrivé gagne, et les tests Testcontainers tournent sur une troisième configuration. Changer le nombre de partitions après coup rompt l'ordre par clé `hospitalId` qu'AD-4 suppose.

**Texte proposé (convention + AD-3) :**

> `auto.create.topics.enable=false`. Les topics sont déclarés une seule fois dans `infra/kafka/topics.yaml` (nom, partitions, `cleanup.policy`, `min.insync.replicas`) et créés par le service Compose `kafka-init` avant tout service applicatif. Les tests Testcontainers créent leurs topics à partir du même fichier. Topics d'état (`hospital-profile-changed`, `hospital-capacity-changed`) : `cleanup.policy=compact`, 6 partitions. Topics de réservation : `delete`, rétention 7 jours, 6 partitions. Le nombre de partitions d'un topic ne change pas ; le changer impose un nouveau `.v<n>`. Aucun service ne crée de topic.

---

## H4 — Haute — Propriété de nginx, image du front, préfixe `/api` et exposition d'actuator

**Unités en conflit :** epic 1 (nginx et squelette Angular) × epic 2 (route `/api`) × epic 3 (« l'image Docker du front ») × socle (« nginx ne sert plus que le statique »).

**Construction conforme mais incompatible :**
- L'epic 3 produit une image `frontend` = nginx + Angular avec son propre `nginx.conf`, qui contient TLS et `location /api`. L'epic 1 a déjà `infra/nginx/` comme point d'entrée. On obtient deux nginx et deux configurations de proxy, et le socle ne peut pas « retirer le routage de nginx » sans reconstruire l'image du front, ce qui contredit AD-14 (même image partout).
- `location /api/ { proxy_pass http://recommendation-service:8080/; }` (barre finale) retire le préfixe : le service reçoit `/v1/recommendations`, alors que l'OpenAPI déclare `/api/v1/…`. JMeter, qui attaque le service en direct (AD-16), utilise un autre chemin que le front.
- `location /api/` proxifie aussi `/actuator/*` si le service sert tout sur un seul port : les sondes et métriques sont alors joignables depuis l'extérieur.

**Texte proposé (compléter AD-11) :**

> Le point d'entrée est le conteneur `edge` (configuration dans `infra/edge/`, propriété de l'epic 1, routes ajoutées par l'epic qui expose une API) : TLS, `/` → `frontend`, `/api/` → `recommendation-service` **sans réécriture de chemin**, `/auth/` → Keycloak. L'image `frontend` (propriété de l'epic 3) est un nginx non root qui ne sert que les fichiers statiques en HTTP sur 8080, sans TLS ni proxy ; le socle remplace `edge` par la passerelle sans toucher à `frontend`. Chaque service sert ses chemins complets `/api/v1/…`. Actuator écoute sur un port de gestion séparé (`8081`), jamais routé par le point d'entrée ; Prometheus et les healthchecks Compose l'utilisent sur `backend`.

---

## M1 — Moyenne — Extrait OSRM du Grand Londres contre des coordonnées hors de l'extrait, délai OSRM non borné, OSRM en CI

**Unités en conflit :** epic 2 (OSRM, BDD « route contre vol d'oiseau ») × epic 4 (2 000 hôpitaux « au Royaume-Uni », décision 2026-11-16) × epic 1 (CI).

**Construction conforme mais incompatible :** le générateur de l'epic 4 répartit ses hôpitaux sur le Royaume-Uni, ce qui est conforme à la décision produit. OSRM, limité au Grand Londres (AD-8), renvoie `null` ou accroche le point au bord de l'extrait. On obtient des distances fausses, un `NullPointerException`, ou un passage en mode dégradé sur la plupart des requêtes : le test mesure le chemin dégradé, pas le nominal. AD-8 ne fixe pas non plus le délai OSRM. L'epic 2 met 1 s, valeur Spring raisonnable, et l'epic 4 ne tient plus le p95. Enfin, AD-17 impose Testcontainers sur un vrai OSRM. Le prétraitement d'un extrait (`osrm-extract`, `osrm-contract`) prend des minutes : qui construit l'image de données, et où vit-elle ?

**Texte proposé (compléter AD-8) :**

> Toutes les coordonnées factices (fixtures du scénario, `load-2000`, requêtes JMeter) sont tirées dans la boîte englobante de l'extrait, publiée dans `contracts/fixtures/bbox.json`. Une requête hors boîte est refusée en `422` `urn:medhead:problem:location-out-of-area`. Une cellule `null` de la réponse OSRM `table` exclut le candidat ; si toutes sont nulles, on bascule en classement à vol d'oiseau avec `degraded: true`. Le délai OSRM est `MEDHEAD_RECOMMENDATION_SERVICE_ROUTING_TIMEOUT` (80 ms par défaut), avec disjoncteur. Les données OSRM prétraitées sont une image `ghcr.io/<org>/medhead-osrm-data:<date-extrait>`, construite par un workflow dédié déclenché manuellement et utilisée telle quelle par Compose et Testcontainers.

---

## M2 — Moyenne — Relais d'outbox : ordre par clé et stabilité de l'`id` CloudEvents

**Unités en conflit :** epic 2 (relais) × socle (registre redondant, plusieurs instances) × consommateurs idempotents (AD-4).

**Construction conforme mais incompatible :** le relais génère l'`id` CloudEvents au moment de l'envoi. Une nouvelle tentative produit alors un nouvel `id`, et l'idempotence sur l'`id` exigée par AD-4 ne déduplique plus rien : deux décrémentations pour une seule demande si un relais rejoue côté consommateur. Dans le socle, deux instances du registre relaient en parallèle avec `FOR UPDATE SKIP LOCKED`, un motif courant, et publient `bed-reservation-confirmed` et `rejected` dans le désordre pour un même hôpital. Ces événements n'ont pas de `version`, donc rien ne rattrape l'inversion. Enfin, l'idempotence du consommateur `hospital-registry` suppose un stockage durable des `id` traités, que la spine ne mentionne pas.

**Texte proposé (compléter AD-6) :**

> L'`id` CloudEvents est attribué à l'insertion dans `outbox` et stocké avec la ligne ; le relais le réutilise à chaque tentative. Le relais publie dans l'ordre de la séquence `outbox`, par un seul publieur actif (verrou consultatif PostgreSQL) ; il ne publie la ligne *n+1* d'une même clé qu'après l'accusé de *n*. Le producteur du relais est idempotent (`acks=all`). Un consommateur qui possède une base enregistre les `id` traités dans une table `processed_event` mise à jour dans la transaction métier.

---

## M3 — Moyenne — Forme et diffusion de `specialties.json`

**Unités en conflit :** epic 2 (validation, codes) × epic 3 (liste déroulante groupée) × fixtures du registre (spécialités des hôpitaux).

**Construction conforme mais incompatible :** R4 parle de « groupe et spécialité ». L'epic 2 écrit `[{code, label}]` ; l'epic 3 attend `[{group, specialties:[…]}]` pour afficher les groupes. L'epic 3 importe le fichier au build Angular, ce qui exige un contexte Docker à la racine du dépôt, alors que l'epic 1 a posé un contexte `frontend/`. Ou bien il appelle un endpoint `/api/v1/specialties` absent de l'OpenAPI. Une spécialité renommée après coup désaligne le front buildé et l'API déployée, deux images publiées séparément.

**Texte proposé (convention Spécialités) :**

> `contracts/reference/specialties.json` suit `contracts/reference/specialties.schema.json` : `[{groupCode, groupLabelFr, specialties:[{code, labelFr}]}]`. Le front l'obtient par `GET /api/v1/specialties` (déclaré dans l'OpenAPI, servi par `recommendation-service` depuis le fichier embarqué), jamais en l'embarquant dans son build. Les fixtures d'hôpitaux n'utilisent que des `code` de ce fichier, ce que vérifie un test de contrat. Les contextes Docker sont la racine du dépôt.

---

## M4 — Moyenne — Propriété du pipeline CI et images utilisées par l'E2E

**Unités en conflit :** epic 1 (pipeline) × epics 2 et 3 (jobs Testcontainers, Playwright) × AD-14 (images publiées sur `develop` et `main` seulement).

**Construction conforme mais incompatible :** l'E2E de l'epic 3 démarre la PoC par Compose avec les images GHCR, mais sur une PR ces images ne sont pas publiées (AD-14). L'E2E teste alors l'image `develop` précédente, pas le code de la PR, ou échoue faute d'image. Les trois epics éditent le même `ci.yml`, et les conflits se règlent à la main, sans ordre d'étapes imposé (R9 : tests, build, images).

**Texte proposé (convention CI) :**

> Un workflow `ci.yml` (propriété de l'epic 1) enchaîne les jobs `test` (matrice par module), `build-images` (charge les images en artefact, étiquette `sha-<court>`), `e2e` (Compose avec `MEDHEAD_IMAGE_TAG=sha-<court>` depuis ces artefacts) et `publish` (`develop` et `main` seulement). Un epic ajoute un module à la matrice ou un job dans un workflow réutilisable `.github/workflows/_<module>.yml`, sans réordonner les étapes. Rapports archivés sous `reports/<niveau>/`.

---

## M5 — Moyenne — Étiquette `instance` et nom de l'histogramme

**Unités en conflit :** epic 2 (instrumentation) × epic 4 (tableau de bord, alertes) × socle (supervision de tous les services).

**Construction conforme mais incompatible :** AD-15 demande l'étiquette `instance` sur les métriques métier. Le service la pose en tag commun Micrometer, mais Prometheus pose aussi `instance` (cible de scrape) et renomme celle de l'application en `exported_instance`. Le tableau de bord de l'epic 4 filtre sur l'une, les alertes du socle sur l'autre. Le nom de l'histogramme n'est pas fixé non plus : `http_server_requests_seconds` de Spring ou métrique `medhead_*` maison. L'alerte « p95 > 200 ms » peut donc viser une série absente.

**Texte proposé (AD-15) :**

> Les étiquettes `service` et `instance` sont posées par Prometheus (`job` = nom du service, `instance` = cible), jamais par l'application. La latence HTTP est `http_server_requests_seconds` (Micrometer) avec `slo=0.2` et histogramme publié ; alertes et tableaux de bord s'appuient sur ce nom et sur `uri="/api/v1/recommendations"`.

---

## B1 — Basse — Mode dégradé et copie locale périmée face à R13

**Unités en conflit :** epic 2 × epic 3 (affichage).

**Construction conforme mais incompatible :** AD-8 ne qualifie de `degraded` que la panne d'OSRM. Si `hospital-registry` est coupé, ce qui est aussi un « service factice » de R13, la copie locale continue à servir un état figé sans signal. Le front de l'epic 3 n'affiche donc jamais d'avertissement, alors que les réservations, non consommées, s'empilent. Le champ `degraded` n'a pas d'emplacement normé (racine de la réponse ou par candidat), ni de motif.

**Texte proposé :**

> La réponse de recommandation porte `degraded: boolean` et `degradedReasons: ["routing-unavailable" | "capacity-stale"]` à la racine. `capacity-stale` est levé quand le dernier événement de capacité reçu date de plus de `MEDHEAD_RECOMMENDATION_SERVICE_STALE_AFTER` (60 s, avec un battement périodique publié par le registre). Le front affiche tout motif présent.

## B2 — Basse — Réplica en lecture dans le cycle de réservation (socle)

AD-13 autorise les lectures sur réplica. Une implémentation conforme lit la capacité sur le réplica avant de décrémenter sur le primaire : le verrouillage optimiste échoue en boucle à cause du retard de réplication. **Texte :** « Toute lecture faite dans une transaction d'écriture, dont le cycle de réservation et l'outbox, va au primaire ; le réplica ne sert que les lectures sans écriture consécutive. »

---

## Points vérifiés sans faille

- Forme de la requête et de la réponse REST : l'OpenAPI est unique et générée des deux côtés (AD-12), et l'epic 2 en est le propriétaire évident. Pas d'incompatibilité tant que H4 fixe le chemin.
- Horodatage et format des dates : fixés (ISO-8601 UTC). Le risque d'horloge ne porte que sur `version`, traité en C2.
- Propriété de l'écriture des hôpitaux : fixée par AD-1. Seul le chemin d'amorçage manque (C1).
