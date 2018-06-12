# Éco Pompe
<a href="https://github.com/PyvesB/alexa-eco-pompe/releases">
<img src ="https://img.shields.io/github/release/PyvesB/alexa-eco-pompe.svg?label=version" />
</a>
<a href="https://github.com/PyvesB/alexa-eco-pompe/issues">
<img src ="https://img.shields.io/github/issues/PyvesB/alexa-eco-pompe.svg?label=tickets%20ouverts" />
</a>
<br/>

**Faites des économies en demandant à Alexa de trouver les pompes à essence les moins chères !**

<p align="center">
<img src ="https://github.com/PyvesB/alexa-eco-pompe/blob/master/images/banner.jpg?raw=true"/>
<br/>
<i><sub>Photo de Peter Heeling.</sub></i>
</p>

# :speech_balloon: La Skill en bref

Éco Pompe vous permet de trouver les pompes à essence les moins chères, pour le carburant de votre choix ! Spécifiez simplement un carburant (gazole, sans plomb 95, sans plomb 98, E10, E85, GPL) suivi d'une ville, d'un département ou d'une distance. Éco Pompe recherchera alors toutes les pompes satisfaisant ce critère géographique, en privilégiant celles qui ont actualisé leurs prix il y a moins d'une semaine. La Skill vous indiquera ensuite le nom de la station-service si disponible, son adresse, le prix du carburant ainsi que la date de dernière mise à jour, et résumera également ces informations dans une carte affichée dans l'application Alexa. Ne dépensez plus un centime de trop et payez votre carburant au juste prix !

Les nouveautés et mises à jour seront documentées [ici](https://github.com/PyvesB/alexa-eco-pompe/releases).

# `$ code`

Vous voulez rendre Éco Pompe encore plus rapide et efficace ? Toute contribution est la bienvenue, ouvrez un "pull request" et partagez votre code ! Faites une copie du dépôt en cliquant sur l'icône "Fork" en haut à droite de la page, compilez le projet en utilisant `mvn clean install` et c'est parti !

# :e-mail: Support

Une idée ? Des soucis avec la Skill ou besoin d'aide ? N'hésitez pas à ouvrir un [**ticket**](https://github.com/PyvesB/alexa-eco-pompe/issues) ! Vous trouvez le project utile, amusant ou intéressant ? Pensez à mettre une **étoile** :star: sur le dépôt en cliquant sur l'icône en haut à droite de cette page !

# :copyright: Licence

Éco Pompe est soumis à la licence [GNU Affero General Public License v3.0](https://github.com/PyvesB/alexa-eco-pompe/blob/master/LICENSE.md). Toute utilisation, modification ou distribution du code source disponible dans ce dépôt ne pourra se faire que sous réserve du respect des conditions de ladite licence.

# :books: Mentions

Un grand merci aux organismes suivants, qui fournissent des jeux de données utilisés par Éco Pompe :
- Ministère de l'Économie et des Finances, [Prix des carburants en France](https://www.data.gouv.fr/fr/datasets/prix-des-carburants-en-france) sous [License Ouverte](https://www.etalab.gouv.fr/wp-content/uploads/2017/04/ETALAB-Licence-Ouverte-v2.0.pdf).
- [OpenStreetMap](https://www.openstreetmap.org) sous licence [ODbL](https://opendatacommons.org/licences/odbl/1.0/). © les contributeurs d’OpenStreetMap.

Les librairies suivantes, sous licence Apache 2.0, sont intégrées et déployées avec le code :
- [Protostuff](https://github.com/protostuff/protostuff)
- [Jackson](https://github.com/FasterXML/jackson)
- [Apache Commons](https://commons.apache.org/)
- [Log4j2](https://logging.apache.org/log4j/2.x/)
- Diverses librairies AWS

Merci à [Creative Tail](https://www.creativetail.com/) dont l'une des créations sous licence CC 4.0 a servi de base au logo "Éco Pompe" !
