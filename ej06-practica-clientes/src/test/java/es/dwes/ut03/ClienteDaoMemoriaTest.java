package es.dwes.ut03;

/** Hereda el contrato: la versión en memoria debe comportarse igual que la de JDBC. */
class ClienteDaoMemoriaTest extends ClienteDaoContratoTest {

    @Override
    protected ClienteDao crearDaoConDosClientes() {
        ClienteDaoMemoria memoria = new ClienteDaoMemoria();
        memoria.insertar("Ana", "ana@ejemplo.es");
        memoria.insertar("Luis", "luis@ejemplo.es");
        return memoria;
    }
}
