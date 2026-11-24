---
review: versions-et-réalité-des-technologies
cible: architecture-meadhead-plateforme.md (status draft, 2026-11-23)
date: 2026-11-23
relecteur: relecteur indépendant (lentille « vérifié sur le web ou affirmé de mémoire »)
---

# Revue — versions et réalité des technologies

## Verdict

Le socle est globalement solide : les composants d'infrastructure (Kafka 4.4.0, Keycloak 26.8.2, PostgreSQL 18, Angular 22, Grafana 13, Graylog 7.1, OSRM sur GHCR) ont été vérifiés sur le web et le memlog le trace. En revanche, plusieurs choix **côté Java/outillage** reposent sur la mémoire d'entraînement et non sur une vérification : ils sont plausibles, mais deux d'entre eux sont déjà inexacts ou incomplets pour Spring Boot 4 (JUnit 5, génération OpenAPI), et la ligne Spring Boot elle-même risque d'être dépassée dans les jours qui viennent. Il faut corriger avant de passer en `status: final`.

## Méthode

- Lecture du spine et du `.memlog.md` (entrées `version`).
- 8 recherches web ciblées le 2026-11-23 : openapi-generator et Spring Boot 4, Angular 22 et Vitest, Prometheus 3.x, Spring Boot 4 et OpenTelemetry/Micrometer Tracing, Spring Boot 4.2 GA, image `eclipse-temurin:25-jre`, JMeter, Testcontainers 2.x et Keycloak.
- Pas de dépôt de code ni de starter existant (greenfield) : aucune valeur par défaut de starter n'a pu être confrontée au projet ; le spine n'invoque d'ailleurs aucun générateur de projet (Spring Initializr, `ng new`), ce qui est en soi une lacune signalée plus bas.

## Tableau de vérification

| Élément du spine | Vérifié par l'auteur ? | Contrôle de cette revue | Statut |
| --- | --- | --- | --- |
| Java 25 LTS | oui (memlog) | Spring Boot 4.x supporte Java 17 à 25 | OK |
| Spring Boot 4.1.x | oui, mais point du 2026-08-20 (4.1.1) | 4.2.0 en RC1 depuis le 19/10/2026, GA annoncée pour **novembre 2026** ; pas de GA trouvée au 23/11 | **À revérifier (F1)** |
| Angular 22.x | oui | Vitest est le runner par défaut depuis Angular 21, conservé en 22 (builder `@angular/build:unit-test`) | OK |
| Vitest (AD-17) | non | confirmé (ci-dessus) | OK |
| JUnit 5 (AD-17) | non | Spring Boot 4 embarque **JUnit Jupiter 6** | **Inexact (F2)** |
| Génération serveur Spring depuis OpenAPI (AD-12) | non | openapi-generator ≥ 7.16.0 requis, options `useSpringBoot4` (+ `useJackson3`) ; `useSpringBoot3` déprécié | **Non spécifié (F3)** |
| Micrometer Tracing (OpenTelemetry) (AD-15) | non | Spring Boot 4 fournit `spring-boot-starter-opentelemetry` (starter stable, dépendances en `-alpha`) et le module `spring-boot-micrometer-tracing-opentelemetry` ; export OTLP actif par défaut | Existe, **réglage manquant (F4)** |
| Journaux JSON (AD-15) | non | Spring Boot 4 a la journalisation structurée native (`ecs`, `logstash`, `gelf`) ; les noms de champs natifs ne sont pas `service`/`level`/`timestamp` | **Écart à trancher (F5)** |
| RFC 9457 Problem Details | non | `ProblemDetail` présent depuis Spring Framework 6, aligné RFC 9457 dans Spring 7 | OK (risque faible) |
| `eclipse-temurin:25-jre` | non | tag officiel existant ; variante OS par défaut = dernière Ubuntu (noble, et `resolute` apparaît) ; tags `-ubi10-minimal` aussi | OK, **épinglage à préciser (F7)** |
| Kafka 4.4.0 (KRaft) | oui | — | OK |
| CloudEvents 1.0, binding Kafka mode binaire | non | spécification du binding Kafka stable (en-têtes `ce_*`) ; **SDK Java CloudEvents non vérifié** vis-à-vis de Jackson 3 / Spring Boot 4 | **Risque (F6)** |
| Keycloak 26.8.2 | oui | — | OK |
| Testcontainers (Kafka, PostgreSQL, Keycloak, OSRM) | non | Testcontainers 2.0.x en usage ; Keycloak = module **communautaire** (`dasniko/testcontainers-keycloak`), pas officiel ; OSRM sans module (GenericContainer) ; pas de `@ServiceConnection` pour Keycloak | Faisable, **à expliciter (F8)** |
| PostgreSQL 18.x | oui | — | OK |
| Prometheus 3.x | non (question ouverte au memlog) | dernière 3.16.0 (05/11/2026, selon endoflife.date) ; ligne **LTS 3.13** | **À épingler (F9)** |
| Grafana 13.x / Graylog 7.1.x | oui | Graylog 7 suppose MongoDB + Data Node/OpenSearch, absents du spine | Note (F10) |
| JMeter | non | dernière stable **5.6.3 du 07/01/2024** ; 6.0 (Java 17 minimum) non publiée ; tests Java 25 en cours côté projet | **À préciser (F11)** |
| Playwright | non | non vérifié dans cette revue (outil très actif, risque faible) | Non vérifié |
| Cucumber-JVM en français | non | Gherkin `# language: fr` existe de longue date ; compatibilité moteur JUnit Platform 6 non vérifiée | Lié à F2 |
| nginx, Flyway, Maven | non, sans version | aucune version au tableau Stack | Note (F12) |

