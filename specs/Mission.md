Mission

Vous travaillez en tant que consultant en architecture logicielle dans une grande entreprise et êtes affecté à un nouveau client, MedHead Consortium.

MedHead

MedHead est un regroupement de grandes institutions médicales œuvrant au sein du système de santé britannique et assujetti à la réglementation et aux directives locales (NHS). Les organisations membres du consortium utilisent actuellement une grande variété de technologies et d'appareils. Ils souhaitent une nouvelle plateforme pour unifier leurs pratiques. La technologie Java est pour eux un socle technique fiable pour ce projet.

Au cours de votre première journée, vous avez participé à une réunion avec le comité d'architecture. Le comité s'attend à ce que la plateforme pallie les risques liés au traitement des recommandations de lits d'hôpitaux dans des situations d'intervention d'urgence. Après avoir examiné le document de définition de l'architecture du projet, vous avez proposé votre aide pour développer une preuve de concept (PoC) afin de convaincre le comité de la validation de l’architecture cible retenue.

Deux jours plus tard

Ash, membre du comité d’architecture, vous envoie un e-mail récapitulatif de la réunion apportant des informations sur les thèmes abordés :

De : Ash K  
À : Vous  
Objet : Résumé de la réunion

Bonjour !

Ravi de te compter dans l'équipe. Je suis heureux que tu aies proposé de t’occuper d’une preuve de concept (“Proof of Concept” en anglais, ou “PoC”) du système d'intervention d'urgence.

Nous y réfléchissons depuis un moment et j’ai pensé qu’il serait utile de te fournir une partie de notre analyse pour t’aider à démarrer.

J’ai ajouté le lien en pièce jointe du repository d’architecture, qui contient les éléments suivants :

    l'énoncé sommaire des travaux ;
    le document principes de l’architecture ;
    le document de définition de l'architecture ;
    les données de référence sur les spécialités NHS.

Chris Pike et moi avons rédigé un document (en pièce jointe) résumant les exigences pour la PoC afin que son périmètre soit clairement établi. Ça te donne nos attentes clés.

Il faudra créer deux repos :

    Un repo du code contenant : 
        le code de la PoC versionné ;
        le(s) fichier(s) de configuration de la chaîne de build d’intégration continue ;
        le code des tests implémentés ;
        un fichier README.
    Un repo d’architecture contenant : 
        tous les documents du repo fourni (voir ci-dessus) ;
        un nouveau document de reporting.

Le document de reporting te permettra de synthétiser et de justifier les résultats de ton travail afin de prouver sa conformité avec ce qui t’est demandé. Il doit inclure une justification des technologies utilisées, du respect des normes et des principes, ainsi que les résultats du bon fonctionnement de la PoC et les enseignements tirés du processus de développement du projet.

Une fois que la PoC aura validé les exigences, tu devras présenter tes conclusions au comité d’architecture.

Bienvenue à bord !

Ash K.  
Comité d’architecture  
Consortium

Pièces jointes

Repository d’architecture  
Document des exigences

Kara, un autre membre du comité d'architecture, a travaillé sur le calendrier du projet. Elle vous envoie un message via l'application de messagerie instantanée de l'entreprise.

Début de citation

Salut ! Bienvenue à bord. En fait, je voulais te parler du planning pour le travail d’architecture.

Le conseil d’administration et nos partenaires extérieurs sont inquiets à propos de la modification du système actuel d’intervention d’urgence. Nous avons donc réexaminé la feuille de route du projet (je te l’envoie) et nous avons décidé de faire de votre PoC une priorité.

Fin de citation

Quelques minutes plus tard...

Kara vous informe que les parties prenantes ont des craintes quant à la gestion des services d'urgence ; elles veulent être sûres à 100 % que le service fonctionnera correctement, même pendant les périodes de pics d’activité.  
Étapes

