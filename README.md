# singular-keycloak-database-federation

Keycloak User Storage SPI for Relational Databases (Keycloak User Federation, supports postgresql, mysql, oracle and mysql).


## Usage

    Fully compatible with Singular Studio NOCODE. See https://www.studio.opensingular.com/
    

## Configuration

Keycloak User Federation Screen Shot

![Sample Screenshot](screen.png)


## Limitations

    - Do not allow user information update, including password update
    - Do not supports user roles our groups
    - Do not support paged queries



## Build

    - mvn clean install

## Deployment

    - Deploy `.ear` to wildfly (keycloak)

