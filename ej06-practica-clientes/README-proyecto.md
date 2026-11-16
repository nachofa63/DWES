# Clientes de la tienda

## Qué es

Aplicación Java estructurada en capas para gestionar el alta, consulta y baja de clientes aplicando reglas de negocio y minimización de datos.

## Estructura

```mermaid
flowchart LR
    A[App] --> F[DaoFactory]
    A --> S[ClienteService]
    S --> M[ClienteMapper]
    S --> I[ClienteDao]
    J[ClienteDaoJdbc] --> I
    Me[ClienteDaoMemoria] --> I