Plusieurs documents vous sont fournis pour compléter cette mission :

    L'énoncé sommaire des travaux contient un récapitulatif du travail à faire.
    Le document principes de l’architecture détaille les principes à respecter dans le cadre de cette mission.
    Le document de définition de l'architecture contient l’architecture cible à produire ainsi que ses spécificités (au-delà de la PoC).
    Les données de référence sur les spécialités NHS vous aidera à élaborer les fonctionnalités qui permettent de sélectionner les hôpitaux par spécialité.
    Les exigences pour le développement de la preuve de concept définissent les besoins techniques à respecter pour la PoC.

Parcourez ces documents au départ du projet, puis creusez dans chaque document quand il vous sera utile pour la réalisation d’une étape précise.

Avant de démarrer cette étape, je dois avoir :

    lu et analysé chaque élément de la mission, dont les documents fournis.

Une fois cette étape terminée, je devrais avoir :

    un repository GitHub pour le code du projet
    un repository GitHub pour les documents d’architecture du projet
    un environnement de développement opérationnel sur mon poste de travail

Recommandations

    Cloner le repository d’architecture existant et s’en servir comme base pour votre repository d’architecture ;
    Créer un repository GitHub pour le code ;
    Choisir un workflow Git adapté et le documenter dans le fichier readme.md. ;
    Mettre en place l’environnement technique nécessaire (JDK, IDE, etc.).

Points de vigilance

    Attention à ne pas tirer des conclusions erronées sur le travail à réaliser : assurez-vous d’avoir bien compris le document des exigences pour le développement de la preuve de concept.

Ressources : Les cours OpenClassrooms suivants servent à vous rappeler les tâches de cette étape :

    Gérez du code avec Git et GitHub ;
    Devenez un expert de Git et GitHub ;
    Mettez en place votre environnement front-end ;
    Le deuxième chapitre du cours Apprenez à programmer en Java.

Avant de démarrer cette étape, je dois avoir :

    un repository GitHub pour le code du projet
    un environnement de développement opérationnel sur mon poste de travail

Une fois cette étape terminée, je devrais avoir :

    un repository GitHub pour le code du projet complété avec :
        le code du back-end
        les tests du back-end (dont des tests de stress)
        un fichier readme.md mis à jour selon le développement back-end

Recommandations

    Utilisez un framework comme Spring pour créer le back-end de la PoC ;
    Protégez les données du patient en sécurisant le back-end ;
    Testez le back-end et ajoutez tout autre fichier utile aux tests, par exemple une collection Postman ;
    Utilisez un outil comme JMeter pour les tests de stress.
    Mettez à jour le fichier readme.md en indiquant comment tester et builder le back-end.

Points de vigilance

    Le calcul de distance doit correspondre à des distances réellement à parcourir et non à une distance dite « à vol d’oiseau ».
    Attention à respecter le périmètre de la preuve de concept et les principes imposés : 
        La PoC doit pouvoir s’inscrire dans une architecture microservice,
        Toute l’architecture n’est pas à développer. Les autres services peuvent être factices.

Ressources

    Créez une application Java avec Spring Boot ;
    Optimisez votre architecture Microservices ;
    Documentation JMeter.

Avant de démarrer cette étape, je dois avoir :

    un repository GitHub pour le code du projet
    un environnement de développement opérationnel sur mon poste de travail

Une fois cette étape terminée, je devrais avoir :

    un repository GitHub pour le code du projet complété avec :
        le code du front-end
        les tests du front-end
        un fichier readme.md mis à jour selon le développement front-end

Recommandations

    Choisissez le framework qui vous permettra de réaliser le front-end ;
    Implémentez un visuel simple mais fonctionnel qui sera interconnecté avec l’API du back-end ;
    Testez le front-end et ajoutez tout fichier utile aux tests ;
    Mettez à jour le fichier readme.md en indiquant comment tester et builder le front-end.

