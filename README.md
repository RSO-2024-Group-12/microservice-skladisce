# Skladišče (microservice-skladisce)

Mikrostoritev omogoča upravljanje zaloge in rezervacij posameznih izdelkov. 
Trenutno stanje skladišča se izračuna na podlagi preteklih dogodkov (Event Sourcing).
Ti dogodki so: 
- dodajanje zaloge za izdelek
- odstranjevanje zaloge za izdelek
- dodajanje izdelkov v košarico
- odstranjevanje izdelkov iz košarice
- posodobitev količine izdelkov v košarici
- prodaja izdelka

## Namen

- pridobitev zaloge in števila rezervacij za podani izdelek
- dodajanje in odstranjevanje zaloge za podani izdelek
- preverjanje količine zaloge za potrebe rezervacije izdelkov 

## Tehnologije

- Java 21
- Quarkus
- PostgreSQL
- Hibernate ORM
- REST
- Kafka (Event Sourcing)
- OpenAPI
- Swagger

## Integracije

### Odjemalci

Spodaj so navedene mikrostoritve, ki uporabljajo microservice-skladisce.

| Mikrostoritev          | Komunikacija | Namen                                |
|------------------------|--------------|--------------------------------------|
| microservice-skladisce | REST (GET)   | pridobitev podatkov o zalogi izdelka |
| microservice-skladisce | REST (POST)  | dodajanje novega izdelka v skladišče |
 | microservice-kosarica | REST (POST) | dodajanje novega dogodka v skladišče  |

## API

### REST
- `GET /v1/skladisce/zaloga/{id}` - pridobivanje zaloge posameznega izdelka
- `POST /v1/skladisce/zaloga` - dodajanje zaloge novega izdelka (izvede le ob dodajanju novega izdelka in le enkrat)
- `POST /v1/skladisce` - dodajanje novega dogodka v skladišče

## Razvoj in zagon

### Lokalni zagon v razvojnem načinu

Za zagon aplikacije s podporo za "vroče" ponovno nalaganje kode (live coding) uporabite:

```shell script
./mvnw quarkus:dev
```

Aplikacija bo privzeto dostopna na `http://localhost:8082`. Razvojni vmesnik (Dev UI) je na voljo na `http://localhost:8082/q/dev/`.

### Pakiranje aplikacije

Za pakiranje aplikacije v JAR datoteko:

```shell script
./mvnw package
```

Za izdelavo *über-jar* (vsebuje vse odvisnosti):

```shell script
./mvnw package -Dquarkus.package.jar.type=uber-jar
```

### Izgradnja Docker slike

Aplikacijo lahko zapakirate v Docker sliko z ukazom:

```shell script
docker build -t nakupify/microservice-skladisce .
```

## Konfiguracija

Konfiguracijski parametri se nahajajo v `src/main/resources/application.properties`. Glavne nastavitve vključujejo:

- `quarkus.datasource.jdbc.url`: Povezava do PostgreSQL baze.
- `mp.messaging.outgoing.orders-out.connector`: Nastavitve za povezavo s Kafka.

## Avtomatski testi

Za zagon vseh testov uporabite:

```shell script
./mvnw test
```