## Constats

### F1 — Ligne Spring Boot possiblement dépassée dans les jours qui viennent [moyen]

Le memlog fige « 4.1.x (4.1.1 au 2026-08-20 ; 4.2 en RC) ». Ce point date de trois mois : 4.2.0-RC1 est sorti mi-octobre 2026 et InfoQ annonce la GA de 4.2 pour novembre 2026, soit autour d'aujourd'hui. Je n'ai pas trouvé d'annonce GA au 23/11, mais le spine ne dit pas *pourquoi* rester en 4.1 (contrairement au raisonnement explicite fait pour PostgreSQL 19). La version de patch citée (4.1.1) est certainement dépassée.

**Correctif :** revérifier spring.io/blog le jour de l'épinglage ; noter dans le spine une règle explicite, par ex. « 4.1.x jusqu'au premier correctif 4.2.1, puis montée » (symétrique de la règle PostgreSQL 19), et vérifier la fin du support OSS de 4.1.

### F2 — « JUnit 5 » est inexact avec Spring Boot 4 [moyen]

AD-17 et le memlog écrivent « unitaires JUnit 5 ». Spring Boot 4 gère JUnit Jupiter 6. Écrire JUnit 5 pousse un agent à forcer une version 5.x (conflit de BOM) ou à suivre de la documentation obsolète. Corollaire : la compatibilité de `cucumber-junit-platform-engine` (Cucumber-JVM 7.x) avec la JUnit Platform 6 doit être confirmée.

**Correctif :** remplacer par « JUnit Jupiter (version gérée par Spring Boot, 6.x) » et vérifier la version de Cucumber-JVM compatible avec JUnit Platform 6 avant l'epic tests.

### F3 — Génération OpenAPI côté Spring non spécifiée pour Spring Boot 4 [élevé]

AD-12 est une règle forte (« interfaces serveur Spring et client TypeScript générés ») mais l'outil n'est nommé nulle part. Avec Spring Boot 4, openapi-generator doit être en **7.16.0 ou plus** avec `useSpringBoot4=true` (qui active `useJakartaEe`) et, selon le choix Jackson, `useJackson3=true` ; l'ancien `useSpringBoot3` est déprécié. Sans cela, un agent produira du code Spring Boot 3 / Jackson 2 qui compilera mal ou tirera Jackson 2 en parallèle. Côté Angular 22, le générateur `typescript-angular` doit aussi être compatible avec la version d'Angular visée (non vérifié).