Points de vigilance

    Le framework front doit respecter le critère « utilisez l’un des frameworks Javascript/Typescript courants du marché : Angular, React, VueJS ».
    Ne pas dédier un temps non nécessaire à la création d’un visuel élaboré alors que ce n’est pas attendu dans le cadre de cette PoC.

Ressources : Selon votre choix du framework front-end, vous pouvez consulter quelques-uns des cours OpenClassrooms ci-dessous

    Apprenez à programmer avec JavaScript
    Testez vos applications front-end avec JavaScript 
    Débutez avec Angular 
    Complétez vos connaissances sur Angular 
    Perfectionnez-vous sur Angular 
    Débutez avec React 
    Créez une application web avec Vue.js

Avant de démarrer cette étape, je dois avoir :

    un repository GitHub pour le code du projet complété.

Une fois cette étape terminée, je devrais avoir :

    un repository GitHub avec le pipeline d’intégration CI/CD opérationnel et le fichier readme.md à jour.

Recommandations

    Utilisez un outil comme GitHub Actions, GitLab-CI ou Jenkins ; 
    Découpez votre pipeline en étapes distinctes (tests, build, etc.) ;
    Mettez à jour le fichier readme.md avec des explications détaillant le fonctionnement du pipeline. Le fichier readme.md doit décrire les points clés pour : 
        builder et exécuter la PoC,
        exécuter les tests,
        exécuter la chaîne de build,
        les règles du workflow utilisé pour le versionning.

Points de vigilance

    L’objectif n’est pas de mettre en production la PoC sur un service Cloud via le pipeline.

Ressources

    Mettez en place l'intégration et la livraison continues avec la démarche DevOps. 

Avant de démarrer cette étape, je dois avoir :

    un repository GitHub pour le code du projet complet.

Une fois cette étape terminée, je devrais avoir :

    un repository GitHub avec les documents d’architecture à jour et complété avec le document de reporting.

Recommandations

    Ayez une approche pédagogique en fournissant dans le document de reporting toutes les informations et justifications nécessaires pour prouver la validité du travail réalisé ;
    Justifiez les parties suivantes : 
        les technologies back-end et front-end utilisées, 
        le respect des normes, notamment le RGPD,
        les principes d’architecture exigées du projet ;
    Discutez des résultats et des enseignements de la PoC.

Points de vigilance

    Mettez-vous dans le rôle du chef de projet pour ce projet de développement - que feriez-vous ? 
        Pour la soutenance, vous devez préparer une explication de pilotage d'une équipe de développement dans le cadre de ce projet. Considérez comment vous soutiendriez une équipe au cours des différentes phases de développement.
    Les justifications sont arbitraires et ne reposent pas sur des arguments factuels ;
    Il est important de fournir des métriques quant au temps d’exécution de l’API afin de montrer le succès de la PoC ;
    Assurez-vous que le repository architectural contient tous les documents existants : 
        l’énoncé des travaux d'architecture TOGAF,
        le document des principes de l’architecture,
        le document de définition de l’architecture,
        les données de références sur les spécialités NHS,
        le document décrivant les exigences de la PoC,
        la feuille de route ;
    Vérifiez que les deux repositories contiennent toutes les parties demandées dans le mail de l’énoncé.
        N’oubliez pas d’ajouter tous les fichiers utiles au repository du code : les fichiers de configurations du pipeline CI / CD, les fichiers utiles aux tests, etc. 

## Livrables

1. Un document TXT contenant un lien vers le **repository GitHub** de code contenant : 

- le code de la PoC versionné grâce à un workflow adapté ;
- le ou les fichiers de configuration de la chaîne de build d’intégration continue ;
- le code des tests implémentés ;
- un fichier readme.md décrivant les points clés.

3. Un document TXT contenant le lien vers le **repository architectural** contenant : 

- tous les documents existants ;
- un nouveau document de reporting décrivant les résultats obtenus par la PoC.
