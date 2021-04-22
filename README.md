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

## Custom attributes

    - Just add a mapper to client mappers with the same name as the returned column alias in your queries. Use mapper type "User Attribute"
    - See the example below
    
    [Sample Screenshot 2](screen2.png)


## Build

    - mvn clean install

## Deployment

    - Deploy `.ear` to wildfly (keycloak)