**Correctif :** ajouter au tableau Stack « openapi-generator (plugin Maven) ≥ 7.16, `spring` avec `interfaceOnly`, `useSpringBoot4`, `useJackson3` » et « générateur `typescript-angular` compatible Angular 22 » ; vérifier la dernière version sur les notes de version.

### F4 — Traçage : export OTLP par défaut sans collecteur [moyen]

Micrometer Tracing avec le pont OpenTelemetry existe bien en Spring Boot 4 (et un `spring-boot-starter-opentelemetry` officiel). Mais le spine reporte la plateforme de traces au socle : en PoC, rien ne reçoit l'OTLP. Le comportement par défaut tentera un export vers `localhost:4318` et produira des erreurs de journal ou une charge inutile pendant la mesure JMeter.

**Correctif :** préciser dans AD-15 « traceId propagé et journalisé ; export OTLP désactivé en PoC » et nommer la dépendance exacte (starter OpenTelemetry ou `micrometer-tracing-bridge-otel`), vérifiée sur la documentation 4.1.

### F5 — Champs de journal personnalisés contre journalisation structurée native [moyen]

Spring Boot 4 sait émettre du JSON nativement (`logging.structured.format.console=ecs|logstash|gelf`), y compris **GELF**, le format natif de Graylog. Les champs imposés par AD-15 (`service`, `level`, `traceId`, `timestamp`) ne correspondent à aucun de ces formats (ECS : `service.name`, `log.level`, `@timestamp`). La décision n'a pas été confrontée à ce que fournit le framework.

**Correctif :** soit adopter un format natif (ECS, ou GELF pour Graylog) et aligner la convention sur ses noms, soit assumer un formateur personnalisé et le dire. La première option est moins coûteuse pour les agents.

### F6 — SDK CloudEvents Java non vérifié avec Jackson 3 [moyen]

Le binding Kafka de CloudEvents 1.0 est stable, mais l'implémentation passera par le SDK Java (`cloudevents-kafka`, `cloudevents-json-jackson`, éventuellement `cloudevents-spring`). Ces modules ont historiquement dépendu de Jackson 2 ; Spring Boot 4 bascule sur Jackson 3. Ce point n'a pas été vérifié.

**Correctif :** vérifier la dernière version du SDK CloudEvents Java et sa compatibilité Spring Boot 4 / Jackson 3 ; à défaut, consigner « en-têtes `ce_*` posés à la main par l'adaptateur Kafka » (simple en mode binaire).

### F7 — Image de base : variante OS non épinglée [faible]

`eclipse-temurin:25-jre` existe. Sans suffixe, l'OS suit la dernière Ubuntu et peut basculer (noble vers resolute) entre deux builds, ce qui contredit l'esprit d'AD-14 (« images identiques »).

**Correctif :** épingler la variante (`25-jre-noble` ou un tag de patch `25.0.x_y-jre-noble`) et, idéalement, par digest dans le Dockerfile.

### F8 — Testcontainers : modules réels à expliciter [faible]

Testcontainers 2.x a renommé les artefacts de modules ; Keycloak n'a qu'un module communautaire ; OSRM demande un `GenericContainer` avec l'extrait OSM prétraité (lourd en CI : temps de préparation à prévoir). La règle AD-17 reste réalisable mais un agent devra deviner.

**Correctif :** mentionner « Testcontainers 2.x (BOM), module communautaire `testcontainers-keycloak`, OSRM via GenericContainer sur un mini-extrait prétraité versionné ».

### F9 — Prometheus 3.x non épinglé alors qu'une ligne LTS existe [faible]

La question est ouverte dans le memlog et le tableau dit « 3.x ». Les sorties sont mensuelles (3.16.0 le 05/11/2026) avec une ligne LTS (3.13).

**Correctif :** choisir la LTS 3.13.x pour le socle de production, ou la dernière mineure pour la PoC, et l'écrire dans le tableau Stack.

### F10 — Dépendances implicites de Graylog 7 [faible]

Graylog 7.1 suppose MongoDB et un stockage de recherche (Graylog Data Node / OpenSearch). Sur un seul hôte Compose, c'est l'ensemble le plus gourmand de la pile. Hors périmètre PoC, mais à poser dans le spine du socle.

**Correctif :** ajouter MongoDB et Data Node à la ligne Graylog (socle) avec une note de dimensionnement mémoire.

### F11 — JMeter : la « dernière stable » date de janvier 2024 [faible]

JMeter 5.6.3 reste la dernière version stable (07/01/2024) ; 6.0 n'est pas publiée. Elle tourne sur Java 8+ (17+ recommandé) ; l'exécution sur Java 25 est en cours de test côté projet. JMeter étant l'instrument de la mesure contractuelle de poc-R8, l'ambiguïté « dernière version stable » mérite d'être levée.

**Correctif :** épingler JMeter 5.6.3 exécuté sur un JRE 21 (image dédiée au profil `load`), plutôt que sur le JRE 25 des services.

### F12 — Composants sans version au tableau Stack [faible]

nginx (point d'entrée et TLS), Flyway, Maven, Playwright, Cucumber-JVM et Testcontainers n'ont pas de version. Pour Flyway, la version est gérée par Spring Boot, mais le module `flyway-database-postgresql` est requis et le support de PostgreSQL 18 doit être confirmé.

**Correctif :** ajouter au tableau « géré par le BOM Spring Boot 4.1 » pour Flyway, Testcontainers et JUnit ; épingler nginx (ligne stable) et Maven (3.9.x ou 4.x, à vérifier) ; Playwright à la dernière version au moment de l'epic E2E.

## Points vérifiés et conformes

- Vitest comme runner par défaut d'Angular 22 (depuis Angular 21).
- Spring Boot 4.x avec Java 25.
- Existence de l'image `eclipse-temurin:25-jre`.
- Micrometer Tracing avec pont OpenTelemetry disponible et documenté en 4.1.
- Infrastructure vérifiée par l'auteur (Kafka, Keycloak, PostgreSQL, Grafana, Graylog, OSRM) : rien ne contredit le memlog.

## Sources (recherches du 2026-11-23)

- OpenRewrite, « Migrate OpenAPI Generator spring configuration to Spring Boot 4 » : https://docs.openrewrite.org/recipes/java/spring/boot4/migrateopenapigeneratortospringboot4
- openapi-generator, générateur spring : https://openapi-generator.tech/docs/generators/spring
- Angular, tests unitaires : https://angular.dev/guide/testing/unit-tests ; https://next.angular.dev/guide/testing
- Prometheus : https://endoflife.date/prometheus ; https://github.com/prometheus/prometheus/releases/
- Spring, « OpenTelemetry with Spring Boot » : https://spring.io/blog/2025/11/18/opentelemetry-with-spring-boot/ ; https://docs.spring.io/spring-boot/4.1/appendix/auto-configuration-classes/spring-boot-micrometer-tracing-opentelemetry.html
- Spring Boot 4.2 : https://spring.io/blog/2026/09/25/spring-boot-4-2-0-M2-available-now/ ; https://infoq.com/news/2026/10/spring-news-roundup-oct19-2026 ; https://infoq.com/news/2026/06/spring-boot-4-1
- Eclipse Temurin : https://hub.docker.com/_/eclipse-temurin/tags ; https://adoptium.net/installation/containers
- JMeter : https://jmeter.apache.org/changes ; https://whimsy-vm9.apache.org/board/minutes/JMeter.html
- Testcontainers et Keycloak : https://testcontainers.com/guides/securing-spring-boot-microservice-using-keycloak-and-testcontainers/ ; https://www.github.com/dasniko/testcontainers-keycloak
